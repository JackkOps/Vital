package com.jackops.rotavital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jackops.rotavital.model.SolicitacaoSangue;
import com.jackops.rotavital.model.enums.StatusSolicitacao;

@Repository
public interface SolicitacaoSangueRepository extends JpaRepository<SolicitacaoSangue, Long> {
    List<SolicitacaoSangue> findAllByOrderByIdAsc();
    List<SolicitacaoSangue> findByStatusOrderByIdAsc(StatusSolicitacao status);
}
