"""
Mede o endpoint GET /api/relatorios/cobertura nas versoes sequencial e com threads.

Uso (com a API rodando em localhost:8080):
    python medir.py

Gera resultados.csv (todas as execucoes), resumo.csv (mediana + speedup) e grafico.png.
"""
import csv
import json
import statistics
import time
import urllib.request
from pathlib import Path

URL = "http://localhost:8080/api/relatorios/cobertura"
SOLICITACOES = 1000
TAMANHOS = [100_000, 1_000_000]
CONFIGURACOES = [(1, False), (2, False), (4, False), (8, False), (2, True), (4, True), (8, True)]
REPETICOES = 5
PASTA = Path(__file__).parent


def chamar(bolsas, threads, virtual):
    url = f"{URL}?bolsas={bolsas}&solicitacoes={SOLICITACOES}&threads={threads}&virtual={str(virtual).lower()}"
    inicio = time.perf_counter()
    with urllib.request.urlopen(url, timeout=600) as resposta:
        corpo = json.loads(resposta.read())
    return corpo, (time.perf_counter() - inicio) * 1000


def main():
    execucoes, resumo = [], []
    for bolsas in TAMANHOS:
        # Aquecimento: gera/cacheia a massa de dados e deixa o JIT compilar todos os caminhos.
        referencia = chamar(bolsas, 1, False)[0]["porTipo"]
        for threads, virtual in CONFIGURACOES[1:]:
            chamar(bolsas, threads, virtual)

        # Rodadas intercaladas: cada rodada passa por todas as configuracoes, para que variacoes
        # da maquina (turbo, temperatura, outros processos) afetem todas igualmente.
        tempos = {config: [] for config in CONFIGURACOES}
        for rodada in range(1, REPETICOES + 1):
            for threads, virtual in CONFIGURACOES:
                corpo, tempo_http = chamar(bolsas, threads, virtual)
                if corpo["porTipo"] != referencia:
                    raise SystemExit(f"Resultado divergente: {bolsas} bolsas, {threads} threads, virtual={virtual}")
                tempos[(threads, virtual)].append(tempo_http)
                execucoes.append([bolsas, corpo["modo"], threads, rodada,
                                  round(corpo["tempoProcessamentoMs"], 2), round(tempo_http, 2)])

        base = statistics.median(tempos[(1, False)])
        for threads, virtual in CONFIGURACOES:
            mediana = statistics.median(tempos[(threads, virtual)])
            modo = "VIRTUAL" if virtual else ("SEQUENCIAL" if threads == 1 else "PLATAFORMA")
            resumo.append([bolsas, modo, threads, round(mediana, 1), round(base / mediana, 2)])
            print(f"{bolsas:>9} bolsas | {modo:<10} | {threads} threads | {mediana:9.1f} ms | speedup {base / mediana:.2f}x")

    with open(PASTA / "resultados.csv", "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow(["bolsas", "modo", "threads", "rodada", "tempo_processamento_ms", "tempo_resposta_ms"])
        w.writerows(execucoes)
    with open(PASTA / "resumo.csv", "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow(["bolsas", "modo", "threads", "mediana_resposta_ms", "speedup"])
        w.writerows(resumo)

    gerar_grafico(resumo)


def gerar_grafico(resumo):
    import matplotlib
    matplotlib.use("Agg")
    import matplotlib.pyplot as plt

    fig, eixos = plt.subplots(1, 2, figsize=(12, 4.5))
    for bolsas in TAMANHOS:
        plataforma = [r for r in resumo if r[0] == bolsas and r[1] != "VIRTUAL"]
        virtuais = [r for r in resumo if r[0] == bolsas and r[1] in ("VIRTUAL", "SEQUENCIAL")]
        rotulo = f"{bolsas:,} bolsas".replace(",", ".")
        for dados, estilo, sufixo in ((plataforma, "-o", "plataforma"), (virtuais, "--s", "virtual")):
            dados = sorted(dados, key=lambda r: r[2])
            x = [r[2] for r in dados]
            eixos[0].plot(x, [r[3] for r in dados], estilo, label=f"{rotulo} ({sufixo})")
            eixos[1].plot(x, [r[4] for r in dados], estilo, label=f"{rotulo} ({sufixo})")

    eixos[1].plot([1, 8], [1, 8], ":", color="gray", label="speedup ideal (linear)")
    eixos[0].set(title="Tempo de resposta do endpoint", xlabel="threads", ylabel="ms (mediana)", yscale="log")
    eixos[1].set(title="Speedup em relacao a versao sequencial", xlabel="threads", ylabel="speedup")
    for eixo in eixos:
        eixo.set_xticks([1, 2, 4, 8])
        eixo.grid(alpha=0.3)
        eixo.legend(fontsize=8)
    fig.tight_layout()
    fig.savefig(PASTA / "grafico.png", dpi=150)


if __name__ == "__main__":
    main()
