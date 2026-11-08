# language: pt
Funcionalidade: Cálculo de rota de distribuição
  Como operador de logística
  Quero visualizar a rota mais eficiente para entregar hemocomponentes a um ou mais hospitais
  Para garantir a entrega dentro da janela de tempo permitida

  # Usa a rede sintética da migration V4, com saída do HEMOPE e janela padrão de 120 minutos.

  Cenário: Rota calculada com sucesso
    Dado que existem entregas pendentes para "Hospital Jayme da Fonte" e "Hospital Getulio Vargas"
    Quando o sistema calcula a rota a partir do "HEMOPE"
    Então o caminho de menor custo é retornado em 30 minutos
    E a rota cumpre a janela de tempo exigida
    E a rota é confirmada automaticamente

  Cenário: Nenhum caminho viável
    Dado que existem entregas pendentes para "Hospital Sao Lucas (Fernando de Noronha)"
    Quando o sistema calcula a rota a partir do "HEMOPE"
    Então o sistema alerta que não há rota disponível

  Cenário: Janela de tempo não cumprida
    Dado que existem entregas pendentes para "Hospital Regional de Arcoverde"
    Quando o sistema calcula a rota a partir do "HEMOPE"
    Então o operador é alertado sobre o risco de atraso
    E a rota não é confirmada automaticamente
