"""
Gera o PDF de entrega da atividade a partir de resumo.csv, resumo-sem-afinidade.csv e grafico.png.

Uso:  python gerar_pdf.py      (requer: pip install reportlab)
"""
import csv
from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_JUSTIFY
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.units import cm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (Image, KeepTogether, PageBreak, Paragraph, Preformatted, SimpleDocTemplate,
                                Spacer, Table, TableStyle)

PASTA = Path(__file__).parent
SAIDA = PASTA / "Atividade-Threads-RotaVital.pdf"
REPOSITORIO = "https://github.com/PCarvalho04/Vital/tree/feature/processamento-paralelo"
PULL_REQUEST = "https://github.com/PCarvalho04/Vital/pull/1"

FONTES = Path("C:/Windows/Fonts")
pdfmetrics.registerFont(TTFont("Arial", FONTES / "arial.ttf"))
pdfmetrics.registerFont(TTFont("Arial-Bold", FONTES / "arialbd.ttf"))
pdfmetrics.registerFont(TTFont("Arial-Italic", FONTES / "ariali.ttf"))
pdfmetrics.registerFont(TTFont("Arial-BoldItalic", FONTES / "arialbi.ttf"))
pdfmetrics.registerFont(TTFont("Consolas", FONTES / "consola.ttf"))
pdfmetrics.registerFontFamily("Arial", normal="Arial", bold="Arial-Bold", italic="Arial-Italic",
                              boldItalic="Arial-BoldItalic")

VERMELHO = colors.HexColor("#9B1C1C")
CINZA = colors.HexColor("#555555")
FUNDO = colors.HexColor("#F4F4F4")

corpo = ParagraphStyle("corpo", fontName="Arial", fontSize=10.5, leading=14.5, alignment=TA_JUSTIFY,
                       spaceAfter=6)
item = ParagraphStyle("item", parent=corpo, leftIndent=14, bulletIndent=4, spaceAfter=3)
h1 = ParagraphStyle("h1", fontName="Arial-Bold", fontSize=15, leading=19, textColor=VERMELHO,
                    spaceBefore=4, spaceAfter=8)
h2 = ParagraphStyle("h2", fontName="Arial-Bold", fontSize=11.5, leading=15, spaceBefore=8, spaceAfter=4)
legenda = ParagraphStyle("legenda", parent=corpo, fontSize=9, leading=12, textColor=CINZA, alignment=TA_CENTER)
celula = ParagraphStyle("celula", fontName="Arial", fontSize=9, leading=11.5)
celula_negrito = ParagraphStyle("celula_negrito", parent=celula, fontName="Arial-Bold")
codigo = ParagraphStyle("codigo", fontName="Consolas", fontSize=8.3, leading=10.5, backColor=FUNDO,
                        borderPadding=6, leftIndent=6, rightIndent=6, spaceBefore=4, spaceAfter=10)


def p(texto, estilo=corpo):
    return Paragraph(texto, estilo)


def itens(*textos):
    return [Paragraph(t, item, bulletText="•") for t in textos]


def tabela(linhas, larguras, cabecalho=True, alinhar_direita=()):
    dados = [[Paragraph(str(c), celula_negrito if cabecalho and i == 0 else celula) for c in linha]
             for i, linha in enumerate(linhas)]
    t = Table(dados, colWidths=larguras, repeatRows=1 if cabecalho else 0)
    estilo = [
        ("GRID", (0, 0), (-1, -1), 0.4, colors.HexColor("#BBBBBB")),
        ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
        ("TOPPADDING", (0, 0), (-1, -1), 3),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 3),
    ]
    if cabecalho:
        estilo.append(("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#EFE3E3")))
    t.setStyle(TableStyle(estilo))
    return t


def ler_resumo(nome):
    with open(PASTA / nome, encoding="utf-8") as f:
        return list(csv.DictReader(f))


def ms(valor):
    return f"{float(valor):,.0f}".replace(",", ".")


def x(valor):
    return f"{float(valor):.2f}".replace(".", ",") + "x"


