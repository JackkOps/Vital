package com.jackops.rotavital.model;

import java.time.LocalDateTime;

import com.jackops.rotavital.model.enums.CondicaoRemessa;
import com.jackops.rotavital.model.enums.StatusRemessa;
import com.jackops.rotavital.model.enums.TipoComponente;

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
 * Carga de hemocomponentes em trânsito, acompanhada por um sensor de temperatura.
 */
@Entity
@Table(name = "remessa")
public class Remessa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Rota confirmada que a remessa segue (opcional). */
    @ManyToOne
    @JoinColumn(name = "rota_id")
    private Rota rota;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoComponente tipoComponente;

    @Column(nullable = false)
    private String sensorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRemessa status = StatusRemessa.EM_TRANSITO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CondicaoRemessa condicao = CondicaoRemessa.CONFORME;

    @Column(nullable = false)
    private boolean sensorIndisponivel;

    @Column(nullable = false)
    private LocalDateTime iniciadaEm;

    private LocalDateTime ultimaLeituraEm;

    private Long ultimaSequencia;

    private LocalDateTime entregueEm;

    public Remessa() {
    }

    public Remessa(Rota rota, TipoComponente tipoComponente, String sensorId, LocalDateTime iniciadaEm) {
        this.rota = rota;
        this.tipoComponente = tipoComponente;
        this.sensorId = sensorId;
        this.iniciadaEm = iniciadaEm;
    }

    public Long getId() {
        return id;
    }

    public Rota getRota() {
        return rota;
    }

    public TipoComponente getTipoComponente() {
        return tipoComponente;
    }

    public String getSensorId() {
        return sensorId;
    }

    public StatusRemessa getStatus() {
        return status;
    }

    public void setStatus(StatusRemessa status) {
        this.status = status;
    }

    public CondicaoRemessa getCondicao() {
        return condicao;
    }

    public void setCondicao(CondicaoRemessa condicao) {
        this.condicao = condicao;
    }

    public boolean isSensorIndisponivel() {
        return sensorIndisponivel;
    }

    public void setSensorIndisponivel(boolean sensorIndisponivel) {
        this.sensorIndisponivel = sensorIndisponivel;
    }

    public LocalDateTime getIniciadaEm() {
        return iniciadaEm;
    }

    public void setIniciadaEm(LocalDateTime iniciadaEm) {
        this.iniciadaEm = iniciadaEm;
    }

    public LocalDateTime getUltimaLeituraEm() {
        return ultimaLeituraEm;
    }

    public void setUltimaLeituraEm(LocalDateTime ultimaLeituraEm) {
        this.ultimaLeituraEm = ultimaLeituraEm;
    }

    public Long getUltimaSequencia() {
        return ultimaSequencia;
    }

    public void setUltimaSequencia(Long ultimaSequencia) {
        this.ultimaSequencia = ultimaSequencia;
    }

    public LocalDateTime getEntregueEm() {
        return entregueEm;
    }

    public void setEntregueEm(LocalDateTime entregueEm) {
        this.entregueEm = entregueEm;
    }
}
