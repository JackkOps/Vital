package com.jackops.rotavital.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jackops.rotavital.model.SolicitacaoSangue;

@Repository
public interface SolicitacaoSangueRepository extends JpaRepository<SolicitacaoSangue, Long> {
}
