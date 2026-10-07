package com.jackops.rotavital.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jackops.rotavital.dto.DemandaPorHospitalResponse;
import com.jackops.rotavital.dto.EstoquePorTipoResponse;
import com.jackops.rotavital.dto.IndicadoresResponse;
import com.jackops.rotavital.model.enums.StatusBolsa;
import com.jackops.rotavital.model.enums.StatusSolicitacao;
import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;
import com.jackops.rotavital.repository.BolsaRepository;
import com.jackops.rotavital.repository.SolicitacaoSangueRepository;

@Service
public class IndicadoresService {

    private final BolsaRepository bolsaRepository;
    private final SolicitacaoSangueRepository solicitacaoRepository;

    public IndicadoresService(BolsaRepository bolsaRepository,SolicitacaoSangueRepository solicitacaoRepository) {
        this.bolsaRepository = bolsaRepository;
        this.solicitacaoRepository = solicitacaoRepository;
    }

    @Transactional(readOnly = true)
    public IndicadoresResponse consultar() {
        LocalDate hoje = LocalDate.now();

        return new IndicadoresResponse(
                hoje,
                "Quantidade de bolsas com status DISPONIVEL e validade igual ou posterior à data de referência, agrupadas por componente e tipo sanguíneo.",
                "Soma das quantidades solicitadas em pedidos PENDENTE ou EM_ATENDIMENTO, agrupadas por hospital, componente e tipo sanguíneo. Pedidos ATENDIDA e CANCELADA não entram na soma.",
                calcularEstoque(hoje),
                calcularDemanda());
    }

    private List<EstoquePorTipoResponse> calcularEstoque(LocalDate hoje) {
        var estoqueAgrupado = bolsaRepository.findAll().stream()
                .filter(bolsa -> bolsa.getStatus() == StatusBolsa.DISPONIVEL)
                .filter(bolsa -> !bolsa.getDataValidade().isBefore(hoje))
                .collect(Collectors.groupingBy(
                        bolsa -> new ChaveEstoque(
                                bolsa.getTipoComponente(),
                                bolsa.getTipoSanguineo()),
                        Collectors.counting()));

        return estoqueAgrupado.entrySet().stream()
                .map(item -> new EstoquePorTipoResponse(
                        item.getKey().tipoComponente(),
                        item.getKey().tipoSanguineo(),
                        item.getValue()))
                .sorted(Comparator
                        .comparing(EstoquePorTipoResponse::tipoComponente)
                        .thenComparing(EstoquePorTipoResponse::tipoSanguineo))
                .toList();
    }

    private List<DemandaPorHospitalResponse> calcularDemanda() {
        var demandaAgrupada = solicitacaoRepository.findAll().stream()
                .filter(solicitacao ->
                        solicitacao.getStatus() == StatusSolicitacao.PENDENTE
                        || solicitacao.getStatus() == StatusSolicitacao.EM_ATENDIMENTO)
                .collect(Collectors.groupingBy(
                        solicitacao -> new ChaveDemanda(
                                solicitacao.getNomeHospital(),
                                solicitacao.getTipoComponente(),
                                solicitacao.getTipoSanguineo()),
                        Collectors.summingLong(
                                solicitacao -> solicitacao.getQuantidade().longValue())));

        return demandaAgrupada.entrySet().stream()
                .map(item -> new DemandaPorHospitalResponse(
                        item.getKey().nomeHospital(),
                        item.getKey().tipoComponente(),
                        item.getKey().tipoSanguineo(),
                        item.getValue()))
                .sorted(Comparator
                        .comparing(DemandaPorHospitalResponse::nomeHospital)
                        .thenComparing(DemandaPorHospitalResponse::tipoComponente)
                        .thenComparing(DemandaPorHospitalResponse::tipoSanguineo))
                .toList();
    }

    private record ChaveEstoque(
            TipoComponente tipoComponente,
            TipoSanguineo tipoSanguineo) {
    }

    private record ChaveDemanda(
            String nomeHospital,
            TipoComponente tipoComponente,
            TipoSanguineo tipoSanguineo) {
    }
}