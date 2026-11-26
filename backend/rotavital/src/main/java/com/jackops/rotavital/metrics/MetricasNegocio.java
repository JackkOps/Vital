package com.jackops.rotavital.metrics;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.Tags;

/**
 * Métricas de NEGÓCIO do Rota Vital (requisito adicional da entrega).
 *
 * <p>Diferença em relação às métricas técnicas: {@code jvm_memory_used_bytes}
 * responde "a aplicação está saudável?"; estas respondem "o banco de sangue está
 * saudável?". São as que um gestor do hemocentro leria.
 *
 * <h2>Por que Gauge, e não Counter</h2>
 * Estoque sobe e desce — bolsas entram por doação e saem por alocação ou descarte.
 * Counter só cresce, então representaria "bolsas já cadastradas na história", o que
 * não responde "quanto tenho agora". A pergunta operacional é instantânea, logo Gauge.
 *
 * <h2>Por que MultiGauge</h2>
 * As contagens vêm de um {@code GROUP BY} cujas combinações mudam em tempo de
 * execução (um componente pode ficar sem nenhuma bolsa). {@link MultiGauge} substitui
 * o conjunto inteiro de séries a cada atualização, em vez de exigir um Gauge
 * registrado para cada combinação possível no boot.
 *
 * <h2>Cardinalidade</h2>
 * As tags usadas têm domínio pequeno e fechado: 4 status x 4 componentes = 16 séries;
 * 4 status x 3 urgências = 12. Usar identificador de bolsa como tag criaria uma série
 * nova por bolsa, para sempre, e degradaria o Prometheus. Nunca use id como tag.
 */
@Component
public class MetricasNegocio {

    private static final Logger log = LoggerFactory.getLogger(MetricasNegocio.class);

    private final ConsultasMetricasNegocio consultas;

    private final MultiGauge estoqueBolsas;
    private final MultiGauge solicitacoes;
    private final MultiGauge remessas;

    public MetricasNegocio(ConsultasMetricasNegocio consultas, MeterRegistry registry) {
        this.consultas = consultas;

        // Sem baseUnit: o Micrometer anexa a unidade ao final do nome exportado,
        // e "rotavital_bolsas_estoque" + "bolsas" produziria o redundante
        // "rotavital_bolsas_estoque_bolsas".
        this.estoqueBolsas = MultiGauge.builder("rotavital.bolsas.estoque")
                .description("Bolsas de sangue em estoque, por status e tipo de componente")
                .register(registry);

        this.solicitacoes = MultiGauge.builder("rotavital.solicitacoes.quantidade")
                .description("Solicitacoes de sangue, por status e nivel de urgencia")
                .register(registry);

        this.remessas = MultiGauge.builder("rotavital.remessas.quantidade")
                .description("Remessas em transporte, por status e condicao da cadeia fria")
                .register(registry);
    }

    /**
     * Relê as contagens do banco e republica as séries.
     *
     * <p>O intervalo (15s por padrão) acompanha o {@code scrape_interval} do
     * Prometheus: atualizar com muito mais frequência gastaria banco sem que o
     * Prometheus enxergasse a diferença; com muito menos, o painel mostraria
     * patamares defasados.
     *
     * <p>Qualquer falha é registrada e engolida de propósito: observabilidade não
     * pode derrubar a aplicação que observa.
     */
    @Scheduled(fixedDelayString = "${rotavital.metricas.intervalo-ms:15000}")
    @Transactional(readOnly = true)
    public void atualizar() {
        try {
            estoqueBolsas.register(linhas(consultas.contarBolsasPorStatusEComponente(),
                    "status", "componente"), true);
            solicitacoes.register(linhas(consultas.contarSolicitacoesPorStatusEUrgencia(),
                    "status", "urgencia"), true);
            remessas.register(linhas(consultas.contarRemessasPorStatusECondicao(),
                    "status", "condicao"), true);
        } catch (RuntimeException e) {
            log.warn("Falha ao atualizar metricas de negocio: {}", e.getMessage());
        }
    }

    /**
     * Converte linhas [tag1, tag2, contagem] em séries do MultiGauge.
     *
     * <p>Montado com laço e {@code ArrayList} declarado como
     * {@code List<MultiGauge.Row<?>>} de propósito: encadear
     * {@code .map(...).toList()} produz {@code List<Row<capture of ?>>}, que o
     * compilador não aceita onde {@code register} pede {@code Iterable<Row<?>>}.
     */
    private List<MultiGauge.Row<?>> linhas(List<Object[]> resultado,
                                           String nomeTag1, String nomeTag2) {
        List<MultiGauge.Row<?>> series = new ArrayList<>(resultado.size());
        for (Object[] linha : resultado) {
            Tags tags = Tags.of(nomeTag1, texto(linha[0]), nomeTag2, texto(linha[1]));
            series.add(MultiGauge.Row.of(tags, ((Number) linha[2]).doubleValue()));
        }
        return series;
    }

    private String texto(Object valor) {
        return valor == null ? "desconhecido" : valor.toString();
    }
}
