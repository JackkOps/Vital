package com.jackops.rotavital.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Aresta de mão dupla da rede de distribuição.
 */
@Entity
@Table(name = "trecho")
public class Trecho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "origem_id", nullable = false)
    private LocalRede origem;

    @ManyToOne(optional = false)
    @JoinColumn(name = "destino_id", nullable = false)
    private LocalRede destino;

    @Column(nullable = false)
    private Integer tempoMinutos;

    @Column(nullable = false)
    private Double distanciaKm;

    public Trecho() {
    }

    public Trecho(LocalRede origem, LocalRede destino, Integer tempoMinutos, Double distanciaKm) {
        this.origem = origem;
        this.destino = destino;
        this.tempoMinutos = tempoMinutos;
        this.distanciaKm = distanciaKm;
    }

    public Long getId() {
        return id;
    }

    public LocalRede getOrigem() {
        return origem;
    }

    public LocalRede getDestino() {
        return destino;
    }

    public Integer getTempoMinutos() {
        return tempoMinutos;
    }

    public Double getDistanciaKm() {
        return distanciaKm;
    }
}
