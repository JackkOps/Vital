package com.jackops.rotavital.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.jackops.rotavital.dto.AlertaValidadeResponse;
import com.jackops.rotavital.dto.BaixaBolsaResponse;
import com.jackops.rotavital.dto.VerificacaoValidadeResponse;
import com.jackops.rotavital.estrutura.PilhaHistorico;
import com.jackops.rotavital.exception.RegraDeNegocioException;
import com.jackops.rotavital.model.BaixaBolsa;
import com.jackops.rotavital.model.Bolsa;
import com.jackops.rotavital.model.enums.MotivoBaixa;
import com.jackops.rotavital.model.enums.StatusBolsa;
import com.jackops.rotavital.repository.BaixaBolsaRepository;
import com.jackops.rotavital.repository.BolsaRepository;

/**
 * História 5 - Alerta e baixa de bolsas próximas do vencimento.
 */
@Service
public class ValidadeBolsaService {
    private final BolsaRepository bolsaRepository;
    private final BaixaBolsaRepository baixaBolsaRepository;
    private final PilhaHistorico pilhaHistorico;

    public ValidadeBolsaService(BolsaRepository bolsaRepository, BaixaBolsaRepository baixaBolsaRepository,
            PilhaHistorico pilhaHistorico) {
        this.bolsaRepository = bolsaRepository;
        this.baixaBolsaRepository = baixaBolsaRepository;
        this.pilhaHistorico = pilhaHistorico;
    }

    /**
     * Lista as bolsas disponíveis que estão dentro da janela de alerta do seu
     * componente, da que vence primeiro para a última.
     */
    @Transactional(readOnly = true)
    public List<AlertaValidadeResponse> listarAlertas() {
        LocalDate hoje = LocalDate.now();
        return bolsaRepository.findByStatus(StatusBolsa.DISPONIVEL).stream()
                .filter(bolsa -> bolsa.estaProximaDoVencimentoEm(hoje))
                .sorted(Comparator.comparing(Bolsa::getDataValidade))
                .map(bolsa -> paraAlerta(bolsa, hoje))
                .toList();
    }

    /**
     * Dá baixa automática nas bolsas disponíveis já vencidas e devolve os
     * alertas das bolsas próximas do vencimento.
     */
    @Transactional
    public VerificacaoValidadeResponse verificarValidade() {
        LocalDate hoje = LocalDate.now();
        List<BaixaBolsaResponse> baixas = bolsaRepository.findByStatus(StatusBolsa.DISPONIVEL).stream()
                .filter(bolsa -> bolsa.estaVencidaEm(hoje))
                .sorted(Comparator.comparing(Bolsa::getDataValidade))
                .map(bolsa -> registrarBaixa(bolsa, hoje, true))
                .toList();

        return new VerificacaoValidadeResponse(hoje, baixas, listarAlertas());
    }

    /**
     * Baixa manual por vencimento, solicitada pelo gestor.
     */
    @Transactional
    public BaixaBolsaResponse darBaixaPorVencimento(Long bolsaId) {
        Bolsa bolsa = bolsaRepository.findById(bolsaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bolsa não encontrada"));

        if (bolsa.getStatus() == StatusBolsa.ALOCADA) {
            throw new RegraDeNegocioException("Cancele a alocação antes de dar baixa");
        }
        if (bolsa.getStatus() == StatusBolsa.DESCARTADA) {
            throw new RegraDeNegocioException("Bolsa já foi descartada");
        }
        if (bolsa.getStatus() != StatusBolsa.DISPONIVEL) {
            throw new RegraDeNegocioException("Somente bolsas disponíveis podem receber baixa");
        }

        LocalDate hoje = LocalDate.now();
        if (!bolsa.estaVencidaEm(hoje)) {
            throw new RegraDeNegocioException("Bolsa ainda está dentro da validade");
        }

        return registrarBaixa(bolsa, hoje, false);
    }

    @Transactional(readOnly = true)
    public List<BaixaBolsaResponse> listarBaixas() {
        return baixaBolsaRepository.findAllByOrderByIdAsc().stream()
                .map(this::paraResponse)
                .toList();
    }

    private BaixaBolsaResponse registrarBaixa(Bolsa bolsa, LocalDate dataBaixa, boolean automatica) {
        bolsa.setStatus(StatusBolsa.DESCARTADA);
        bolsaRepository.save(bolsa);

        BaixaBolsa baixa = baixaBolsaRepository.save(
                new BaixaBolsa(bolsa, MotivoBaixa.VENCIMENTO, dataBaixa, automatica));
        pilhaHistorico.empilhar("Baixa por vencimento da bolsa " + bolsa.getIdentificador());
        return paraResponse(baixa);
    }

    private AlertaValidadeResponse paraAlerta(Bolsa bolsa, LocalDate hoje) {
        return new AlertaValidadeResponse(bolsa.getId(), bolsa.getIdentificador(), bolsa.getTipoSanguineo(),
                bolsa.getTipoComponente(), bolsa.getDataValidade(),
                ChronoUnit.DAYS.between(hoje, bolsa.getDataValidade()));
    }

    private BaixaBolsaResponse paraResponse(BaixaBolsa baixa) {
        return new BaixaBolsaResponse(baixa.getId(), baixa.getBolsa().getId(), baixa.getBolsa().getIdentificador(),
                baixa.getMotivo(), baixa.getDataBaixa(), baixa.isAutomatica());
    }
}
