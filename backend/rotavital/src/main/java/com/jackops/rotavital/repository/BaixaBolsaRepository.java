package com.jackops.rotavital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jackops.rotavital.model.BaixaBolsa;

@Repository
public interface BaixaBolsaRepository extends JpaRepository<BaixaBolsa, Long> {
    List<BaixaBolsa> findAllByOrderByIdAsc();
}
