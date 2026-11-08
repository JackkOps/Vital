# language: pt
Funcionalidade: Priorização das solicitações hospitalares por urgência
  Como responsável pelo estoque de um hospital
  Quero que pedidos de emergência sejam atendidos antes dos eletivos
  Para que o hemocentro priorize quem corre mais risco

  Contexto:
    Dado que a fila de solicitações hospitalares está inicializada

  Cenário: Emergência passa à frente das eletivas
    Dado que existem as solicitações eletivas pendentes dos hospitais "Hospital A" e "Hospital B"
    Quando o hospital "Hospital C" registra uma solicitação com urgência "EMERGENCIA"
    Então a fila de atendimento fica na ordem "Hospital C", "Hospital A", "Hospital B"

  Cenário: Quantidade acima do limite por solicitação
    Dado que um hospital envia solicitação com 11 bolsas
    Quando submete a solicitação
    Então o sistema deve recusar a solicitação com código HTTP 400 Bad Request
    E nenhuma requisição deve ser adicionada à fila

  Cenário: Cancelamento devolve as bolsas ao estoque
    Dado que existe uma solicitação em atendimento com uma bolsa alocada
    Quando o hospital cancela a solicitação
    Então a solicitação passa a ter status "CANCELADA"
    E a bolsa alocada volta a ter status "DISPONIVEL"
