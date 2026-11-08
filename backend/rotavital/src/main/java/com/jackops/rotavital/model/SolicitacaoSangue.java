package com.jackops.rotavital.model;

import java.time.LocalDateTime;

import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;
import com.jackops.rotavital.model.enums.NivelUrgencia;
import com.jackops.rotavital.model.enums.StatusSolicitacao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "solicitacao_sangue")
public class SolicitacaoSangue {

    /** Limite de bolsas que um hospital pode pedir numa única solicitação. */
    public static final int QUANTIDADE_MAXIMA = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomeHospital;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoSanguineo tipoSanguineo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoComponente tipoComponente;

    @Column(nullable = false)
    private Integer quantidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private NivelUrgencia nivelUrgencia = NivelUrgencia.ELETIVA;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private StatusSolicitacao status = StatusSolicitacao.PENDENTE;

    /** Momento em que o pedido foi registrado. */
    private LocalDateTime criadaEm;

    /** Momento em que o pedido recebeu todas as bolsas (status ATENDIDA). */
    private LocalDateTime atendidaEm;

    public SolicitacaoSangue() {
    }

    public SolicitacaoSangue(Long id, String nomeHospital, TipoSanguineo tipoSanguineo,
                            TipoComponente tipoComponente, Integer quantidade) {
        this.id = id;
        this.nomeHospital = nomeHospital;
        this.tipoSanguineo = tipoSanguineo;
        this.tipoComponente = tipoComponente;
        this.quantidade = quantidade;
    }

    public static SolicitacaoSangueBuilder builder() {
        return new SolicitacaoSangueBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeHospital() {
        return nomeHospital;
    }

    public void setNomeHospital(String nomeHospital) {
        this.nomeHospital = nomeHospital;
    }

    public TipoSanguineo getTipoSanguineo() {
        return tipoSanguineo;
    }

    public void setTipoSanguineo(TipoSanguineo tipoSanguineo) {
        this.tipoSanguineo = tipoSanguineo;
    }

    public TipoComponente getTipoComponente() {
        return tipoComponente;
    }

    public void setTipoComponente(TipoComponente tipoComponente) {
        this.tipoComponente = tipoComponente;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public NivelUrgencia getNivelUrgencia(){
        return nivelUrgencia;
    }

    public void setNivelUrgencia(NivelUrgencia nivelUrgencia){
        this.nivelUrgencia = nivelUrgencia;
    }

    public StatusSolicitacao getStatus(){
        return status;
    }

    public void setStatus(StatusSolicitacao status){
        this.status = status;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(LocalDateTime criadaEm) {
        this.criadaEm = criadaEm;
    }

    public LocalDateTime getAtendidaEm() {
        return atendidaEm;
    }

    public void setAtendidaEm(LocalDateTime atendidaEm) {
        this.atendidaEm = atendidaEm;
    }

    @PrePersist
    void registrarCriacao() {
        if (criadaEm == null) {
            criadaEm = LocalDateTime.now();
        }
    }

    public static class SolicitacaoSangueBuilder {
        private Long id;
        private String nomeHospital;
        private TipoSanguineo tipoSanguineo;
        private TipoComponente tipoComponente;
        private Integer quantidade;

        public SolicitacaoSangueBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public SolicitacaoSangueBuilder nomeHospital(String nomeHospital) {
            this.nomeHospital = nomeHospital;
            return this;
        }

        public SolicitacaoSangueBuilder tipoSanguineo(TipoSanguineo tipoSanguineo) {
            this.tipoSanguineo = tipoSanguineo;
            return this;
        }

        public SolicitacaoSangueBuilder tipoComponente(TipoComponente tipoComponente) {
            this.tipoComponente = tipoComponente;
            return this;
        }

        public SolicitacaoSangueBuilder quantidade(Integer quantidade) {
            this.quantidade = quantidade;
            return this;
        }

        public SolicitacaoSangue build() {
            return new SolicitacaoSangue(id, nomeHospital, tipoSanguineo, tipoComponente, quantidade);
        }
    }
}
