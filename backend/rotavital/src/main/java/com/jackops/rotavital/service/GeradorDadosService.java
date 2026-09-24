package com.jackops.rotavital.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.jackops.rotavital.model.enums.StatusBolsa;
import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;
import com.jackops.rotavital.model.relatorio.BolsaEmEstoque;
import com.jackops.rotavital.model.relatorio.DemandaHospitalar;

/**
 * Gera massas de dados sinteticas (bolsas e solicitacoes) em memoria, simulando a escala nacional.
 * A semente e fixa, entao o mesmo tamanho sempre gera os mesmos dados. As massas ficam em cache
 * para que a medicao do endpoint considere apenas o processamento, e nao a geracao.
 */
@Service
public class GeradorDadosService {

    public static final LocalDate DATA_REFERENCIA = LocalDate.of(2026, 9, 23);

    /** Unidades que armazenam bolsas (hemocentros, hemonucleos e agencias transfusionais). */
    public static final int UNIDADES_ARMAZENAMENTO = 3_000;
    public static final int HOSPITAIS = 5_000;

    private static final long SEMENTE = 42L;
    private static final TipoSanguineo[] TIPOS = TipoSanguineo.values();
    private static final TipoComponente[] COMPONENTES = TipoComponente.values();

    // Retangulo aproximado do territorio brasileiro.
    private static final double LATITUDE_MIN = -33.0;
    private static final double LATITUDE_MAX = 5.0;
    private static final double LONGITUDE_MIN = -73.0;
    private static final double LONGITUDE_MAX = -35.0;

    private static final double[][] COORDENADAS_UNIDADES = gerarCoordenadas(UNIDADES_ARMAZENAMENTO, SEMENTE + 2);
    private static final double[][] COORDENADAS_HOSPITAIS = gerarCoordenadas(HOSPITAIS, SEMENTE + 3);

    private final Map<Integer, List<BolsaEmEstoque>> bolsasPorTamanho = new ConcurrentHashMap<>();
    private final Map<Integer, List<DemandaHospitalar>> solicitacoesPorTamanho = new ConcurrentHashMap<>();

    public List<BolsaEmEstoque> bolsas(int quantidade) {
        return bolsasPorTamanho.computeIfAbsent(quantidade, GeradorDadosService::gerarBolsas);
    }

    public List<DemandaHospitalar> solicitacoes(int quantidade) {
        return solicitacoesPorTamanho.computeIfAbsent(quantidade, GeradorDadosService::gerarSolicitacoes);
    }

    public static List<BolsaEmEstoque> gerarBolsas(int quantidade) {
        Random random = new Random(SEMENTE);
        List<BolsaEmEstoque> bolsas = new ArrayList<>(quantidade);
        for (int i = 0; i < quantidade; i++) {
            LocalDate dataColeta = DATA_REFERENCIA.minusDays(random.nextInt(60));
            int unidade = random.nextInt(UNIDADES_ARMAZENAMENTO);
            bolsas.add(new BolsaEmEstoque(
                    TIPOS[random.nextInt(TIPOS.length)],
                    COMPONENTES[random.nextInt(COMPONENTES.length)],
                    dataColeta.plusDays(5 + random.nextInt(60)),
                    sortearStatus(random),
                    unidade,
                    COORDENADAS_UNIDADES[unidade][0],
                    COORDENADAS_UNIDADES[unidade][1]));
        }
        return List.copyOf(bolsas);
    }

    public static List<DemandaHospitalar> gerarSolicitacoes(int quantidade) {
        Random random = new Random(SEMENTE + 1);
        List<DemandaHospitalar> solicitacoes = new ArrayList<>(quantidade);
        for (int i = 0; i < quantidade; i++) {
            int hospital = random.nextInt(HOSPITAIS);
            solicitacoes.add(new DemandaHospitalar(
                    "Hospital " + (hospital + 1),
                    TIPOS[random.nextInt(TIPOS.length)],
                    COMPONENTES[random.nextInt(COMPONENTES.length)],
                    1 + random.nextInt(10),
                    COORDENADAS_HOSPITAIS[hospital][0],
                    COORDENADAS_HOSPITAIS[hospital][1]));
        }
        return List.copyOf(solicitacoes);
    }

    private static double[][] gerarCoordenadas(int quantidade, long semente) {
        Random random = new Random(semente);
        double[][] coordenadas = new double[quantidade][2];
        for (int i = 0; i < quantidade; i++) {
            coordenadas[i][0] = LATITUDE_MIN + random.nextDouble() * (LATITUDE_MAX - LATITUDE_MIN);
            coordenadas[i][1] = LONGITUDE_MIN + random.nextDouble() * (LONGITUDE_MAX - LONGITUDE_MIN);
        }
        return coordenadas;
    }

    private static StatusBolsa sortearStatus(Random random) {
        int sorteio = random.nextInt(10);
        if (sorteio < 7) {
            return StatusBolsa.DISPONIVEL;
        }
        return sorteio < 9 ? StatusBolsa.ALOCADA : StatusBolsa.INDISPONIVEL;
    }
}
