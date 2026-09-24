package com.jackops.rotavital.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.jackops.rotavital.dto.CoberturaPorTipo;
import com.jackops.rotavital.dto.RelatorioCoberturaResponse;
import com.jackops.rotavital.model.enums.StatusBolsa;
import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;
import com.jackops.rotavital.model.relatorio.BolsaEmEstoque;
import com.jackops.rotavital.model.relatorio.DemandaHospitalar;
import com.jackops.rotavital.service.CoberturaEstoqueService.Modo;

@SpringBootTest
class CoberturaEstoqueServiceTest {

    // Recife
    private static final double LATITUDE_HOSPITAL = -8.05;
    private static final double LONGITUDE_HOSPITAL = -34.90;

    @Autowired
    private CoberturaEstoqueService coberturaEstoqueService;

    @Test
    void versoesParalelasDevemRetornarExatamenteOMesmoResultadoDaSequencial() {
        List<BolsaEmEstoque> bolsas = GeradorDadosService.gerarBolsas(20_000);
        List<DemandaHospitalar> solicitacoes = GeradorDadosService.gerarSolicitacoes(501);

        List<CoberturaPorTipo> sequencial = coberturaEstoqueService.calcular(bolsas, solicitacoes, 1, Modo.SEQUENCIAL);
        assertTrue(sequencial.stream().mapToLong(CoberturaPorTipo::bolsasCompativeis).sum() > 0);

        for (int threads : new int[] {2, 3, 4, 8}) {
            assertEquals(sequencial, coberturaEstoqueService.calcular(bolsas, solicitacoes, threads, Modo.PLATAFORMA),
                    "Divergencia com " + threads + " threads de plataforma");
            assertEquals(sequencial, coberturaEstoqueService.calcular(bolsas, solicitacoes, threads, Modo.VIRTUAL),
                    "Divergencia com " + threads + " virtual threads");
        }
    }

    @Test
    void deveContarApenasBolsasDisponiveisValidasCompativeisEDentroDoRaio() {
        LocalDate hoje = GeradorDadosService.DATA_REFERENCIA;
        List<BolsaEmEstoque> bolsas = List.of(
                // contam: O- universal vencendo em 3 dias; A+ em Olinda (~7 km)
                bolsa(TipoSanguineo.O_NEGATIVO, TipoComponente.HEMACIAS, hoje.plusDays(3), StatusBolsa.DISPONIVEL,
                        LATITUDE_HOSPITAL, LONGITUDE_HOSPITAL),
                bolsa(TipoSanguineo.A_POSITIVO, TipoComponente.HEMACIAS, hoje.plusDays(20), StatusBolsa.DISPONIVEL,
                        -8.01, -34.85),
                // nao contam: incompativel, outro componente, vencida, alocada, em Sao Paulo (~2.100 km)
                bolsa(TipoSanguineo.B_POSITIVO, TipoComponente.HEMACIAS, hoje.plusDays(20), StatusBolsa.DISPONIVEL,
                        LATITUDE_HOSPITAL, LONGITUDE_HOSPITAL),
                bolsa(TipoSanguineo.A_POSITIVO, TipoComponente.PLASMA, hoje.plusDays(20), StatusBolsa.DISPONIVEL,
                        LATITUDE_HOSPITAL, LONGITUDE_HOSPITAL),
                bolsa(TipoSanguineo.A_POSITIVO, TipoComponente.HEMACIAS, hoje.minusDays(1), StatusBolsa.DISPONIVEL,
                        LATITUDE_HOSPITAL, LONGITUDE_HOSPITAL),
                bolsa(TipoSanguineo.A_POSITIVO, TipoComponente.HEMACIAS, hoje.plusDays(20), StatusBolsa.ALOCADA,
                        LATITUDE_HOSPITAL, LONGITUDE_HOSPITAL),
                bolsa(TipoSanguineo.A_POSITIVO, TipoComponente.HEMACIAS, hoje.plusDays(20), StatusBolsa.DISPONIVEL,
                        -23.55, -46.63));
        List<DemandaHospitalar> solicitacoes = List.of(new DemandaHospitalar("Hospital Teste",
                TipoSanguineo.A_POSITIVO, TipoComponente.HEMACIAS, 2, LATITUDE_HOSPITAL, LONGITUDE_HOSPITAL));

        CoberturaPorTipo aPositivo = coberturaEstoqueService.calcular(bolsas, solicitacoes, 1, Modo.SEQUENCIAL)
                .get(TipoSanguineo.A_POSITIVO.ordinal());

        assertEquals(new CoberturaPorTipo("A+", 1, 1, 2, 1), aPositivo);
    }

    @Test
    void relatorioDeveInformarModoETotais() {
        RelatorioCoberturaResponse relatorio = coberturaEstoqueService.gerarRelatorio(5_000, 100, 4, false);

        assertEquals("PLATAFORMA", relatorio.modo());
        assertEquals(100, relatorio.porTipo().stream().mapToLong(CoberturaPorTipo::solicitacoes).sum());
    }

    private BolsaEmEstoque bolsa(TipoSanguineo tipo, TipoComponente componente, LocalDate validade,
                                 StatusBolsa status, double latitude, double longitude) {
        return new BolsaEmEstoque(tipo, componente, validade, status, 0, latitude, longitude);
    }
}
