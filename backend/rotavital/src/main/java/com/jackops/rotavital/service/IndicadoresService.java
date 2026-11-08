package com.jackops.rotavital.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.jackops.rotavital.dto.DemandaPorHospitalResponse;
import com.jackops.rotavital.dto.EstoquePorTipoResponse;
import com.jackops.rotavital.dto.FiltroIndicadores;
import com.jackops.rotavital.dto.IndicadoresResponse;
import com.jackops.rotavital.dto.SituacaoComponenteResponse;
import com.jackops.rotavital.model.Bolsa;
import com.jackops.rotavital.model.SolicitacaoSangue;
import com.jackops.rotavital.model.enums.StatusBolsa;
import com.jackops.rotavital.model.enums.StatusSolicitacao;
import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;
import com.jackops.rotavital.repository.BolsaRepository;
import com.jackops.rotavital.repository.SolicitacaoSangueRepository;

/**
 * História 7 - Painel de indicadores da rede de sangue. Os números são recalculados
 * a cada consulta a partir das bolsas e solicitações gravadas.
 */
@Service
public class IndicadoresService {

    static final String MENSAGEM_DADOS_INSUFICIENTES = "Dados insuficientes para o período";

    private static final String DESCRICAO_ESTOQUE = "Quantidade de bolsas com status DISPONIVEL e validade igual ou posterior à data de referência, agrupadas por componente e tipo sanguíneo.";
    private static final String DESCRICAO_DEMANDA = "Soma das quantidades solicitadas em pedidos PENDENTE ou EM_ATENDIMENTO, agrupadas por hospital, componente e tipo sanguíneo. Pedidos ATENDIDA e CANCELADA não entram na soma.";

    private final BolsaRepository bolsaRepository;
    private final SolicitacaoSangueRepository solicitacaoRepository;
    private final int periodoPadraoDias;
    private final int nivelCritico;
    private final int horizonteDias;

    public IndicadoresService(BolsaRepository bolsaRepository,
            SolicitacaoSangueRepository solicitacaoRepository,
            @Value("${rotavital.indicadores.periodo-padrao-dias:30}") int periodoPadraoDias,
            @Value("${rotavital.indicadores.nivel-critico:5}") int nivelCritico,
            @Value("${rotavital.indicadores.horizonte-dias:7}") int horizonteDias) {
        this.bolsaRepository = bolsaRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.periodoPadraoDias = periodoPadraoDias;
        this.nivelCritico = nivelCritico;
        this.horizonteDias = horizonteDias;
    }

    @Transactional(readOnly = true)
    public IndicadoresResponse consultar() {
        return consultar(FiltroIndicadores.semFiltros());
    }

    @Transactional(readOnly = true)
    public IndicadoresResponse consultar(FiltroIndicadores filtro) {
        LocalDate hoje = LocalDate.now();
        LocalDate fim = filtro.dataFim() != null ? filtro.dataFim() : hoje;
        LocalDate inicio = filtro.dataInicio() != null ? filtro.dataInicio() : fim.minusDays(periodoPadraoDias - 1L);
        if (inicio.isAfter(fim)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Data inicial do período não pode ser posterior à data final");
        }

        Predicate<TipoComponente> componenteFiltrado = componente ->
                filtro.tipoComponente() == null || filtro.tipoComponente() == componente;
        String hospital = filtro.hospital() == null || filtro.hospital().isBlank() ? null : filtro.hospital().trim();

        List<Bolsa> bolsas = bolsaRepository.findAll().stream()
                .filter(bolsa -> componenteFiltrado.test(bolsa.getTipoComponente()))
                .toList();
        List<SolicitacaoSangue> solicitacoes = solicitacaoRepository.findAll().stream()
                .filter(solicitacao -> componenteFiltrado.test(solicitacao.getTipoComponente()))
                .filter(solicitacao -> hospital == null || hospital.equalsIgnoreCase(solicitacao.getNomeHospital()))
                .toList();

        if (!houveMovimentacao(bolsas, solicitacoes, inicio, fim)) {
            return new IndicadoresResponse(hoje, inicio, fim, false, MENSAGEM_DADOS_INSUFICIENTES,
                    DESCRICAO_ESTOQUE, DESCRICAO_DEMANDA, List.of(), List.of(), null, null, horizonteDias, List.of());
        }

        List<SolicitacaoSangue> emAberto = solicitacoes.stream().filter(this::estaEmAberto).toList();
        List<TipoComponente> componentes = Arrays.stream(TipoComponente.values())
                .filter(componenteFiltrado)
                .toList();

        return new IndicadoresResponse(
                hoje,
                inicio,
                fim,
                true,
                null,
                DESCRICAO_ESTOQUE,
                DESCRICAO_DEMANDA,
                calcularEstoque(bolsas, hoje),
                calcularDemanda(emAberto),
                calcularTaxaDescarte(bolsas, inicio, fim, hoje),
                calcularTempoMedioAtendimento(solicitacoes, inicio, fim),
                horizonteDias,
                componentes.stream()
                        .map(componente -> calcularSituacao(componente, bolsas, solicitacoes, emAberto, inicio, fim, hoje))
                        .toList());
    }

