package com.jackops.rotavital.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Leitura do sensor de temperatura de uma remessa, guardada para auditoria.
 */
@Entity
@Table(name = "leitura_temperatura")
public class LeituraTemperatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "remessa_id", nullable = false)
    private Remessa remessa;

    /** Identificador único gerado pelo sensor: um reenvio repete o mesmo valor. */
    @Column(nullable = false, unique = true)
    private String leituraId;

    @Column(nullable = false)
    private String sensorId;

    @Column(nullable = false)
    private Long sequencia;

    @Column(nullable = false)
    private Double temperatura;

    /** Momento da medição, informado pelo sensor. */
    @Column(nullable = false)
    private LocalDateTime dataHora;

    /** Momento em que a plataforma recebeu a leitura. */
    @Column(nullable = false)
    private LocalDateTime recebidaEm;

    @Column(nullable = false)
    private boolean dentroDaFaixa;

    public LeituraTemperatura() {
    }

    public LeituraTemperatura(Remessa remessa, String leituraId, String sensorId, Long sequencia,
            Double temperatura, LocalDateTime dataHora, LocalDateTime recebidaEm, boolean dentroDaFaixa) {
        this.remessa = remessa;
        this.leituraId = leituraId;
        this.sensorId = sensorId;
        this.sequencia = sequencia;
        this.temperatura = temperatura;
        this.dataHora = dataHora;
        this.recebidaEm = recebidaEm;
        this.dentroDaFaixa = dentroDaFaixa;
    }

    public Long getId() {
        return id;
    }

    public Remessa getRemessa() {
        return remessa;
    }

    public String getLeituraId() {
        return leituraId;
    }

    public String getSensorId() {
        return sensorId;
    }

    public Long getSequencia() {
        return sequencia;
    }

    public Double getTemperatura() {
        return temperatura;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public LocalDateTime getRecebidaEm() {
        return recebidaEm;
    }

    public boolean isDentroDaFaixa() {
        return dentroDaFaixa;
    }
}
