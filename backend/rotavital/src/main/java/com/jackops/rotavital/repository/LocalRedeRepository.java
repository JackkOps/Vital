package com.jackops.rotavital.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jackops.rotavital.model.LocalRede;

@Repository
public interface LocalRedeRepository extends JpaRepository<LocalRede, Long> {
    List<LocalRede> findAllByOrderByIdAsc();
    Optional<LocalRede> findByNomeIgnoreCase(String nome);
}
