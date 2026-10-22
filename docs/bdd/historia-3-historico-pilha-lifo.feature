# language: pt
Funcionalidade: Histórico de operações e auditoria em estrutura LIFO
  Como auditor ou operador do hemocentro
  Quero manter uma pilha cronológica reversa das operações e poder desempilhar o último registro
  Para auditar intervenções recentes e remover registros de auditoria incorretos da pilha

  Contexto:
    Dado que o módulo de auditoria e histórico de operações está ativo

  Cenário: Registro de operações e consulta em ordem cronológica reversa (LIFO)
    Dado que foram executadas em sequência as ações "CRIACAO_BOLSA_01" e "ATUALIZACAO_BOLSA_01"
    Quando consulto o histórico de operações
    Então a primeira operação listada deve ser "ATUALIZACAO_BOLSA_01" (topo da pilha)
    E a operação seguinte deve ser "CRIACAO_BOLSA_01"

  Cenário: Desempilhar a última operação de auditoria (pop da pilha)
    Dado que a última operação registrada na pilha foi "CADASTRO_SOLICITACAO_10"
    Quando solicito a remoção do último registro do histórico
    Então a operação "CADASTRO_SOLICITACAO_10" é desempilhada
    E a operação anterior passa a ocupar o topo da pilha
    E o cadastro da solicitação permanece no banco de dados

  Cenário: Tentativa de remoção em histórico vazio
    Dado que o histórico de operações está vazio
    Quando tento remover a última operação
    Então o sistema deve retornar status informando que não há registros a serem removidos
