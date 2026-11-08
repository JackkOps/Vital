-- Datas da solicitação para o painel de indicadores (história 7):
-- criada_em marca a entrada do pedido e atendida_em o momento em que ele recebeu
-- todas as bolsas. Ficam nulas nos pedidos antigos, que o tempo médio ignora.

ALTER TABLE solicitacao_sangue ADD COLUMN criada_em TIMESTAMP;
ALTER TABLE solicitacao_sangue ADD COLUMN atendida_em TIMESTAMP;
