package com.jackops.rotavital.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jackops.rotavital.model.Bolsa;
import com.jackops.rotavital.model.enums.StatusBolsa;
import com.jackops.rotavital.model.enums.TipoComponente;

import jakarta.persistence.LockModeType;

@Repository
public interface BolsaRepository extends JpaRepository<Bolsa, Long> {
    List<Bolsa> findByStatus(StatusBolsa status);
    List<Bolsa> findAllByOrderByIdAsc();
    Optional<Bolsa> findByIdentificador(String identificador);
    boolean existsByIdentificador(String identificador);
    List<Bolsa> findBySolicitacaoIdOrderByIdAsc(Long solicitacaoId);
    long countBySolicitacaoId(Long solicitacaoId);

    /**
     * Busca as bolsas candidatas travando as linhas até o fim da transação, para que
     * duas alocações simultâneas não escolham a mesma bolsa.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bolsa b where b.status = :status and b.tipoComponente = :tipoComponente")
    List<Bolsa> travarPorStatusEComponente(@Param("status") StatusBolsa status,
                                           @Param("tipoComponente") TipoComponente tipoComponente);
}
