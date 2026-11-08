package com.jackops.rotavital.model;

import com.jackops.rotavital.model.enums.TipoLocal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Vértice da rede de distribuição: um hemocentro ou um hospital.
 */
@Entity
@Table(name = "local_rede")
public class LocalRede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoLocal tipo;

    public LocalRede() {
    }

    public LocalRede(String nome, TipoLocal tipo) {
        this.nome = nome;
        this.tipo = tipo;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public TipoLocal getTipo() {
        return tipo;
    }
}
