package com.jackops.rotavital.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.jackops.rotavital.dto.CadastroRemessaRequest;
import com.jackops.rotavital.dto.CalculoRotaRequest;
import com.jackops.rotavital.dto.LeituraTemperaturaRequest;
import com.jackops.rotavital.dto.RemessaResponse;
import com.jackops.rotavital.dto.ResultadoLeituraResponse;
import com.jackops.rotavital.exception.RecursoNaoEncontradoException;
import com.jackops.rotavital.exception.RegraDeNegocioException;
import com.jackops.rotavital.model.enums.CondicaoRemessa;
import com.jackops.rotavital.model.enums.StatusRemessa;
import com.jackops.rotavital.model.enums.TipoAlertaRemessa;
import com.jackops.rotavital.model.enums.TipoComponente;

@SpringBootTest
@Transactional
class RemessaServiceTest {

    @Autowired
    private RemessaService remessaService;

    @Autowired
    private RotaService rotaService;

    @Test
    void leituraDentroDaFaixaFicaNoHistoricoEARemessaContinuaConforme() {
        RemessaResponse remessa = despachar(TipoComponente.HEMACIAS);

        ResultadoLeituraResponse resultado = ler(remessa, "L-1", 1, 4.5);

        assertFalse(resultado.duplicada());
        assertTrue(resultado.leitura().dentroDaFaixa());
        assertEquals(CondicaoRemessa.CONFORME, resultado.condicaoRemessa());
        assertTrue(resultado.alertasGerados().isEmpty());
        assertEquals(1, remessaService.listarLeituras(remessa.id()).size());
    }

    @Test
    void excursaoTermicaMarcaSobRiscoEAlertaUmaVez() {
        RemessaResponse remessa = despachar(TipoComponente.HEMACIAS);

        ResultadoLeituraResponse primeira = ler(remessa, "L-1", 1, 12.3);
        ResultadoLeituraResponse segunda = ler(remessa, "L-2", 2, 13.0);
        ResultadoLeituraResponse normal = ler(remessa, "L-3", 3, 5.0);

        assertEquals(CondicaoRemessa.SOB_RISCO, primeira.condicaoRemessa());
        assertEquals(TipoAlertaRemessa.EXCURSAO_TERMICA, primeira.alertasGerados().get(0).tipo());
        assertTrue(primeira.alertasGerados().get(0).mensagem().contains("12,3 °C"));
        assertTrue(segunda.alertasGerados().isEmpty());
        // Uma volta à faixa não desfaz a excursão: a carga continua sob risco.
        assertEquals(CondicaoRemessa.SOB_RISCO, normal.condicaoRemessa());
        assertEquals(1, remessaService.buscar(remessa.id()).alertas().size());
    }

    @Test
    void faixaDependeDoComponente() {
        RemessaResponse plaquetas = despachar(TipoComponente.PLAQUETAS);
        RemessaResponse plasma = despachar(TipoComponente.PLASMA);

        assertTrue(ler(plaquetas, "P-1", 1, 22.0).leitura().dentroDaFaixa());
        assertFalse(ler(plaquetas, "P-2", 2, 4.0).leitura().dentroDaFaixa());
        assertTrue(ler(plasma, "F-1", 1, -25.0).leitura().dentroDaFaixa());
        assertFalse(ler(plasma, "F-2", 2, -10.0).leitura().dentroDaFaixa());
    }

    @Test
    void reenvioDoMesmoLeituraIdNaoEGravadoDeNovo() {
        RemessaResponse remessa = despachar(TipoComponente.HEMACIAS);
        ler(remessa, "L-1", 1, 4.0);

        ResultadoLeituraResponse reenvio = ler(remessa, "L-1", 1, 4.0);

        assertTrue(reenvio.duplicada());
        assertEquals(1, remessaService.listarLeituras(remessa.id()).size());
    }

    @Test
    void saltoNaSequenciaGeraAlertaDeLeiturasPerdidas() {
        RemessaResponse remessa = despachar(TipoComponente.HEMACIAS);
        ler(remessa, "L-41", 41, 4.0);

        ResultadoLeituraResponse resultado = ler(remessa, "L-44", 44, 4.0);

        assertEquals(TipoAlertaRemessa.LEITURAS_PERDIDAS, resultado.alertasGerados().get(0).tipo());
        assertEquals("Leituras de sequência 42 a 43 não recebidas", resultado.alertasGerados().get(0).mensagem());
    }

