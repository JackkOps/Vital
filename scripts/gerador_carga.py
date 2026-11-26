#!/usr/bin/env python3
"""
Gerador de carga do Rota Vital — produz tráfego realista para a observabilidade.

POR QUE ISTO EXISTE
-------------------
Dashboard sem dados vale metade da nota (critério E4/E7 do enunciado), e um
gráfico de latência p95 sobre 3 requisições não significa nada. Este script
mantém tráfego constante com uma taxa de erro conhecida, para que haja algo
de fato observável em métricas, logs e traces.

O QUE ELE GERA
--------------
- Requisições de LEITURA distribuídas pelas rotas reais da API.
- Uma fração configurável (padrão 3%) de requisições que falham DE PROPÓSITO,
  batendo em recursos inexistentes. Isso alimenta:
    * http_server_requests_seconds{status="404"}  -> painel de taxa de erro
    * logback_events{level="warn|error"}          -> logs no Loki (F6)
- Latência variável, porque cada rota faz consultas de custo diferente.

Usa apenas a biblioteca padrão do Python: nada para instalar.

USO
---
    python3 scripts/gerador_carga.py                     # 10 min, 8 workers
    python3 scripts/gerador_carga.py --duracao 0          # roda até Ctrl+C
    python3 scripts/gerador_carga.py --rps 50 --erro 0.10 # mais pesado
"""

import argparse
import json
import random
import signal
import sys
import threading
import time
import urllib.error
import urllib.request
import uuid
from collections import Counter
from datetime import date, timedelta

# Rotas de leitura reais da API (ver os @GetMapping dos controllers).
# O peso simula uso realista: consulta de estoque é mais comum que painel.
# IMPORTANTE: todas verificadas respondendo 200. Rotas sem GET (ex.
# /api/alocacoes, que só tem POST) ficariam fora e inflariam a taxa de erro,
# tornando o percentual de falha do painel não confiável.
ROTAS_OK = [
    ("/api/bolsas", 25),
    ("/api/solicitacoes", 20),
    ("/api/remessas", 12),
    ("/api/solicitacoes/fila", 10),
    ("/api/rotas", 8),
    ("/api/indicadores", 7),
    ("/api/validade/alertas", 7),
    ("/api/rotas/rede", 6),
    ("/api/historico", 5),
]

# Rotas que falham de propósito, separadas por CLASSE de erro, porque os painéis
# respondem a perguntas diferentes:
#   4xx = erro do CLIENTE ("você pediu algo que não existe") -> normal em produção
#   5xx = erro do SERVIDOR ("eu falhei")                     -> indica problema real
# O painel de taxa de erro exigido pelo enunciado filtra status=~"5..", então sem
# tráfego 5xx aquele painel ficaria permanentemente em zero.
ROTAS_ERRO_CLIENTE = [
    "/api/bolsas/999999",
    "/api/solicitacoes/999999",
    "/api/remessas/999999",
    "/api/rotas/999999",
    "/api/nao-existe",
]

ROTAS_ERRO_SERVIDOR = [
    "/api/diagnostico/erro",          # HTTP 500
    "/api/diagnostico/indisponivel",  # HTTP 503
]

# ---------------------------------------------------------------------------
# Operações de ESCRITA
#
# Necessárias para as métricas de negócio. Elas vêm de agregações sobre o banco
# (GROUP BY por status/componente) e de contadores de operação: com o banco vazio
# o GROUP BY devolve zero linhas, o MultiGauge não publica série nenhuma e as
# métricas simplesmente não aparecem no /actuator/prometheus. Tráfego somente de
# leitura, portanto, deixa o painel de negócio em branco.
# ---------------------------------------------------------------------------

TIPOS_SANGUINEOS = [
    "A_POSITIVO", "A_NEGATIVO", "B_POSITIVO", "B_NEGATIVO",
    "AB_POSITIVO", "AB_NEGATIVO", "O_POSITIVO", "O_NEGATIVO",
]
# Distribuição aproximada da população brasileira, para o estoque não ficar uniforme.
PESOS_SANGUINEOS = [34, 8, 9, 2, 3, 1, 36, 7]

