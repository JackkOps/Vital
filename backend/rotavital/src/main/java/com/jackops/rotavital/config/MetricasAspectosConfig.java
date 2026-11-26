package com.jackops.rotavital.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;

/**
 * Habilita as anotações {@code @Timed} e {@code @Counted} do Micrometer.
 *
 * <h2>Por que aspectos, e não injetar MeterRegistry nos services</h2>
 * Injetar o registry exigiria um parâmetro novo no construtor de cada service
 * instrumentado. Os testes unitários existentes instanciam esses services
 * diretamente ({@code new AlocacaoBolsaService(...)}), então a mudança de
 * assinatura quebraria a suíte inteira por um motivo que não tem relação com
 * regra de negócio.
 *
 * <p>Com os aspectos, instrumentar passa a ser uma anotação no método: o código de
 * negócio não conhece o Micrometer e os construtores permanecem intactos.
 *
 * <p>Limitação a conhecer: aspectos funcionam por proxy, logo só valem em chamadas
 * que atravessam o proxy do Spring (ex.: controller -> service). Uma chamada de um
 * método para outro da mesma classe não é interceptada.
 */
@Configuration
public class MetricasAspectosConfig {

    /** Habilita {@code @Timed}: mede duração, contagem e (opcionalmente) percentis. */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /** Habilita {@code @Counted}: conta execuções, separando sucesso de falha. */
    @Bean
    public CountedAspect countedAspect(MeterRegistry registry) {
        return new CountedAspect(registry);
    }

    /**
     * Habilita {@code @Observed}, que gera **métrica e span ao mesmo tempo**.
     *
     * <p>É o que cria a camada intermediária do trace: sem ela o trace teria
     * apenas o span HTTP do Tomcat e o span da consulta JDBC, sem nada no meio
     * representando a regra de negócio. Com ela a árvore fica
     * {@code http -> service -> jdbc}, atendendo ao requisito de múltiplos spans.
     */
    @Bean
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }
}