def rodape(canvas, doc):
    if doc.page == 1:
        return
    canvas.saveState()
    canvas.setFont("Arial", 8)
    canvas.setFillColor(CINZA)
    canvas.drawString(2 * cm, 1.2 * cm, "Rota Vital — Processamento paralelo na camada de aplicação")
    canvas.drawRightString(A4[0] - 2 * cm, 1.2 * cm, f"Página {doc.page}")
    canvas.restoreState()


def capa():
    equipe = [
        "Allan Max de Jesus Rodrigues de Lima", "Boniek Araujo dos Santos Junior",
        "Caio Cesar Leandro Amorim", "Luan Ventura Ferreira de Moura", "Miguel Victor Lussac Barboza",
        "Pedro Augusto Carvalho Araujo", "Vinicius Pessoa de Albuquerque", "Wesley Yuri da Silva",
    ]
    centro = ParagraphStyle("centro", parent=corpo, alignment=TA_CENTER)
    return [
        Spacer(1, 3 * cm),
        p("CESAR School — Análise e Desenvolvimento de Sistemas (2026.2)", centro),
        p("Infraestrutura de Software · Projeto Integrador", centro),
        Spacer(1, 2.2 * cm),
        p("Rota Vital", ParagraphStyle("t", parent=h1, fontSize=30, leading=36, alignment=TA_CENTER)),
        p("Processamento paralelo na camada de aplicação", ParagraphStyle(
            "st", parent=h2, fontSize=16, leading=21, alignment=TA_CENTER, textColor=CINZA)),
        Spacer(1, 0.4 * cm),
        p("Relatório de cobertura nacional: cruzamento solicitações × estoque com threads", centro),
        Spacer(1, 2.4 * cm),
        p("<b>Equipe</b>", centro),
        *[p(nome, ParagraphStyle("eq", parent=centro, spaceAfter=1)) for nome in equipe],
        Spacer(1, 2 * cm),
        p(f"Código: <link href='{REPOSITORIO}' color='#9B1C1C'>{REPOSITORIO}</link>", centro),
        p(f"Pull request: <link href='{PULL_REQUEST}' color='#9B1C1C'>{PULL_REQUEST}</link>", centro),
        Spacer(1, 0.6 * cm),
        p("Recife, setembro de 2026", centro),
        PageBreak(),
    ]


