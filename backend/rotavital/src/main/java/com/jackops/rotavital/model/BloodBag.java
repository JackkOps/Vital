package com.jackops.rotavital.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "blood_bag")
public class BloodBag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BloodType bloodType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RhFactor rhFactor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComponentType componentType;

    @Column(nullable = false)
    private LocalDate collectionDate;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = false)
    private Integer volume;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BloodBagStatus status;

    public BloodBag() {
    }

    public BloodBag(Long id, BloodType bloodType, RhFactor rhFactor, ComponentType componentType,
                   LocalDate collectionDate, LocalDate expirationDate, Integer volume, BloodBagStatus status) {
        this.id = id;
        this.bloodType = bloodType;
        this.rhFactor = rhFactor;
        this.componentType = componentType;
        this.collectionDate = collectionDate;
        this.expirationDate = expirationDate;
        this.volume = volume;
        this.status = status;
    }

    public static BloodBagBuilder builder() {
        return new BloodBagBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BloodType getBloodType() { return bloodType; }
    public void setBloodType(BloodType bloodType) { this.bloodType = bloodType; }
    public RhFactor getRhFactor() { return rhFactor; }
    public void setRhFactor(RhFactor rhFactor) { this.rhFactor = rhFactor; }
    public ComponentType getComponentType() { return componentType; }
    public void setComponentType(ComponentType componentType) { this.componentType = componentType; }
    public LocalDate getCollectionDate() { return collectionDate; }
    public void setCollectionDate(LocalDate collectionDate) { this.collectionDate = collectionDate; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
    public Integer getVolume() { return volume; }
    public void setVolume(Integer volume) { this.volume = volume; }
    public BloodBagStatus getStatus() { return status; }
    public void setStatus(BloodBagStatus status) { this.status = status; }

    public static class BloodBagBuilder {
        private Long id;
        private BloodType bloodType;
        private RhFactor rhFactor;
        private ComponentType componentType;
        private LocalDate collectionDate;
        private LocalDate expirationDate;
        private Integer volume;
        private BloodBagStatus status;

        public BloodBagBuilder id(Long id) { this.id = id; return this; }
        public BloodBagBuilder bloodType(BloodType bloodType) { this.bloodType = bloodType; return this; }
        public BloodBagBuilder rhFactor(RhFactor rhFactor) { this.rhFactor = rhFactor; return this; }
        public BloodBagBuilder componentType(ComponentType componentType) { this.componentType = componentType; return this; }
        public BloodBagBuilder collectionDate(LocalDate collectionDate) { this.collectionDate = collectionDate; return this; }
        public BloodBagBuilder expirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; return this; }
        public BloodBagBuilder volume(Integer volume) { this.volume = volume; return this; }
        public BloodBagBuilder status(BloodBagStatus status) { this.status = status; return this; }
        public BloodBag build() { return new BloodBag(id, bloodType, rhFactor, componentType, collectionDate, expirationDate, volume, status); }
    }
}
