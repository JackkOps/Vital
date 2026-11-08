package com.jackops.rotavital.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jackops.rotavital.model.Remessa;
import com.jackops.rotavital.model.enums.StatusRemessa;

import jakarta.persistence.LockModeType;

@Repository
public interface RemessaRepository extends JpaRepository<Remessa, Long> {
    List<Remessa> findAllByOrderByIdDesc();
    List<Remessa> findByStatus(StatusRemessa status);

    /**
     * Trava a remessa até o fim da transação, para que leituras simultâneas não
     * atualizem a última sequência e a condição ao mesmo tempo.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Remessa r where r.id = :id")
    Optional<Remessa> travarPorId(@Param("id") Long id);
}