def justificativa():
    candidatas = [
        ["Candidata", "Big-O", "Gargalo na escala nacional", "Particionável?", "Decisão"],
        ["Estatísticas do estoque (contagem por tipo, status, validade)", "O(n)",
         "Banco — resolve-se com GROUP BY e índices", "Sim", "Descartada: a resposta certa é SQL"],
        ["Alocação de uma bolsa para uma solicitação", "O(n log n)",
         "Banco (leitura do estoque) e transação", "Pouco — operação unitária", "Descartada"],
        ["Detecção de solicitações duplicadas", "O(n) com hash",
         "Banco — restrição de unicidade", "Sim", "Descartada: resolve-se no banco"],
        ["<b>Cruzamento solicitações × estoque (relatório de cobertura)</b>", "<b>O(S × B)</b>",
         "<b>CPU — regras avaliadas bolsa a bolsa</b>", "<b>Sim, por solicitação</b>", "<b>Escolhida</b>"],
    ]
    return [
        p("1. Justificativa", h1),
        p("O Rota Vital nasce como um sistema web em três camadas (apresentação, API e banco de dados). "
          "Assumindo a escala nacional — milhares de hospitais, estoque da ordem de milhões de bolsas e "
          "painéis consultados o dia inteiro —, buscamos uma operação da camada de aplicação cujo tempo seja "
          "dominado por <b>processamento</b>, e não por espera de banco ou rede, e que possa ser dividida em "
          "partes independentes."),
        p("1.1 Como chegamos à operação", h2),
        p("Seguimos o roteiro proposto, avaliando as operações que processam volume de dados:"),
        tabela(candidatas, [4.6 * cm, 1.9 * cm, 4.2 * cm, 2.6 * cm, 3.7 * cm]),
        Spacer(1, 6),
        p("1.2 A operação escolhida", h2),
        p("<b>Relatório de cobertura nacional.</b> Para cada solicitação de sangue registrada, o serviço "
          "percorre o estoque e conta quantas bolsas poderiam atendê-la, aplicando as regras do Rota Vital:"),
        *itens("a bolsa está com status <font name='Consolas'>DISPONIVEL</font>;",
               "é do mesmo componente solicitado (hemácias, plasma ou plaquetas);",
               "está dentro da validade;",
               "é compatível pelo sistema ABO/Rh com o tipo do receptor — a mesma regra usada na alocação;",
               "está armazenada a até <b>150 km</b> do hospital solicitante (abastecimento regional)."),
        p("O resultado é agregado por tipo sanguíneo: solicitações, solicitações atendíveis pelo estoque "
          "próximo, total de bolsas compatíveis e quantas delas vencem em até 7 dias (prioridade FEFO). É o "
          "tipo de painel que a rede consultaria continuamente para decidir remanejamentos entre unidades — "
          "com 100 mil bolsas, por exemplo, apenas 87 de 137 solicitações O− têm cobertura regional."),
        p("1.3 Complexidade da solução sequencial", h2),
        p("Sendo <b>S</b> o número de solicitações e <b>B</b> o número de bolsas, o algoritmo executa, para "
          "cada solicitação, uma varredura completa do estoque: S iterações externas × B iterações internas de "
          "custo constante (até cinco verificações), mais a agregação final em O(8). <b>Complexidade: "
          "O(S × B).</b> Com S = 1.000 e B = 1.000.000 são 10<super>9</super> verificações por requisição — "
          "cerca de 24 segundos em uma única thread."),
        p("1.4 Onde está o gargalo", h2),
        p("Na escala nacional o custo dominante é o <b>cálculo</b>. A leitura do estoque custa O(B) e pode ser "
          "mantida em cache, enquanto o cruzamento executa S × B verificações sobre os mesmos dados — ordens "
          "de grandeza a mais. Durante a execução a CPU fica 100% ocupada, sem espera de I/O."),
        p("Poderíamos resolver no SQL? Um <i>join</i> com as mesmas regras continuaria sendo O(S × B), só que "
          "executado no banco compartilhado por todos os hospitais. E pré-agregar o estoque não ajuda: como a "
          "distância depende da unidade que armazena a bolsa e a validade importa dia a dia, a chave de "
          "agrupamento seria unidade × tipo × componente × validade — 3.000 × 8 × 3 × ~65 ≈ 4,7 milhões de "
          "grupos, mais do que o próprio número de bolsas. O trabalho é inerentemente par a par."),
        p("Para isolar esse custo nas medições, os dados são gerados em memória (100 mil e 1 milhão de "
          "bolsas, 1.000 solicitações, 3.000 unidades de armazenamento e 5.000 hospitais distribuídos pelo "
          "território), com semente fixa — toda execução processa exatamente os mesmos dados — e mantidos em "
          "cache. O tempo medido no endpoint corresponde, portanto, ao processamento."),
        p("1.5 Por que os dados são particionáveis", h2),
        p("A contagem de cada solicitação é <b>independente</b> das demais: depende apenas da própria "
          "solicitação e do estoque, que é somente lido. Assim, a lista de solicitações é dividida em "
          "<i>p</i> fatias contíguas, cada thread acumula o resultado da sua fatia em contadores "
          "<b>próprios</b>, e ao final a thread principal soma as parciais. Como a soma é associativa e "
          "comutativa, a ordem de término das threads não altera o resultado; como nenhuma thread escreve em "
          "dados compartilhados, não há <i>locks</i> nem risco de <i>race condition</i>."),
        PageBreak(),
    ]


