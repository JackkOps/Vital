-- Vínculo persistido entre a bolsa e a solicitação que a recebeu (PR #22).
-- A coluna é opcional: fica nula enquanto a bolsa não foi alocada.

ALTER TABLE bolsa ADD COLUMN solicitacao_id BIGINT;

ALTER TABLE bolsa
    ADD CONSTRAINT fk_bolsa_solicitacao
    FOREIGN KEY (solicitacao_id) REFERENCES solicitacao_sangue (id);

CREATE INDEX idx_bolsa_solicitacao_id ON bolsa (solicitacao_id);
