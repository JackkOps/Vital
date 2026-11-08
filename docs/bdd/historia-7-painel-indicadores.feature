# language: pt
Funcionalidade: Painel de indicadores da rede de sangue
  Como gestor do hemocentro
  Quero visualizar indicadores de estoque, demanda e descarte
  Para apoiar decisões de reposição e distribuição

  Contexto:
    Dado que a fila de solicitações hospitalares está inicializada

  Cenário: Período sem dados suficientes
    Dado que não houve movimentação entre "2020-01-01" e "2020-01-31"
    Quando acesso o painel de indicadores do período de "2020-01-01" a "2020-01-31"
    Então o painel exibe a mensagem "Dados insuficientes para o período"
    E nenhum indicador é calculado

  Cenário: Alerta de estoque crítico
    Dado que há 2 bolsas de "HEMACIAS" disponíveis e uma solicitação em aberto de 3 bolsas
    Quando acesso o painel de indicadores
    Então o componente "HEMACIAS" aparece como estoque crítico
    E a probabilidade de desabastecimento de "HEMACIAS" é apresentada