    @Test
    void sensorSemLeituraAlemDoIntervaloGeraFalhaDeComunicacao() {
        RemessaResponse remessa = despachar(TipoComponente.HEMACIAS);
        LocalDateTime agora = LocalDateTime.now();
        remessaService.registrarLeitura(remessa.id(), leitura("L-1", 1, 4.0), agora);

        var dentroDoPrazo = remessaService.verificarSensores(agora.plusMinutes(10));
        var foraDoPrazo = remessaService.verificarSensores(agora.plusMinutes(11));
        var deNovo = remessaService.verificarSensores(agora.plusMinutes(20));

        assertTrue(dentroDoPrazo.alertasGerados().stream().noneMatch(a -> a.remessaId().equals(remessa.id())));
        assertEquals(1, foraDoPrazo.alertasGerados().stream().filter(a -> a.remessaId().equals(remessa.id())).count());
        assertEquals(TipoAlertaRemessa.SENSOR_INDISPONIVEL, foraDoPrazo.alertasGerados().get(0).tipo());
        assertTrue(deNovo.alertasGerados().stream().noneMatch(a -> a.remessaId().equals(remessa.id())));
        assertTrue(remessaService.buscar(remessa.id()).sensorIndisponivel());

        // A leitura seguinte mostra que o sensor voltou.
        remessaService.registrarLeitura(remessa.id(), leitura("L-2", 2, 4.0), agora.plusMinutes(21));
        assertFalse(remessaService.buscar(remessa.id()).sensorIndisponivel());
    }

    @Test
    void remessaEntregueNaoRecebeLeiturasNemEntregaDuasVezes() {
        RemessaResponse remessa = despachar(TipoComponente.HEMACIAS);

        RemessaResponse entregue = remessaService.entregar(remessa.id());

        assertEquals(StatusRemessa.ENTREGUE, entregue.status());
        assertThrows(RegraDeNegocioException.class, () -> ler(remessa, "L-1", 1, 4.0));
        assertThrows(RegraDeNegocioException.class, () -> remessaService.entregar(remessa.id()));
    }

    @Test
    void deveRecusarLeituraDeOutroSensor() {
        RemessaResponse remessa = despachar(TipoComponente.HEMACIAS);

        assertThrows(RegraDeNegocioException.class, () -> remessaService.registrarLeitura(remessa.id(),
                new LeituraTemperaturaRequest("X-1", "SENSOR-INTRUSO", 1L, 4.0, LocalDateTime.now())));
    }

    @Test
    void soDespachaComRotaConfirmada() {
        var comRisco = rotaService.calcular(new CalculoRotaRequest(1L, List.of(9L), null, null));
        var confirmada = rotaService.calcular(new CalculoRotaRequest(1L, List.of(3L), null, null));

        assertThrows(RegraDeNegocioException.class, () -> remessaService.despachar(
                new CadastroRemessaRequest(comRisco.id(), TipoComponente.HEMACIAS, "SENSOR-1")));
        assertEquals(confirmada.id(), remessaService.despachar(
                new CadastroRemessaRequest(confirmada.id(), TipoComponente.HEMACIAS, "SENSOR-1")).rotaId());
        assertThrows(RecursoNaoEncontradoException.class, () -> remessaService.despachar(
                new CadastroRemessaRequest(999L, TipoComponente.HEMACIAS, "SENSOR-1")));
    }

    private RemessaResponse despachar(TipoComponente componente) {
        return remessaService.despachar(new CadastroRemessaRequest(null, componente, "SENSOR-1"));
    }

    private ResultadoLeituraResponse ler(RemessaResponse remessa, String leituraId, long sequencia, double temperatura) {
        return remessaService.registrarLeitura(remessa.id(), leitura(leituraId, sequencia, temperatura));
    }

    private LeituraTemperaturaRequest leitura(String leituraId, long sequencia, double temperatura) {
        return new LeituraTemperaturaRequest(leituraId, "SENSOR-1", sequencia, temperatura, LocalDateTime.now());
    }
}