COMPONENTES = ["HEMACIAS", "PLASMA", "PLAQUETAS", "SANGUE_TOTAL"]
URGENCIAS = ["ELETIVA", "ELETIVA", "URGENTE", "EMERGENCIA"]  # eletiva é mais comum

HOSPITAIS = [
    "Hospital da Restauracao", "Hospital Jayme da Fonte",
    "Hospital Agamenon Magalhaes", "Hospital Getulio Vargas",
    "Hospital Barao de Lucena", "Hospital Otavio de Freitas",
]
# Locais da rede criados pela migration V4: 1 = HEMOPE (origem), 2..8 = hospitais.
ORIGEM_REDE = 1
DESTINOS_REDE = [2, 3, 4, 5, 6, 7, 8]

_parar = threading.Event()
_lock = threading.Lock()
_stats = Counter()
_latencias = []


def _sortear_rota():
    """Escolhe uma rota respeitando os pesos de ROTAS_OK."""
    total = sum(peso for _, peso in ROTAS_OK)
    alvo = random.uniform(0, total)
    acumulado = 0
    for rota, peso in ROTAS_OK:
        acumulado += peso
        if alvo <= acumulado:
            return rota
    return ROTAS_OK[0][0]


def _requisitar(base_url, rota, timeout):
    """Faz um GET e devolve (status, duracao_segundos)."""
    req = urllib.request.Request(
        base_url + rota,
        headers={"User-Agent": "rotavital-gerador-carga/1.0"},
    )
    inicio = time.perf_counter()
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            resp.read()
            return resp.status, time.perf_counter() - inicio
    except urllib.error.HTTPError as e:
        # 404/400/500 chegam aqui: é resposta do servidor, não falha de rede.
        return e.code, time.perf_counter() - inicio
    except Exception:
        return 0, time.perf_counter() - inicio  # 0 = não conseguiu falar com a API


def _post(base_url, rota, corpo, timeout):
    """Faz um POST com corpo JSON e devolve (status, duracao, resposta_decodificada)."""
    dados = json.dumps(corpo).encode("utf-8") if corpo is not None else b"{}"
    req = urllib.request.Request(
        base_url + rota,
        data=dados,
        method="POST",
        headers={"Content-Type": "application/json",
                 "User-Agent": "rotavital-gerador-carga/1.0"},
    )
    inicio = time.perf_counter()
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            texto = resp.read()
            try:
                return resp.status, time.perf_counter() - inicio, json.loads(texto)
            except ValueError:
                return resp.status, time.perf_counter() - inicio, None
    except urllib.error.HTTPError as e:
        return e.code, time.perf_counter() - inicio, None
    except Exception:
        return 0, time.perf_counter() - inicio, None


def _corpo_bolsa():
    """Bolsa com identificador único e validade folgada (para não vencer sozinha)."""
    hoje = date.today()
    return {
        "identificador": f"BS-{uuid.uuid4().hex[:10].upper()}",
        "tipoSanguineo": random.choices(TIPOS_SANGUINEOS, weights=PESOS_SANGUINEOS)[0],
        "tipoComponente": random.choice(COMPONENTES),
        "dataColeta": hoje.isoformat(),
        "dataValidade": (hoje + timedelta(days=random.randint(5, 25))).isoformat(),
        "volume": random.choice([420, 450, 480, 500]),
    }


def _corpo_solicitacao():
    return {
        "nomeHospital": random.choice(HOSPITAIS),
        "tipoSanguineo": random.choices(TIPOS_SANGUINEOS, weights=PESOS_SANGUINEOS)[0],
        "tipoComponente": random.choice(COMPONENTES),
        "quantidade": random.randint(1, 3),
        "nivelUrgencia": random.choice(URGENCIAS),
    }


def _corpo_rota():
    destinos = random.sample(DESTINOS_REDE, random.randint(1, 4))
    return {"origemId": ORIGEM_REDE, "destinoIds": destinos,
            "solicitacaoIds": None, "janelaMinutos": None}


