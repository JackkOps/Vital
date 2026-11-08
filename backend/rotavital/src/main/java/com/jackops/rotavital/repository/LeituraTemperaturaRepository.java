package com.jackops.rotavital.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jackops.rotavital.model.LeituraTemperatura;

@Repository
public interface LeituraTemperaturaRepository extends JpaRepository<LeituraTemperatura, Long> {
    List<LeituraTemperatura> findByRemessaIdOrderBySequenciaAsc(Long remessaId);
    Optional<LeituraTemperatura> findFirstByRemessaIdOrderByRecebidaEmDesc(Long remessaId);
    Optional<LeituraTemperatura> findByLeituraId(String leituraId);
    long countByRemessaId(Long remessaId);
}
