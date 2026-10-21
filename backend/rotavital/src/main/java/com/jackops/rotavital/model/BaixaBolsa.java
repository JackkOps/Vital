package com.jackops.rotavital.model;

import java.time.LocalDate;

import com.jackops.rotavital.model.enums.MotivoBaixa;

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
 * Registro da baixa de uma bolsa do estoque, com o motivo, para compor a taxa
 * de descarte.
 */
@Entity
@Table(name = "baixa_bolsa")
public class BaixaBolsa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bolsa_id", nullable = false)
    private Bolsa bolsa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MotivoBaixa motivo;

    @Column(nullable = false)
    private LocalDate dataBaixa;

    @Column(nullable = false)
    private boolean automatica;

    public BaixaBolsa() {
    }

    public BaixaBolsa(Bolsa bolsa, MotivoBaixa motivo, LocalDate dataBaixa, boolean automatica) {
        this.bolsa = bolsa;
        this.motivo = motivo;
        this.dataBaixa = dataBaixa;
        this.automatica = automatica;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Bolsa getBolsa() {
        return bolsa;
    }

    public void setBolsa(Bolsa bolsa) {
        this.bolsa = bolsa;
    }

    public MotivoBaixa getMotivo() {
        return motivo;
    }

    public void setMotivo(MotivoBaixa motivo) {
        this.motivo = motivo;
    }

    public LocalDate getDataBaixa() {
        return dataBaixa;
    }

    public void setDataBaixa(LocalDate dataBaixa) {
        this.dataBaixa = dataBaixa;
    }

    public boolean isAutomatica() {
        return automatica;
    }

    public void setAutomatica(boolean automatica) {
        this.automatica = automatica;
    }
}
