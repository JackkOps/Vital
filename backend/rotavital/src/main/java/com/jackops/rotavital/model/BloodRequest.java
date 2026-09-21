package com.jackops.rotavital.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "blood_request")
public class BloodRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String hospitalName;

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
    private Integer quantity;

    public BloodRequest() {
    }

    public BloodRequest(Long id, String hospitalName, BloodType bloodType, RhFactor rhFactor,
                       ComponentType componentType, Integer quantity) {
        this.id = id;
        this.hospitalName = hospitalName;
        this.bloodType = bloodType;
        this.rhFactor = rhFactor;
        this.componentType = componentType;
        this.quantity = quantity;
    }

    public static BloodRequestBuilder builder() {
        return new BloodRequestBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    public BloodType getBloodType() { return bloodType; }
    public void setBloodType(BloodType bloodType) { this.bloodType = bloodType; }
    public RhFactor getRhFactor() { return rhFactor; }
    public void setRhFactor(RhFactor rhFactor) { this.rhFactor = rhFactor; }
    public ComponentType getComponentType() { return componentType; }
    public void setComponentType(ComponentType componentType) { this.componentType = componentType; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public static class BloodRequestBuilder {
        private Long id;
        private String hospitalName;
        private BloodType bloodType;
        private RhFactor rhFactor;
        private ComponentType componentType;
        private Integer quantity;

        public BloodRequestBuilder id(Long id) { this.id = id; return this; }
        public BloodRequestBuilder hospitalName(String hospitalName) { this.hospitalName = hospitalName; return this; }
        public BloodRequestBuilder bloodType(BloodType bloodType) { this.bloodType = bloodType; return this; }
        public BloodRequestBuilder rhFactor(RhFactor rhFactor) { this.rhFactor = rhFactor; return this; }
        public BloodRequestBuilder componentType(ComponentType componentType) { this.componentType = componentType; return this; }
        public BloodRequestBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public BloodRequest build() { return new BloodRequest(id, hospitalName, bloodType, rhFactor, componentType, quantity); }
    }
}
