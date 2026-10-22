# language: pt
Funcionalidade: Registro de solicitação hospitalar e atendimento FIFO
  Como hospital conveniado
  Quero registrar solicitações de hemocomponentes e consumi-las na ordem de chegada
  Para garantir equidade e atendimento estritamente cronológico

  Contexto:
    Dado que a fila de solicitações hospitalares está inicializada

  Cenário: Enfileiramento e consumo na ordem de chegada (FIFO)
    Dado que o hospital "Hospital da Restauracao" submete uma solicitação para "O+" com 2 unidades
    E em seguida o hospital "Hospital Jayme da Fonte" submete uma solicitação para "A-" com 1 unidade
    Quando o operador solicita a próxima solicitação a ser atendida
    Então a solicitação retornada deve ser a do "Hospital da Restauracao"
    E a solicitação do "Hospital Jayme da Fonte" deve passar a ser a primeira da fila

  Cenário: Rejeição de solicitação com quantidade solicitada inválida
    Dado que um hospital envia solicitação com quantidadeSolicitada menor ou igual a 0
    Quando submete a solicitação
    Então o sistema deve recusar a solicitação com código HTTP 400 Bad Request
    E nenhuma requisição deve ser adicionada à fila

  Cenário: Atendimento de fila vazia
    Dado que não existem solicitações pendentes na fila
    Quando o operador solicita a próxima solicitação
    Então o sistema deve informar que não há solicitações para processamento com HTTP 204 No Content ou 404
