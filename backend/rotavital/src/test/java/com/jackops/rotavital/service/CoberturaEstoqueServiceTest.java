package com.jackops.rotavital.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.jackops.rotavital.dto.CoberturaPorTipo;
import com.jackops.rotavital.dto.RelatorioCoberturaResponse;
import com.jackops.rotavital.model.Bolsa;
import com.jackops.rotavital.model.SolicitacaoSangue;
import com.jackops.rotavital.model.enums.StatusBolsa;
import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;
import com.jackops.rotavital.service.CoberturaEstoqueService.Modo;

@SpringBootTest
class CoberturaEstoqueServiceTest {

    @Autowired
    private CoberturaEstoqueService coberturaEstoqueService;

    @Test
    void versoesParalelasDevemRetornarExatamenteOMesmoResultadoDaSequencial() {
        List<Bolsa> bolsas = GeradorDadosService.gerarBolsas(20_000);
        List<SolicitacaoSangue> solicitacoes = GeradorDadosService.gerarSolicitacoes(501);

        List<CoberturaPorTipo> sequencial = coberturaEstoqueService.calcular(bolsas, solicitacoes, 1, Modo.SEQUENCIAL);

        for (int threads : new int[] {2, 3, 4, 8}) {
            assertEquals(sequencial, coberturaEstoqueService.calcular(bolsas, solicitacoes, threads, Modo.PLATAFORMA),
                    "Divergencia com " + threads + " threads de plataforma");
            assertEquals(sequencial, coberturaEstoqueService.calcular(bolsas, solicitacoes, threads, Modo.VIRTUAL),
                    "Divergencia com " + threads + " virtual threads");
        }
    }

    @Test
    void deveContarApenasBolsasDisponiveisValidasDoMesmoComponenteECompativeis() {
        LocalDate hoje = GeradorDadosService.DATA_REFERENCIA;
        List<Bolsa> bolsas = List.of(
                bolsa(TipoSanguineo.O_NEGATIVO, TipoComponente.HEMACIAS, hoje.plusDays(3), StatusBolsa.DISPONIVEL),
                bolsa(TipoSanguineo.A_POSITIVO, TipoComponente.HEMACIAS, hoje.plusDays(20), StatusBolsa.DISPONIVEL),
                bolsa(TipoSanguineo.B_POSITIVO, TipoComponente.HEMACIAS, hoje.plusDays(20), StatusBolsa.DISPONIVEL),
                bolsa(TipoSanguineo.A_POSITIVO, TipoComponente.PLASMA, hoje.plusDays(20), StatusBolsa.DISPONIVEL),
                bolsa(TipoSanguineo.A_POSITIVO, TipoComponente.HEMACIAS, hoje.minusDays(1), StatusBolsa.DISPONIVEL),
                bolsa(TipoSanguineo.A_POSITIVO, TipoComponente.HEMACIAS, hoje.plusDays(20), StatusBolsa.ALOCADA));
        List<SolicitacaoSangue> solicitacoes = List.of(SolicitacaoSangue.builder()
                .id(1L)
                .nomeHospital("Hospital Teste")
                .tipoSanguineo(TipoSanguineo.A_POSITIVO)
                .tipoComponente(TipoComponente.HEMACIAS)
                .quantidade(2)
                .build());

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

    private Bolsa bolsa(TipoSanguineo tipo, TipoComponente componente, LocalDate validade, StatusBolsa status) {
        return Bolsa.builder()
                .tipoSanguineo(tipo)
                .tipoComponente(componente)
                .dataColeta(validade.minusDays(30))
                .dataValidade(validade)
                .volume(450)
                .status(status)
                .build();
    }
}