    /**
     * Houve movimentação quando alguma bolsa foi coletada ou venceu no período, ou
     * algum pedido foi registrado ou atendido nele.
     */
    private boolean houveMovimentacao(List<Bolsa> bolsas, List<SolicitacaoSangue> solicitacoes,
            LocalDate inicio, LocalDate fim) {
        boolean bolsasNoPeriodo = bolsas.stream().anyMatch(bolsa ->
                dentro(bolsa.getDataColeta(), inicio, fim) || dentro(bolsa.getDataValidade(), inicio, fim));
        boolean pedidosNoPeriodo = solicitacoes.stream().anyMatch(solicitacao ->
                dentro(solicitacao.getCriadaEm(), inicio, fim) || dentro(solicitacao.getAtendidaEm(), inicio, fim));
        return bolsasNoPeriodo || pedidosNoPeriodo;
    }

    private List<EstoquePorTipoResponse> calcularEstoque(List<Bolsa> bolsas, LocalDate hoje) {
        var estoqueAgrupado = bolsas.stream()
                .filter(bolsa -> estaDisponivel(bolsa, hoje))
                .collect(Collectors.groupingBy(
                        bolsa -> new ChaveEstoque(bolsa.getTipoComponente(), bolsa.getTipoSanguineo()),
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

    private List<DemandaPorHospitalResponse> calcularDemanda(List<SolicitacaoSangue> emAberto) {
        var demandaAgrupada = emAberto.stream()
                .collect(Collectors.groupingBy(
                        solicitacao -> new ChaveDemanda(
                                solicitacao.getNomeHospital(),
                                solicitacao.getTipoComponente(),
                                solicitacao.getTipoSanguineo()),
                        Collectors.summingLong(solicitacao -> solicitacao.getQuantidade().longValue())));

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

    /**
     * Das bolsas cuja validade terminou no período (até ontem), a fração que foi
     * descartada em vez de usada. Nulo quando nenhuma bolsa venceu no período.
     */
    private Double calcularTaxaDescarte(List<Bolsa> bolsas, LocalDate inicio, LocalDate fim, LocalDate hoje) {
        LocalDate ultimoDiaVencido = fim.isBefore(hoje) ? fim : hoje.minusDays(1);
        List<Bolsa> vencidasNoPeriodo = bolsas.stream()
                .filter(bolsa -> dentro(bolsa.getDataValidade(), inicio, ultimoDiaVencido))
                .toList();
        if (vencidasNoPeriodo.isEmpty()) {
            return null;
        }
        long descartadas = vencidasNoPeriodo.stream()
                .filter(bolsa -> bolsa.getStatus() == StatusBolsa.DESCARTADA)
                .count();
        return arredondar((double) descartadas / vencidasNoPeriodo.size(), 4);
    }

    /**
     * Média, em horas, entre o registro e o atendimento completo dos pedidos atendidos
     * no período. Nulo quando nenhum pedido foi atendido nele.
     */
    private Double calcularTempoMedioAtendimento(List<SolicitacaoSangue> solicitacoes, LocalDate inicio, LocalDate fim) {
        var minutos = solicitacoes.stream()
                .filter(solicitacao -> solicitacao.getStatus() == StatusSolicitacao.ATENDIDA)
                .filter(solicitacao -> solicitacao.getCriadaEm() != null)
                .filter(solicitacao -> dentro(solicitacao.getAtendidaEm(), inicio, fim))
                .mapToLong(solicitacao -> Duration.between(solicitacao.getCriadaEm(), solicitacao.getAtendidaEm()).toMinutes())
                .average();
        return minutos.isPresent() ? arredondar(minutos.getAsDouble() / 60.0, 1) : null;
    }

    private SituacaoComponenteResponse calcularSituacao(TipoComponente componente, List<Bolsa> bolsas,
            List<SolicitacaoSangue> solicitacoes, List<SolicitacaoSangue> emAberto,
            LocalDate inicio, LocalDate fim, LocalDate hoje) {
        long disponiveis = bolsas.stream()
                .filter(bolsa -> bolsa.getTipoComponente() == componente && estaDisponivel(bolsa, hoje))
                .count();
        long demandaAberta = emAberto.stream()
                .filter(solicitacao -> solicitacao.getTipoComponente() == componente)
                .mapToLong(solicitacao -> solicitacao.getQuantidade() - bolsaRepository.countBySolicitacaoId(solicitacao.getId()))
                .sum();
        long pedidasNoPeriodo = solicitacoes.stream()
                .filter(solicitacao -> solicitacao.getTipoComponente() == componente)
                .filter(solicitacao -> dentro(solicitacao.getCriadaEm(), inicio, fim))
                .mapToLong(SolicitacaoSangue::getQuantidade)
                .sum();

        double demandaDiaria = (double) pedidasNoPeriodo / (ChronoUnit.DAYS.between(inicio, fim) + 1);
        double probabilidade = probabilidadeDesabastecimento(disponiveis - demandaAberta, demandaDiaria * horizonteDias);

        return new SituacaoComponenteResponse(componente, disponiveis, demandaAberta, nivelCritico,
                disponiveis < nivelCritico, probabilidade);
    }

    /**
     * Chance de a demanda do horizonte passar do estoque livre, supondo que os pedidos
     * chegam como um processo de Poisson com a média diária observada no período:
     * P(D > estoqueLivre) = 1 - soma de k = 0 até estoqueLivre de e^-λ λ^k / k!.
     * Se a demanda já em aberto supera o estoque, a falta é certa.
     */
    static double probabilidadeDesabastecimento(long estoqueLivre, double demandaEsperada) {
        if (estoqueLivre < 0) {
            return 1.0;
        }
        if (demandaEsperada <= 0) {
            return 0.0;
        }
        double termo = Math.exp(-demandaEsperada);
        double acumulada = termo;
        for (long k = 1; k <= estoqueLivre; k++) {
            termo *= demandaEsperada / k;
            acumulada += termo;
        }
        return arredondar(Math.max(0.0, 1.0 - acumulada), 4);
    }

    private boolean estaDisponivel(Bolsa bolsa, LocalDate hoje) {
        return bolsa.getStatus() == StatusBolsa.DISPONIVEL && !bolsa.getDataValidade().isBefore(hoje);
    }

    private boolean estaEmAberto(SolicitacaoSangue solicitacao) {
        return solicitacao.getStatus() == StatusSolicitacao.PENDENTE
                || solicitacao.getStatus() == StatusSolicitacao.EM_ATENDIMENTO;
    }

    private static boolean dentro(LocalDate data, LocalDate inicio, LocalDate fim) {
        return data != null && !data.isBefore(inicio) && !data.isAfter(fim);
    }

    private static boolean dentro(LocalDateTime momento, LocalDate inicio, LocalDate fim) {
        return momento != null && dentro(momento.toLocalDate(), inicio, fim);
    }

    private static double arredondar(double valor, int casas) {
        double fator = Math.pow(10, casas);
        return Math.round(valor * fator) / fator;
    }

    private record ChaveEstoque(TipoComponente tipoComponente, TipoSanguineo tipoSanguineo) {
    }

    private record ChaveDemanda(String nomeHospital, TipoComponente tipoComponente, TipoSanguineo tipoSanguineo) {
    }
}
