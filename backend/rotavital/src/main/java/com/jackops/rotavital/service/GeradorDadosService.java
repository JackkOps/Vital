package com.jackops.rotavital.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.jackops.rotavital.model.Bolsa;
import com.jackops.rotavital.model.SolicitacaoSangue;
import com.jackops.rotavital.model.enums.StatusBolsa;
import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;

/**
 * Gera massas de dados sinteticas (bolsas e solicitacoes) em memoria, simulando a escala nacional.
 * A semente e fixa, entao o mesmo tamanho sempre gera os mesmos dados. As massas ficam em cache
 * para que a medicao do endpoint considere apenas o processamento, e nao a geracao.
 */
@Service
public class GeradorDadosService {

    public static final LocalDate DATA_REFERENCIA = LocalDate.of(2026, 9, 23);

    private static final long SEMENTE = 42L;
    private static final TipoSanguineo[] TIPOS = TipoSanguineo.values();
    private static final TipoComponente[] COMPONENTES = TipoComponente.values();

    private final Map<Integer, List<Bolsa>> bolsasPorTamanho = new ConcurrentHashMap<>();
    private final Map<Integer, List<SolicitacaoSangue>> solicitacoesPorTamanho = new ConcurrentHashMap<>();

    public List<Bolsa> bolsas(int quantidade) {
        return bolsasPorTamanho.computeIfAbsent(quantidade, GeradorDadosService::gerarBolsas);
    }

    public List<SolicitacaoSangue> solicitacoes(int quantidade) {
        return solicitacoesPorTamanho.computeIfAbsent(quantidade, GeradorDadosService::gerarSolicitacoes);
    }

    public static List<Bolsa> gerarBolsas(int quantidade) {
        Random random = new Random(SEMENTE);
        List<Bolsa> bolsas = new ArrayList<>(quantidade);
        for (int i = 0; i < quantidade; i++) {
            LocalDate dataColeta = DATA_REFERENCIA.minusDays(random.nextInt(60));
            bolsas.add(Bolsa.builder()
                    .id((long) i + 1)
                    .identificador("BOLSA-" + (i + 1))
                    .tipoSanguineo(TIPOS[random.nextInt(TIPOS.length)])
                    .tipoComponente(COMPONENTES[random.nextInt(COMPONENTES.length)])
                    .dataColeta(dataColeta)
                    .dataValidade(dataColeta.plusDays(5 + random.nextInt(60)))
                    .volume(200 + random.nextInt(301))
                    .status(sortearStatus(random))
                    .build());
        }
        return List.copyOf(bolsas);
    }

    public static List<SolicitacaoSangue> gerarSolicitacoes(int quantidade) {
        Random random = new Random(SEMENTE + 1);
        List<SolicitacaoSangue> solicitacoes = new ArrayList<>(quantidade);
        for (int i = 0; i < quantidade; i++) {
            solicitacoes.add(SolicitacaoSangue.builder()
                    .id((long) i + 1)
                    .nomeHospital("Hospital " + (1 + random.nextInt(5000)))
                    .tipoSanguineo(TIPOS[random.nextInt(TIPOS.length)])
                    .tipoComponente(COMPONENTES[random.nextInt(COMPONENTES.length)])
                    .quantidade(1 + random.nextInt(10))
                    .build());
        }
        return List.copyOf(solicitacoes);
    }

    private static StatusBolsa sortearStatus(Random random) {
        int sorteio = random.nextInt(10);
        if (sorteio < 7) {
            return StatusBolsa.DISPONIVEL;
        }
        return sorteio < 9 ? StatusBolsa.ALOCADA : StatusBolsa.INDISPONIVEL;
    }
}
