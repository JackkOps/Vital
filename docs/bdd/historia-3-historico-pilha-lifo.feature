# language: pt
Funcionalidade: Histórico de operações e auditoria em estrutura LIFO
  Como auditor ou operador do hemocentro
  Quero manter uma pilha cronológica reversa das operações e poder desfazê-las
  Para auditar intervenções recentes e reverter ações indevidas com segurança

  Contexto:
    Dado que o módulo de auditoria e histórico de operações está ativo

  Cenário: Registro de operações e consulta em ordem cronológica reversa (LIFO)
    Dado que foram executadas em sequência as ações "CRIACAO_BOLSA_01" e "ATUALIZACAO_BOLSA_01"
    Quando consulto o histórico de operações
    Então a primeira operação listada deve ser "ATUALIZACAO_BOLSA_01" (topo da pilha)
    E a operação seguinte deve ser "CRIACAO_BOLSA_01"

  Cenário: Desfazer a última operação (pop da pilha)
    Dado que a última operação registrada na pilha foi "CADASTRO_SOLICITACAO_10"
    Quando solicito a remoção ou reversão da última operação
    Então a operação "CADASTRO_SOLICITACAO_10" é desempilhada
    E a operação anterior passa a ocupar o topo da pilha

  Cenário: Tentativa de remoção em histórico vazio
    Dado que o histórico de operações está vazio
    Quando tento remover a última operação
    Então o sistema deve retornar status informando que não há operações a serem revertidas
