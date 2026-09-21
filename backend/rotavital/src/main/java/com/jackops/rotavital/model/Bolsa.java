package com.jackops.rotavital.model;

import java.time.LocalDate;

import com.jackops.rotavital.model.enums.StatusBolsa;
import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bolsa")
public class Bolsa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoSanguineo tipoSanguineo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoComponente tipoComponente;

    @Column(nullable = false)
    private LocalDate dataColeta;

    @Column(nullable = false)
    private LocalDate dataValidade;

    @Column(nullable = false)
    private Integer volume;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusBolsa status;

    public Bolsa() {
    }

    public Bolsa(Long id, TipoSanguineo tipoSanguineo, TipoComponente tipoComponente,
                 LocalDate dataColeta, LocalDate dataValidade, Integer volume, StatusBolsa status) {
        this.id = id;
        this.tipoSanguineo = tipoSanguineo;
        this.tipoComponente = tipoComponente;
        this.dataColeta = dataColeta;
        this.dataValidade = dataValidade;
        this.volume = volume;
        this.status = status;
    }

    public static BolsaBuilder builder() {
        return new BolsaBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDate getDataColeta() {
        return dataColeta;
    }

    public void setDataColeta(LocalDate dataColeta) {
        this.dataColeta = dataColeta;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(LocalDate dataValidade) {
        this.dataValidade = dataValidade;
    }

    public Integer getVolume() {
        return volume;
    }

    public void setVolume(Integer volume) {
        this.volume = volume;
    }

    public StatusBolsa getStatus() {
        return status;
    }

    public void setStatus(StatusBolsa status) {
        this.status = status;
    }

    public static class BolsaBuilder {
        private Long id;
        private TipoSanguineo tipoSanguineo;
        private TipoComponente tipoComponente;
        private LocalDate dataColeta;
        private LocalDate dataValidade;
        private Integer volume;
        private StatusBolsa status;

        public BolsaBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public BolsaBuilder tipoSanguineo(TipoSanguineo tipoSanguineo) {
            this.tipoSanguineo = tipoSanguineo;
            return this;
        }

        public BolsaBuilder tipoComponente(TipoComponente tipoComponente) {
            this.tipoComponente = tipoComponente;
            return this;
        }

        public BolsaBuilder dataColeta(LocalDate dataColeta) {
            this.dataColeta = dataColeta;
            return this;
        }

        public BolsaBuilder dataValidade(LocalDate dataValidade) {
            this.dataValidade = dataValidade;
            return this;
        }

        public BolsaBuilder volume(Integer volume) {
            this.volume = volume;
            return this;
        }

        public BolsaBuilder status(StatusBolsa status) {
            this.status = status;
            return this;
        }

        public Bolsa build() {
            return new Bolsa(id, tipoSanguineo, tipoComponente, dataColeta, dataValidade, volume, status);
        }
    }
}