def servico():
    trecho = """try (ExecutorService executor = modo == Modo.VIRTUAL
        ? Executors.newVirtualThreadPerTaskExecutor()
        : Executors.newFixedThreadPool(threads)) {
    List<Future<Parcial>> futuros = new ArrayList<>(threads);
    int total = solicitacoes.size();
    for (int t = 0; t < threads; t++) {
        int inicio = (int) ((long) total * t / threads);        // fatia [inicio, fim)
        int fim = (int) ((long) total * (t + 1) / threads);
        futuros.add(executor.submit(() -> processarFatia(bolsas, solicitacoes, inicio, fim)));
    }
    Parcial resultado = new Parcial();
    for (Future<Parcial> futuro : futuros) {
        resultado.somar(futuro.get());                          // agregação ao final
    }
    return resultado.paraLista();
}"""
    arquivos = [
        ["Arquivo (backend/rotavital/...)", "Papel"],
        ["controller/RelatorioCoberturaController", "Endpoint GET /api/relatorios/cobertura e validação dos parâmetros"],
        ["service/CoberturaEstoqueService", "Versão sequencial, ExecutorService (plataforma) e virtual threads"],
        ["service/GeradorDadosService", "Massa de dados sintética em memória, semente fixa, em cache"],
        ["model/relatorio/BolsaEmEstoque, DemandaHospitalar", "Projeções de leitura com a localização"],
        ["test/.../CoberturaEstoqueServiceTest", "Igualdade sequencial × paralelo e regras de negócio"],
        ["docs/atividade-threads/medir.py", "Script de medição: CSVs e gráfico"],
    ]
    return [
        p("2. O serviço", h1),
        p("A operação foi implementada como endpoint real na API Spring Boot (Java 21) do Rota Vital: a "
          "requisição chega, o servidor processa e a resposta volta em JSON."),
        p("<font name='Consolas'>GET /api/relatorios/cobertura?bolsas=1000000&amp;threads=8</font>", ParagraphStyle("url", parent=corpo, alignment=TA_CENTER)),
        *itens("<font name='Consolas'>threads=1</font> executa a versão <b>sequencial</b>, na própria thread "
               "da requisição;",
               "<font name='Consolas'>threads=2, 4, 8</font> usam um <b>ExecutorService</b> com pool fixo de "
               "threads de plataforma, uma fatia por thread;",
               "<font name='Consolas'>virtual=true</font> usa <b>virtual threads</b> do Java 21 (opcional); "
               "<font name='Consolas'>solicitacoes</font> define S (padrão 1.000).",
               "A resposta traz o relatório por tipo sanguíneo, o modo, o número de threads e o tempo de "
               "processamento medido no servidor."),
        p("2.1 Paralelização", h2),
        p("Cada tarefa recebe um intervalo contíguo de solicitações e devolve um objeto <font name='Consolas'>"
          "Parcial</font> próprio (contadores por tipo sanguíneo). A agregação acontece depois que todas as "
          "tarefas terminam:"),
        Preformatted(trecho, codigo),
        p("2.2 Mesma resposta nas duas versões", h2),
        p("O teste <font name='Consolas'>CoberturaEstoqueServiceTest</font> executa o cálculo sequencial e as "
          "versões com 2, 3, 4 e 8 threads (de plataforma e virtuais) sobre a mesma massa e exige resultados "
          "<b>idênticos</b>. O script de medição repete essa verificação em todas as chamadas ao endpoint: "
          "nenhuma divergência foi encontrada, o que confirma a ausência de <i>race condition</i>. Os 15 "
          "testes do projeto passam."),
        p("2.3 Organização do código", h2),
        tabela(arquivos, [7.2 * cm, 9.8 * cm]),
        PageBreak(),
    ]


