# E2 — Execução e evidências

Checkout: `C:\Users\migue\Vital`, branch `main`. Execuções locais em 22/10/2026,
com Java 25.0.4, Maven Wrapper e compilação configurada para Java 21 no POM.

## Ciclo observado

| Estado | Commit | Resultado de `mvnw.cmd test -q` |
| --- | --- | --- |
| Base | `e6d9ec4` | 42 testes, nenhuma falha |
| RED | `6552fcb` — `test: evidencia RED da US-01 e automatiza BDD das tres historias` | 61 testes, 4 falhas, 0 erros, 0 ignorados; saída 1 |
| GREEN | `dc2b990` — `feat: aceita sangue total e retorna 400 para validade anterior a coleta` | 61 testes, 0 falhas, 0 erros, 0 ignorados; saída 0 |

As quatro falhas RED foram:

- BDD: cadastro com `SANGUE_TOTAL` não retornava bolsa com status disponível,
  pois o enum não aceitava esse componente.
- BDD: validade anterior à coleta esperava HTTP 400, mas recebia 409.
- `BolsaControllerTest.deveBloquearValidadeAnteriorAColeta`: esperava 400, recebia 409.
- `BolsaValidationTest.validadeAnteriorDeveSerBadRequestSemPersistenciaOuHistorico`:
  esperava `ResponseStatusException` com status 400, recebia `RegraDeNegocioException`.

O commit RED contém somente testes, configuração de testes e os três arquivos
Gherkin de autoria do Antigravity. O GREEN altera somente o enum de componentes
e o tratamento de datas inválidas no cadastro/atualização de bolsas.

FIFO, LIFO, validações obrigatórias e unicidade já existiam. Seus testes novos
constituem cobertura de regressão; não demonstram desenvolvimento original por TDD.

## Reproduzir

Em um checkout independente do commit desejado:

```powershell
cd backend/rotavital
.\mvnw.cmd clean test
```

Para executar somente os dez cenários BDD:

```powershell
.\mvnw.cmd "-Dtest=RunCucumberTest" test
```

Para unitários novos e integração de unicidade:

```powershell
.\mvnw.cmd "-Dtest=BolsaValidationTest,HistoricoServiceTest,BolsaRepositoryIntegrationTest" test
```

O Maven copia diretamente `../../docs/bdd/*.feature` para os recursos de teste;
não há cópias manuais de cenários para manter sincronizadas. O Cucumber executa
os cenários por JUnit Platform e usa MockMvc com contexto Spring completo,
services e repositórios reais. Cada cenário limpa banco e estruturas em memória.
A execução BDD é sequencial porque as estruturas são componentes compartilhados.

Relatórios gerados em `target/` (não versionados):

- `surefire-reports/TEST-*.xml` e `surefire-reports/*.txt`: resultados JUnit e BDD.
- `cucumber.html` e `cucumber.json`: relatório dos dez cenários.
- `e2-red-us01.log` e `e2-green-us01.log`: saídas das execuções locais iniciais,
  preservadas neste checkout até uma limpeza de `target/`.

## Rastreabilidade

| História | Unitário | Integração/aceitação |
| --- | --- | --- |
| US01 | `BolsaValidationTest`: campos obrigatórios, volume nulo/zero/negativo, datas inválidas sem gravação ou histórico | `BolsaRepositoryIntegrationTest`: restrição única no banco sem passar pelo service; `BolsaControllerTest`; 4 cenários BDD |
| US02 | `FilaRequisicoesTest`: ordem FIFO da estrutura existente | `SolicitacaoSangueControllerTest`; 3 cenários BDD, incluindo status persistido `EM_ATENDIMENTO`, rejeição de quantidade zero sem inserir e fila vazia |
| US03 | `PilhaHistoricoTest` e `HistoricoServiceTest`: LIFO, consulta preservando conteúdo e pop | `HistoricoControllerTest`; 3 cenários BDD, incluindo cadastro/atualização reais pela API e consulta do histórico resultante |

## Vocabulário dos cenários e contrato existente

| Gherkin | API |
| --- | --- |
| `codigoIdentificador` | `identificador` |
| `tipoSanguineo = O`, `fatorRh = POSITIVO` | `tipoSanguineo = O_POSITIVO` |
| `O+`, `A-` | `O_POSITIVO`, `A_NEGATIVO` |
| `tipoHemocomponente` | `tipoComponente` |
| `volumeMl` | `volume` |
| `quantidadeSolicitada` | `quantidade` |
| hospital | `nomeHospital`; o cenário usa `HEMACIAS` e urgência `ELETIVA` |

Os rótulos `CRIACAO_BOLSA_01` e `ATUALIZACAO_BOLSA_01` correspondem às descrições
`Cadastro de bolsa 01` e `Atualização de bolsa 01`. `CADASTRO_SOLICITACAO_10`
representa o cadastro de uma solicitação; a execução usa o ID real gerado pelo
banco, sem exigir uma sequência específica de IDs entre cenários.

## Limites da evidência

O banco dos testes de integração é H2. Esta execução não demonstra compatibilidade
com PostgreSQL, Docker ou deploy, que pertencem às verificações E1/E3.
Os cenários exercitam HTTP via MockMvc, sem servidor em porta de rede.

O histórico é uma pilha em memória. `DELETE /api/historico/ultima` remove uma
descrição da pilha, sem desfazer a alteração nos dados de negócio e sem persistir
uma auditoria adicional. O BDD verifica esse pop e a preservação do cadastro.
