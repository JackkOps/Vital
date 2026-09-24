# Atividade — Processamento paralelo no Rota Vital

Operação escolhida: **relatório de cobertura nacional** — cruza cada solicitação de sangue com todo o
estoque de bolsas e conta quantas bolsas disponíveis, dentro da validade, do mesmo componente,
compatíveis (ABO/Rh) e armazenadas a até 150 km do hospital poderiam atendê-la. Complexidade sequencial: **O(S × B)**
(S = solicitações, B = bolsas). Com S = 1.000 e B = 1.000.000 são 10⁹ verificações.

## Onde está o código

| Arquivo | Papel |
|---|---|
| `backend/rotavital/.../controller/RelatorioCoberturaController.java` | Endpoint `GET /api/relatorios/cobertura` |
| `backend/rotavital/.../service/CoberturaEstoqueService.java` | Versões sequencial, `ExecutorService` (plataforma) e virtual threads |
| `backend/rotavital/.../service/GeradorDadosService.java` | Gera a massa de dados em memória (semente fixa, cacheada) |
| `backend/rotavital/.../model/relatorio/` | Projeções de leitura (bolsa e demanda) com a localização |
| `backend/rotavital/.../service/CoberturaEstoqueServiceTest.java` | Garante que todas as versões devolvem exatamente o mesmo resultado |
| `docs/atividade-threads/medir.py` | Mede o endpoint, gera `resultados.csv`, `resumo.csv` e `grafico.png` |
| `docs/atividade-threads/gerar_pdf.py` | Gera o PDF de entrega (`Atividade-Threads-RotaVital.pdf`) |

Cada thread recebe uma fatia contígua das solicitações, lê o estoque (somente leitura) e acumula em
uma parcial própria; no final as parciais são somadas. Não há estado compartilhado mutável.

## Como executar

```bash
cd backend/rotavital
./mvnw test                          # inclui o teste de igualdade sequencial x paralelo
./mvnw spring-boot:run               # sobe a API em http://localhost:8080

# exemplo de chamada
curl "http://localhost:8080/api/relatorios/cobertura?bolsas=1000000&solicitacoes=1000&threads=8"
curl "http://localhost:8080/api/relatorios/cobertura?bolsas=1000000&threads=8&virtual=true"

# medição completa (outro terminal; requer: pip install matplotlib reportlab)
# em CPU híbrida (Intel 12ª geração+), prenda a JVM aos núcleos P antes de medir (PowerShell):
#   (Get-Process java).ProcessorAffinity = 0xFF
python docs/atividade-threads/medir.py
python docs/atividade-threads/gerar_pdf.py
```

Parâmetros: `bolsas` (1–2.000.000), `solicitacoes` (1–100.000), `threads` (1–64; `1` = sequencial),
`virtual` (`true` usa virtual threads do Java 21).

## Resultados

Máquina: Intel Core i5-12450HX (4 núcleos P com HT + 4 núcleos E, 12 threads lógicas, L3 de 12 MB),
16 GB RAM, Windows 11, JDK 21. JVM presa aos 4 núcleos P (8 threads lógicas). Mediana de 5 execuções
intercaladas por configuração, 1.000 solicitações, tempo de resposta HTTP medido pelo cliente.

| Bolsas | Versão | Threads | Tempo (ms) | Speedup |
|---:|---|---:|---:|---:|
| 100.000 | Sequencial | 1 | 1.572 | 1,00 |
| 100.000 | Plataforma | 2 | 681 | 2,31 |
| 100.000 | Plataforma | 4 | 401 | 3,92 |
| 100.000 | Plataforma | 8 | 283 | 5,55 |
| 100.000 | Virtual | 2 | 725 | 2,17 |
| 100.000 | Virtual | 4 | 405 | 3,88 |
| 100.000 | Virtual | 8 | 252 | 6,23 |
| 1.000.000 | Sequencial | 1 | 24.421 | 1,00 |
| 1.000.000 | Plataforma | 2 | 7.985 | 3,06 |
| 1.000.000 | Plataforma | 4 | 4.550 | 5,37 |
| 1.000.000 | Plataforma | 8 | 2.652 | 9,21 |
| 1.000.000 | Virtual | 2 | 8.233 | 2,97 |
| 1.000.000 | Virtual | 4 | 4.574 | 5,34 |
| 1.000.000 | Virtual | 8 | 2.646 | 9,23 |

![Gráfico](grafico.png)

Sem fixar os núcleos, o Windows colocava a thread sequencial sozinha num núcleo de eficiência
(~2,3× mais lenta), inflando o speedup — esses dados estão em `resumo-sem-afinidade.csv` /
`resultados-sem-afinidade.csv`. A justificativa e a análise completas estão no PDF.
