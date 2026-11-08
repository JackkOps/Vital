# language: pt
Funcionalidade: Monitoramento da cadeia fria durante o transporte
  Como operador de logística
  Quero acompanhar a temperatura das remessas em trânsito
  Para agir antes da perda das bolsas quando a carga sai da faixa segura

  Cenário: Transporte dentro da faixa segura
    Dado que existe uma remessa de "HEMACIAS" em trânsito com sensor ativo
    Quando o sistema recebe uma leitura de 4,5 graus
    Então a leitura é registrada no histórico da remessa
    E a remessa permanece com condição "CONFORME"

  Cenário: Excursão térmica detectada
    Dado que existe uma remessa de "HEMACIAS" em trânsito com sensor ativo
    Quando o sistema recebe uma leitura de 12,0 graus
    Então a remessa permanece com condição "SOB_RISCO"
    E o operador recebe um alerta "EXCURSAO_TERMICA"

  Cenário: Perda de comunicação com o sensor
    Dado que existe uma remessa de "HEMACIAS" em trânsito sem leitura há 15 minutos
    Quando o sistema verifica os sensores
    Então o sistema registra falha de comunicação
    E o operador recebe um alerta "SENSOR_INDISPONIVEL"
