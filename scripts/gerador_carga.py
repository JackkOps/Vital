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
import random
import signal
import sys
import threading
import time
import urllib.error
import urllib.request
from collections import Counter

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

# Rotas que falham de propósito. IDs altos não existem no banco -> 404.
ROTAS_ERRO = [
    "/api/bolsas/999999",
    "/api/solicitacoes/999999",
    "/api/remessas/999999",
    "/api/rotas/999999",
    "/api/nao-existe",
]

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


def _worker(base_url, intervalo, taxa_erro, timeout):
    """Loop de um worker: sorteia rota, requisita, contabiliza."""
    while not _parar.is_set():
        if random.random() < taxa_erro:
            rota = random.choice(ROTAS_ERRO)
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
    print(f"Conexão:  OK (HTTP {status})\n")

    signal.signal(signal.SIGINT, lambda *_: _parar.set())
    signal.signal(signal.SIGTERM, lambda *_: _parar.set())

    threads = [
        threading.Thread(target=_worker,
                         args=(args.url, intervalo, args.erro, args.timeout),
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
