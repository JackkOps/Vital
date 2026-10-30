package com.jackops.rotavital.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jackops.rotavital.model.SolicitacaoSangue;
import com.jackops.rotavital.model.enums.StatusSolicitacao;

import jakarta.persistence.LockModeType;

@Repository
public interface SolicitacaoSangueRepository extends JpaRepository<SolicitacaoSangue, Long> {
    List<SolicitacaoSangue> findAllByOrderByIdAsc();
    List<SolicitacaoSangue> findByStatusOrderByIdAsc(StatusSolicitacao status);

    /**
     * Trava a solicitação até o fim da transação, para que alocações simultâneas
     * para o mesmo pedido não ultrapassem a quantidade solicitada.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SolicitacaoSangue s where s.id = :id")
    Optional<SolicitacaoSangue> travarPorId(@Param("id") Long id);
}
