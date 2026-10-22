# language: pt
Funcionalidade: Cadastro de bolsa de hemocomponente com validações e unicidade
  Como operador do banco de sangue
  Quero cadastrar novas bolsas no estoque com dados validados
  Para que o estoque contenha apenas informações consistentes e sem duplicidades

  Contexto:
    Dado que o sistema de hemocentro está ativo e pronto para receber cadastros

  Cenário: Cadastro de bolsa com dados válidos realizado com sucesso
    Dado que informo os dados da bolsa:
      | codigoIdentificador | BOLSA-2026-001 |
      | tipoSanguineo       | O              |
      | fatorRh             | POSITIVO       |
      | tipoHemocomponente  | SANGUE_TOTAL   |
      | volumeMl            | 450            |
      | dataColeta          | 2026-10-20     |
      | dataValidade        | 2026-11-24     |
    Quando submeto o cadastro da bolsa
    Então a bolsa é persistida com status "DISPONIVEL"
    E o sistema retorna código HTTP 201 Created

  Cenário: Bloqueio de cadastro de bolsa com código identificador duplicado
    Dado que já existe uma bolsa cadastrada com o código "BOLSA-2026-001"
    Quando tento cadastrar uma nova bolsa com o mesmo código "BOLSA-2026-001"
    Então o sistema deve rejeitar o cadastro com código HTTP 409 Conflict
    E a mensagem de erro deve indicar duplicidade de identificador

  Cenário: Rejeição de cadastro com data de validade anterior à data de coleta
    Dado que informo uma bolsa com data de coleta "2026-10-20" e validade "2026-10-15"
    Quando submeto o cadastro da bolsa
    Então o sistema deve rejeitar a requisição com código HTTP 400 Bad Request
    E deve retornar mensagem informando que a validade não pode ser anterior à coleta

  Cenário: Rejeição de cadastro com volume não positivo
    Dado que informo uma bolsa com volumeMl menor ou igual a zero
    Quando submeto o cadastro da bolsa
    Então o sistema deve rejeitar a requisição com código HTTP 400 Bad Request
    E deve apontar violação de validação no campo volumeMl