def medicoes():
    resumo = ler_resumo("resumo.csv")
    sem_afinidade = {(r["bolsas"], r["modo"], r["threads"]): r for r in ler_resumo("resumo-sem-afinidade.csv")}
    nomes = {"SEQUENCIAL": "Sequencial", "PLATAFORMA": "Plataforma", "VIRTUAL": "Virtual"}

    linhas = [["Bolsas", "Versão", "Threads", "Tempo de resposta (ms)", "Speedup", "Eficiência"]]
    for r in resumo:
        threads = int(r["threads"])
        linhas.append([f"{int(r['bolsas']):,}".replace(",", "."), nomes[r["modo"]], threads,
                       ms(r["mediana_resposta_ms"]), x(r["speedup"]),
                       f"{float(r['speedup']) / threads * 100:.0f}%"])

    seq_livre = sem_afinidade[("100000", "SEQUENCIAL", "1")]["mediana_resposta_ms"]
    seq_fixo = next(r for r in resumo if r["bolsas"] == "100000" and r["modo"] == "SEQUENCIAL")
    return [
        p("3. Medições", h1),
        p("<b>Ambiente:</b> notebook com Intel Core i5-12450HX — processador híbrido com 4 núcleos de "
          "desempenho (P, com hyper-threading) e 4 de eficiência (E), 12 threads lógicas, cache L3 de 12 MB — "
          "16 GB de RAM, Windows 11, JDK 21 (Temurin), heap de 4 GB."),
        p("<b>Método:</b> para cada tamanho, uma rodada de aquecimento (gera a massa de dados e deixa o JIT "
          "compilar todos os caminhos) seguida de 5 rodadas <b>intercaladas</b> — cada rodada passa por todas "
          "as configurações, para que variações da máquina afetem todas igualmente. Registramos a mediana do "
          "tempo de resposta HTTP medido pelo cliente. S = 1.000 solicitações em todos os casos."),
        p(f"<b>Afinidade de CPU:</b> na primeira bateria, sem controle, o Windows colocava a thread "
          f"sequencial sozinha nos núcleos E: {ms(seq_livre)} ms, contra {ms(seq_fixo['mediana_resposta_ms'])} "
          "ms num núcleo P — o que inflava artificialmente o speedup. Por isso a medição final restringe o "
          "processo Java aos 4 núcleos P (8 threads lógicas), garantindo uma base de comparação justa."),
        tabela(linhas, [2.4 * cm, 2.6 * cm, 1.8 * cm, 4.2 * cm, 2.4 * cm, 2.4 * cm]),
        p("Speedup = tempo sequencial ÷ tempo paralelo. Eficiência = speedup ÷ número de threads.", legenda),
        Spacer(1, 4),
        KeepTogether([
            Image(str(PASTA / "grafico.png"), width=17 * cm, height=17 * cm * 675 / 1800),
            p("Tempo de resposta (escala logarítmica) e speedup por número de threads. A linha pontilhada é o "
              "speedup ideal (linear).", legenda),
        ]),
        PageBreak(),
    ]


def analise():
    return [
        p("4. Análise", h1),
        p("<b>O ganho foi linear?</b> Não, e o motivo muda com o tamanho. Com 100 mil bolsas (~7 MB, cabem no "
          "cache L3) o speedup seguiu o ideal até 4 threads (3,92x), mas ficou em 5,55x com 8: são só 4 núcleos "
          "físicos, o hyper-threading divide as mesmas unidades de execução, e há custos fixos (criar threads, "
          "agregar, HTTP). Com 1 milhão (~70 MB) foi <i>superlinear</i> (9,2x): o sequencial fica esperando a "
          "RAM (10× mais dados custaram 15,5× mais tempo), e as threads, varrendo o mesmo estoque juntas, "
          "reaproveitam no L3 compartilhado o que as outras trouxeram."),
        p("<b>A Big-O mudou?</b> Não: o trabalho segue O(S × B); só o tempo cai para ~O(S × B / <i>p</i>)."),
        p("<b>Concorrência × paralelismo.</b> Na Mesa DJ havia <b>concorrência</b>: vários eventos "
          "independentes intercalados, o que funciona até num só núcleo. Aqui é <b>paralelismo</b>: uma tarefa "
          "dividida em fatias que rodam ao mesmo tempo em núcleos diferentes. Por isso as virtual threads "
          "empataram com as de plataforma (9,23x × 9,21x): elas ajudam quem espera I/O, não quem calcula."),
        p("<b>E quando nem 8 threads bastarem?</b> (1) Gerar o relatório de forma assíncrona quando o estoque "
          "muda e servi-lo do cache; (2) escalar horizontalmente com várias instâncias da API atrás de um "
          "balanceador; (3) distribuir o cálculo: as fatias viram mensagens numa fila (Kafka/RabbitMQ), "
          "processadas por <i>workers</i> em várias máquinas, com agregação no final. É a mesma ideia desta "
          "atividade, particionar e agregar, levada de threads num processo para muitas máquinas."),
    ]


def main():
    doc = SimpleDocTemplate(str(SAIDA), pagesize=A4, leftMargin=2 * cm, rightMargin=2 * cm, topMargin=2 * cm,
                            bottomMargin=2 * cm, title="Rota Vital — Processamento paralelo",
                            author="Equipe Rota Vital — CESAR School")
    doc.build(capa() + justificativa() + servico() + medicoes() + analise(), onFirstPage=rodape,
              onLaterPages=rodape)
    print(SAIDA)


if __name__ == "__main__":
    main()