def _operacao_escrita(base_url, timeout):
    """
    Executa uma operação de negócio. Devolve (rotulo, status, duracao).

    As três alimentam métricas distintas:
      bolsa      -> gauge de estoque (rotavital_bolsas_estoque)
      alocacao   -> counter (rotavital_bolsas_alocadas_total) + gauge de solicitações
      rota       -> timer (rotavital_rotas_calculo_seconds), o Dijkstra do projeto
    """
    sorteio = random.random()

    if sorteio < 0.55:
        st, dur, _ = _post(base_url, "/api/bolsas", _corpo_bolsa(), timeout)
        return "bolsa", st, dur

    if sorteio < 0.90:
        st, dur, corpo = _post(base_url, "/api/solicitacoes", _corpo_solicitacao(), timeout)
        if st in (200, 201) and isinstance(corpo, dict) and corpo.get("id"):
            # Aloca em seguida: é o que incrementa o contador de negócio. Pode
            # devolver 4xx legitimamente quando não há bolsa compatível em estoque.
            st2, dur2, _ = _post(
                base_url, f"/api/alocacoes/solicitacoes/{corpo['id']}/alocar", None, timeout)
            return "alocacao", st2, dur + dur2
        return "solicitacao", st, dur

    st, dur, _ = _post(base_url, "/api/rotas", _corpo_rota(), timeout)
    return "rota", st, dur


def _semear(base_url, timeout, bolsas, solicitacoes):
    """
    Popula o banco antes de medir.

    Sem estoque inicial toda alocação falha por falta de bolsa compatível, o
    contador de negócio fica em zero e a taxa de erro medida estoura.
    """
    print(f"Semeando: {bolsas} bolsas e {solicitacoes} solicitacoes...", flush=True)
    criadas = falhas = 0
    for _ in range(bolsas):
        st, _, _ = _post(base_url, "/api/bolsas", _corpo_bolsa(), timeout)
        criadas += 1 if st in (200, 201) else 0
        falhas += 0 if st in (200, 201) else 1
    alocadas = 0
    for _ in range(solicitacoes):
        st, _, corpo = _post(base_url, "/api/solicitacoes", _corpo_solicitacao(), timeout)
        if st in (200, 201) and isinstance(corpo, dict) and corpo.get("id"):
            st2, _, _ = _post(
                base_url, f"/api/alocacoes/solicitacoes/{corpo['id']}/alocar", None, timeout)
            alocadas += 1 if st2 in (200, 201) else 0
    print(f"Semeadura: {criadas} bolsas criadas ({falhas} falhas), "
          f"{alocadas} alocacoes bem-sucedidas\n", flush=True)


def _worker(base_url, intervalo, taxa_erro, timeout, fracao_servidor, taxa_escrita):
    """Loop de um worker: sorteia a próxima ação, executa, contabiliza."""
    while not _parar.is_set():
        if random.random() < taxa_escrita:
            # Escrita: alimenta as métricas de negócio.
            rotulo, status, duracao = _operacao_escrita(base_url, timeout)
            with _lock:
                _stats[status] += 1
                _stats["total"] += 1
                _stats["escrita:" + rotulo] += 1
                _latencias.append(duracao)
            _parar.wait(intervalo * random.uniform(0.5, 1.5))
            continue

        if random.random() < taxa_erro:
            # Dentro da fatia de erro, parte vira 5xx e parte 4xx.
            if random.random() < fracao_servidor:
                rota = random.choice(ROTAS_ERRO_SERVIDOR)
            else:
                rota = random.choice(ROTAS_ERRO_CLIENTE)
        else:
            rota = _sortear_rota()

        status, duracao = _requisitar(base_url, rota, timeout)

        with _lock:
            _stats[status] += 1
            _stats["total"] += 1
            _latencias.append(duracao)

        # Jitter: sem variação aleatória, todos os workers batem em uníssono
        # e o gráfico fica com degraus artificiais.
        _parar.wait(intervalo * random.uniform(0.5, 1.5))


def _percentil(valores, p):
    if not valores:
        return 0.0
    ordenados = sorted(valores)
    indice = min(int(len(ordenados) * p), len(ordenados) - 1)
    return ordenados[indice]


