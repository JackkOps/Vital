# language: pt
# História âncora da Parte 2 (contexto Atendimento, agregado Requisicao).
# Invariantes cobertas: I2 (não passa da quantidade pedida) e I3 (só bolsa compatível),
# com a regra FEFO do SeletorFefo. Será automatizado na Parte 3.
Funcionalidade: Alocação de bolsas a uma requisição hospitalar
  Como Marina, operadora do banco de sangue do HEMOPE
  Quero alocar a uma requisição só bolsas compatíveis, na quantidade pedida e vencendo primeiro
  Para atender o hospital sem enviar sangue incompatível nem desperdiçar bolsas

  Cenário: A requisição recebe as bolsas compatíveis que vencem primeiro e recusa uma alocação a mais
    Dado uma requisição do "Hospital da Restauracao" de 2 bolsas de "HEMACIAS" "O-" com urgência "EMERGENCIA"
    E o estoque tem estas bolsas disponíveis de "HEMACIAS":
      | identificador | tipo sanguíneo | dias para vencer |
      | B-001         | O-             | 10               |
      | B-002         | O-             | 4                |
      | B-003         | O+             | 2                |
      | B-004         | O-             | 6                |
    Quando a operadora aloca bolsas à requisição 3 vezes
    Então as bolsas alocadas são "B-002" e "B-004", nessa ordem
    E a requisição fica com status "ATENDIDA"
    E a terceira alocação é recusada com a mensagem "Solicitação já recebeu a quantidade de bolsas pedida"
    E as bolsas "B-001" e "B-003" continuam com status "DISPONIVEL"
