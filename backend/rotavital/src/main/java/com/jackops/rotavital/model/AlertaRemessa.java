package com.jackops.rotavital.model;

import java.time.LocalDateTime;

import com.jackops.rotavital.model.enums.TipoAlertaRemessa;

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
 * Alerta emitido para o operador de logística sobre uma remessa.
 */
@Entity
@Table(name = "alerta_remessa")
public class AlertaRemessa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "remessa_id", nullable = false)
    private Remessa remessa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAlertaRemessa tipo;

    @Column(nullable = false, length = 1000)
    private String mensagem;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    public AlertaRemessa() {
    }

    public AlertaRemessa(Remessa remessa, TipoAlertaRemessa tipo, String mensagem, LocalDateTime criadoEm) {
        this.remessa = remessa;
        this.tipo = tipo;
        this.mensagem = mensagem;
        this.criadoEm = criadoEm;
    }

    public Long getId() {
        return id;
    }

    public Remessa getRemessa() {
        return remessa;
    }

    public TipoAlertaRemessa getTipo() {
        return tipo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