def _relatorio(inicio):
    with _lock:
        total = _stats["total"]
        erros = sum(v for k, v in _stats.items()
                    if isinstance(k, int) and (k == 0 or k >= 400))
        amostra = list(_latencias[-2000:])
        status = {k: v for k, v in _stats.items() if isinstance(k, int)}

    decorrido = max(time.time() - inicio, 0.001)
    pct_erro = (erros / total * 100) if total else 0.0
    print(
        f"[{decorrido:6.0f}s] {total:6d} reqs "
        f"| {total / decorrido:5.1f} req/s "
        f"| erros {pct_erro:4.1f}% "
        f"| p50 {_percentil(amostra, 0.50) * 1000:5.1f}ms "
        f"| p95 {_percentil(amostra, 0.95) * 1000:6.1f}ms "
        f"| status {dict(sorted(status.items()))}",
        flush=True,
    )


def main():
    ap = argparse.ArgumentParser(description="Gerador de carga do Rota Vital")
    ap.add_argument("--url", default="http://localhost:8080",
                    help="base da API (padrão: http://localhost:8080)")
    ap.add_argument("--workers", type=int, default=8,
                    help="threads concorrentes (padrão: 8)")
    ap.add_argument("--rps", type=float, default=20.0,
                    help="requisições por segundo desejadas (padrão: 20)")
    ap.add_argument("--erro", type=float, default=0.03,
                    help="fração de requisições que falham de propósito (padrão: 0.03 = 3%%)")
    ap.add_argument("--fracao-5xx", type=float, default=0.4,
                    help="fatia dos erros que deve ser 5xx em vez de 4xx (padrão: 0.4)")
    ap.add_argument("--escrita", type=float, default=0.08,
                    help="fração de operações de ESCRITA, que alimentam as métricas "
                         "de negócio (padrão: 0.08 = 8%%)")
    ap.add_argument("--semear", type=int, default=0,
                    help="cria N bolsas e N/4 solicitações antes de iniciar; "
                         "use na primeira execução com banco vazio (padrão: 0)")
    ap.add_argument("--duracao", type=int, default=600,
                    help="segundos de execução; 0 = até Ctrl+C (padrão: 600)")
    ap.add_argument("--timeout", type=float, default=10.0,
                    help="timeout de cada requisição em segundos")
    args = ap.parse_args()

    # Intervalo que cada worker espera entre requisições para atingir o rps alvo.
    intervalo = args.workers / args.rps if args.rps > 0 else 0.05

    print(f"Alvo:     {args.url}")
    print(f"Carga:    {args.workers} workers, ~{args.rps} req/s, "
          f"{args.erro * 100:.0f}% de erro proposital")
    print(f"Duração:  {'até Ctrl+C' if args.duracao == 0 else str(args.duracao) + 's'}")

    status, _ = _requisitar(args.url, "/api/bolsas", args.timeout)
    if status == 0:
        print(f"\nERRO: não consegui falar com {args.url}. "
              f"A API está no ar? (docker compose ps)", file=sys.stderr)
        return 1
    print(f"Conexao:  OK (HTTP {status})\n")

    if args.semear > 0:
        _semear(args.url, args.timeout, args.semear, max(1, args.semear // 4))

    signal.signal(signal.SIGINT, lambda *_: _parar.set())
    signal.signal(signal.SIGTERM, lambda *_: _parar.set())

    threads = [
        threading.Thread(target=_worker,
                         args=(args.url, intervalo, args.erro, args.timeout,
                               args.fracao_5xx, args.escrita),
                         daemon=True)
        for _ in range(args.workers)
    ]
    for t in threads:
        t.start()

    inicio = time.time()
    try:
        while not _parar.is_set():
            _parar.wait(10)
            _relatorio(inicio)
            if args.duracao and (time.time() - inicio) >= args.duracao:
                _parar.set()
    except KeyboardInterrupt:
        _parar.set()

    for t in threads:
        t.join(timeout=2)

    print("\n--- resumo final ---")
    _relatorio(inicio)
    return 0


if __name__ == "__main__":
    sys.exit(main())
