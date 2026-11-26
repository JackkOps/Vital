# ADR 0002: Comunicação entre contextos por eventos de domínio

Status: proposto · Data: 26/11/2026 · Squad JackkOps

## Contexto

Vários fluxos atravessam contextos: uma requisição atendida precisa de rota (Logística), uma remessa despachada precisa de monitoramento (Cadeia Fria), uma excursão térmica afeta Estoque e Atendimento, e o painel (Indicadores) lê dados de todos. Se cada contexto chamar os outros por REST, eles ficam acoplados no tempo: a falha de um interrompe o outro. A telemetria chega em rajadas de rede móvel, com 2,9 milhões de leituras por mês no cenário moderado e 43,2 milhões no alto.

## Decisão

Os contextos se comunicam por **eventos de domínio** com nome no passado (`RequisicaoAtendida`, `RemessaDespachada`, `ExcursaoTermicaDetectada`). No monolito modular, os eventos são publicados em processo (`ApplicationEventPublisher`) depois do commit, gravados antes numa tabela de saída (padrão outbox). No cenário alto, a mesma tabela é publicada em tópicos SNS com uma fila SQS por consumidor. Chamada síncrona REST só onde a resposta é imediata: a reserva de bolsa do Atendimento no Estoque. A telemetria entra por MQTT no AWS IoT Core e vai para uma fila SQS.

## Consequências

- **Melhora:** os contextos ficam desacoplados no tempo; a fila absorve picos de telemetria; a saga de atendimento tem compensações explícitas; mudar para microsserviços troca só o transporte dos eventos.
- **Piora:** a consistência entre contextos passa a ser eventual (o painel pode atrasar alguns segundos); os consumidores precisam ser idempotentes, usando o id do evento, porque SQS e MQTT QoS 1 entregam pelo menos uma vez; rastrear um fluxo exige id de correlação nos logs.
- **Alternativa descartada:** REST síncrono entre todos os contextos. É mais simples de depurar, mas encadeia falhas e latências e obriga a Cadeia Fria a chamar a API a cada leitura.
