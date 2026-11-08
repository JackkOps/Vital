package com.jackops.rotavital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jackops.rotavital.model.Rota;

@Repository
public interface RotaRepository extends JpaRepository<Rota, Long> {
    List<Rota> findAllByOrderByIdDesc();
}
