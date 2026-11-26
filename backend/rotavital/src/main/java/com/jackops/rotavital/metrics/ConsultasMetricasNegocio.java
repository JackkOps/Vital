package com.jackops.rotavital.metrics;

import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;

/**
 * Consultas agregadas usadas somente pelas métricas de negócio.
 *
 * <p>Existe como classe separada, e não como métodos novos nos repositórios de
 * domínio, por dois motivos: deixa explícito que estas consultas servem à
 * observabilidade (e não a regras de negócio) e evita alterar interfaces que os
 * testes existentes já utilizam.
 *
 * <p>Cada consulta devolve contagens agrupadas — nunca a lista de entidades.
 * Trazer as entidades para contá-las em memória custaria uma carga de registros
 * a cada coleta; o banco faz a agregação em uma única passada.
 */
@Repository
public class ConsultasMetricasNegocio {

    private final EntityManager em;

    public ConsultasMetricasNegocio(EntityManager em) {
        this.em = em;
    }

    /** Linhas no formato [StatusBolsa, TipoComponente, long]. */
    public List<Object[]> contarBolsasPorStatusEComponente() {
        return em.createQuery(
                "select b.status, b.tipoComponente, count(b) "
                        + "from Bolsa b group by b.status, b.tipoComponente",
                Object[].class)
                .getResultList();
    }

    /** Linhas no formato [StatusSolicitacao, NivelUrgencia, long]. */
    public List<Object[]> contarSolicitacoesPorStatusEUrgencia() {
        return em.createQuery(
                "select s.status, s.nivelUrgencia, count(s) "
                        + "from SolicitacaoSangue s group by s.status, s.nivelUrgencia",
                Object[].class)
                .getResultList();
    }

    /** Linhas no formato [StatusRemessa, CondicaoRemessa, long]. */
    public List<Object[]> contarRemessasPorStatusECondicao() {
        return em.createQuery(
                "select r.status, r.condicao, count(r) "
                        + "from Remessa r group by r.status, r.condicao",
                Object[].class)
                .getResultList();
    }
}
