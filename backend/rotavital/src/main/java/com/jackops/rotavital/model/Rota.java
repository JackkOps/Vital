package com.jackops.rotavital.model;

import java.time.LocalDateTime;

import com.jackops.rotavital.model.enums.StatusRota;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Rota calculada para entregar hemocomponentes a um ou mais hospitais.
 */
@Entity
@Table(name = "rota")
public class Rota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "origem_id", nullable = false)
    private LocalRede origem;

    /** Ids dos locais percorridos, separados por vírgula. */
    @Column(nullable = false, length = 2000)
    private String caminho;

    /** "localId@minutoDeChegada" de cada hospital, na ordem de visita, separados por vírgula. */
    @Column(nullable = false, length = 2000)
    private String entregas;

    @Column(nullable = false)
    private Integer tempoTotalMinutos;

    @Column(nullable = false)
    private Double distanciaTotalKm;

    @Column(nullable = false)
    private Integer janelaMinutos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRota status;

    @Column(nullable = false)
    private LocalDateTime criadaEm;

    public Rota() {
    }

    public Rota(LocalRede origem, String caminho, String entregas, Integer tempoTotalMinutos,
            Double distanciaTotalKm, Integer janelaMinutos, StatusRota status) {
        this.origem = origem;
        this.caminho = caminho;
        this.entregas = entregas;
        this.tempoTotalMinutos = tempoTotalMinutos;
        this.distanciaTotalKm = distanciaTotalKm;
        this.janelaMinutos = janelaMinutos;
        this.status = status;
        this.criadaEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public LocalRede getOrigem() {
        return origem;
    }

    public String getCaminho() {
        return caminho;
    }

    public String getEntregas() {
        return entregas;
    }

    public Integer getTempoTotalMinutos() {
        return tempoTotalMinutos;
    }

    public Double getDistanciaTotalKm() {
        return distanciaTotalKm;
    }

    public Integer getJanelaMinutos() {
        return janelaMinutos;
    }

    public StatusRota getStatus() {
        return status;
    }

    public void setStatus(StatusRota status) {
        this.status = status;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }
}
