package com.jackops.rotavital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jackops.rotavital.model.Bolsa;
import com.jackops.rotavital.model.enums.StatusBolsa;

@Repository
public interface BolsaRepository extends JpaRepository<Bolsa, Long> {
    List<Bolsa> findByStatus(StatusBolsa status);
}
