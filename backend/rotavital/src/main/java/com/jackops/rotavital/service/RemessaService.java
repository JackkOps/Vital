package com.jackops.rotavital.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jackops.rotavital.dto.AlertaRemessaResponse;
import com.jackops.rotavital.dto.CadastroRemessaRequest;
import com.jackops.rotavital.dto.LeituraTemperaturaRequest;
import com.jackops.rotavital.dto.LeituraTemperaturaResponse;
import com.jackops.rotavital.dto.RemessaResponse;
import com.jackops.rotavital.dto.ResultadoLeituraResponse;
import com.jackops.rotavital.dto.VerificacaoSensoresResponse;
import com.jackops.rotavital.estrutura.PilhaHistorico;
import com.jackops.rotavital.exception.RecursoNaoEncontradoException;
import com.jackops.rotavital.exception.RegraDeNegocioException;
import com.jackops.rotavital.model.AlertaRemessa;
import com.jackops.rotavital.model.LeituraTemperatura;
import com.jackops.rotavital.model.Remessa;
import com.jackops.rotavital.model.Rota;
import com.jackops.rotavital.model.enums.CondicaoRemessa;
import com.jackops.rotavital.model.enums.StatusRemessa;
import com.jackops.rotavital.model.enums.StatusRota;
import com.jackops.rotavital.model.enums.TipoAlertaRemessa;
import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.repository.AlertaRemessaRepository;
import com.jackops.rotavital.repository.LeituraTemperaturaRepository;
import com.jackops.rotavital.repository.RemessaRepository;
import com.jackops.rotavital.repository.RotaRepository;

/**
 * História 6 - Monitoramento da cadeia fria durante o transporte. Recebe as
 * leituras simuladas do sensor, guarda o histórico e emite alertas de excursão
 * térmica, leituras perdidas e sensor sem sinal.
 */
@Service
public class RemessaService {

    private final RemessaRepository remessaRepository;
    private final LeituraTemperaturaRepository leituraRepository;
    private final AlertaRemessaRepository alertaRepository;
    private final RotaRepository rotaRepository;
    private final PilhaHistorico pilhaHistorico;
    private final int intervaloMaximoMinutos;

    public RemessaService(RemessaRepository remessaRepository, LeituraTemperaturaRepository leituraRepository,
            AlertaRemessaRepository alertaRepository, RotaRepository rotaRepository, PilhaHistorico pilhaHistorico,
            @Value("${rotavital.remessas.intervalo-maximo-minutos:10}") int intervaloMaximoMinutos) {
        this.remessaRepository = remessaRepository;
        this.leituraRepository = leituraRepository;
        this.alertaRepository = alertaRepository;
        this.rotaRepository = rotaRepository;
        this.pilhaHistorico = pilhaHistorico;
        this.intervaloMaximoMinutos = intervaloMaximoMinutos;
    }

    @Transactional
    public RemessaResponse despachar(CadastroRemessaRequest request) {
        Rota rota = null;
        if (request.rotaId() != null) {
            rota = rotaRepository.findById(request.rotaId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Rota não encontrada"));
            if (rota.getStatus() != StatusRota.CONFIRMADA) {
                throw new RegraDeNegocioException("Confirme a rota antes de despachar a remessa");
            }
        }
        Remessa remessa = remessaRepository.save(
                new Remessa(rota, request.tipoComponente(), request.sensorId().trim(), LocalDateTime.now()));
        pilhaHistorico.empilhar("Despacho da remessa " + remessa.getId());
        return paraResponse(remessa);
    }

    @Transactional
    public ResultadoLeituraResponse registrarLeitura(Long remessaId, LeituraTemperaturaRequest request) {
        return registrarLeitura(remessaId, request, LocalDateTime.now());
    }

    /**
     * Grava a leitura e avalia a faixa do componente. Um reenvio (mesmo leituraId)
     * não é gravado de novo: devolve a leitura original com {@code duplicada = true}.
     */
    @Transactional
    public ResultadoLeituraResponse registrarLeitura(Long remessaId, LeituraTemperaturaRequest request,
            LocalDateTime agora) {
        Remessa remessa = remessaRepository.travarPorId(remessaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Remessa não encontrada"));

        Optional<LeituraTemperatura> repetida = leituraRepository.findByLeituraId(request.leituraId());
        if (repetida.isPresent()) {
            if (!repetida.get().getRemessa().getId().equals(remessaId)) {
                throw new RegraDeNegocioException("leituraId já pertence a outra remessa");
            }
            return new ResultadoLeituraResponse(true, paraResponse(repetida.get()), remessa.getCondicao(), List.of());
        }
        if (remessa.getStatus() == StatusRemessa.ENTREGUE) {
            throw new RegraDeNegocioException("Remessa já foi entregue");
        }
        if (!remessa.getSensorId().equals(request.sensorId())) {
            throw new RegraDeNegocioException("Sensor não pertence a esta remessa");
        }

        TipoComponente componente = remessa.getTipoComponente();
        boolean dentroDaFaixa = componente.temperaturaSegura(request.temperatura());
        LeituraTemperatura leitura = leituraRepository.save(new LeituraTemperatura(remessa, request.leituraId(),
                request.sensorId(), request.sequencia(), request.temperatura(), request.dataHora(), agora,
                dentroDaFaixa));

        List<AlertaRemessaResponse> alertas = new ArrayList<>();
        Long ultima = remessa.getUltimaSequencia();
        if (ultima != null && request.sequencia() > ultima + 1) {
            long primeira = ultima + 1;
            long ultimaPerdida = request.sequencia() - 1;
            alertas.add(alertar(remessa, TipoAlertaRemessa.LEITURAS_PERDIDAS, primeira == ultimaPerdida
                    ? "Leitura de sequência " + primeira + " não recebida"
                    : "Leituras de sequência " + primeira + " a " + ultimaPerdida + " não recebidas", agora));
        }
        if (ultima == null || request.sequencia() > ultima) {
            remessa.setUltimaSequencia(request.sequencia());
        }

        if (!dentroDaFaixa && remessa.getCondicao() == CondicaoRemessa.CONFORME) {
            remessa.setCondicao(CondicaoRemessa.SOB_RISCO);
            alertas.add(alertar(remessa, TipoAlertaRemessa.EXCURSAO_TERMICA, String.format(Locale.forLanguageTag("pt-BR"),
                    "Excursão térmica: %.1f °C fora da faixa de %.1f °C a %.1f °C para %s",
                    request.temperatura(), componente.getTemperaturaMinima(), componente.getTemperaturaMaxima(),
                    componente.name()), agora));
        }

        remessa.setUltimaLeituraEm(agora);
        remessa.setSensorIndisponivel(false);
        remessaRepository.save(remessa);
        return new ResultadoLeituraResponse(false, paraResponse(leitura), remessa.getCondicao(), alertas);
    }

    @Transactional
    public VerificacaoSensoresResponse verificarSensores() {
        return verificarSensores(LocalDateTime.now());
    }

    /**
     * Marca como indisponível o sensor de cada remessa em trânsito que está sem
     * leitura além do intervalo máximo, e emite um alerta por falha.
     */
    @Transactional
    public VerificacaoSensoresResponse verificarSensores(LocalDateTime agora) {
        List<AlertaRemessaResponse> alertas = new ArrayList<>();
        for (Remessa remessa : remessaRepository.findByStatus(StatusRemessa.EM_TRANSITO)) {
            if (remessa.isSensorIndisponivel()) {
                continue;
            }
            LocalDateTime referencia = remessa.getUltimaLeituraEm() != null
                    ? remessa.getUltimaLeituraEm()
                    : remessa.getIniciadaEm();
            long semSinal = Duration.between(referencia, agora).toMinutes();
            if (semSinal > intervaloMaximoMinutos) {
                remessa.setSensorIndisponivel(true);
                remessaRepository.save(remessa);
                alertas.add(alertar(remessa, TipoAlertaRemessa.SENSOR_INDISPONIVEL,
                        "Falha de comunicação: sensor " + remessa.getSensorId() + " sem leitura há " + semSinal
                                + " min (máximo " + intervaloMaximoMinutos + " min)", agora));
            }
        }
        return new VerificacaoSensoresResponse(agora, alertas);
    }

    @Transactional
    public RemessaResponse entregar(Long id) {
        Remessa remessa = remessaRepository.travarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Remessa não encontrada"));
        if (remessa.getStatus() == StatusRemessa.ENTREGUE) {
            throw new RegraDeNegocioException("Remessa já foi entregue");
        }
        remessa.setStatus(StatusRemessa.ENTREGUE);
        remessa.setEntregueEm(LocalDateTime.now());
        pilhaHistorico.empilhar("Entrega da remessa " + remessa.getId());
        return paraResponse(remessaRepository.save(remessa));
    }

    @Transactional(readOnly = true)
    public List<RemessaResponse> listar() {
        return remessaRepository.findAllByOrderByIdDesc().stream().map(this::paraResponse).toList();
    }

    @Transactional(readOnly = true)
    public RemessaResponse buscar(Long id) {
        return remessaRepository.findById(id).map(this::paraResponse)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Remessa não encontrada"));
    }

    @Transactional(readOnly = true)
    public List<LeituraTemperaturaResponse> listarLeituras(Long remessaId) {
        if (!remessaRepository.existsById(remessaId)) {
            throw new RecursoNaoEncontradoException("Remessa não encontrada");
        }
        return leituraRepository.findByRemessaIdOrderBySequenciaAsc(remessaId).stream()
                .map(this::paraResponse)
                .toList();
    }

    private AlertaRemessaResponse alertar(Remessa remessa, TipoAlertaRemessa tipo, String mensagem,
            LocalDateTime agora) {
        return paraResponse(alertaRepository.save(new AlertaRemessa(remessa, tipo, mensagem, agora)));
    }

    private RemessaResponse paraResponse(Remessa remessa) {
        Double ultimaTemperatura = leituraRepository.findFirstByRemessaIdOrderByRecebidaEmDesc(remessa.getId())
                .map(LeituraTemperatura::getTemperatura)
                .orElse(null);
        return new RemessaResponse(
                remessa.getId(),
                remessa.getRota() == null ? null : remessa.getRota().getId(),
                remessa.getTipoComponente(),
                remessa.getTipoComponente().getTemperaturaMinima(),
                remessa.getTipoComponente().getTemperaturaMaxima(),
                remessa.getSensorId(),
                remessa.getStatus(),
                remessa.getCondicao(),
                remessa.isSensorIndisponivel(),
                remessa.getIniciadaEm(),
                remessa.getUltimaLeituraEm(),
                ultimaTemperatura,
                leituraRepository.countByRemessaId(remessa.getId()),
                remessa.getEntregueEm(),
                alertaRepository.findByRemessaIdOrderByIdAsc(remessa.getId()).stream().map(this::paraResponse).toList());
    }

    private LeituraTemperaturaResponse paraResponse(LeituraTemperatura leitura) {
        return new LeituraTemperaturaResponse(leitura.getId(), leitura.getLeituraId(), leitura.getSequencia(),
                leitura.getTemperatura(), leitura.getDataHora(), leitura.getRecebidaEm(), leitura.isDentroDaFaixa());
    }

    private AlertaRemessaResponse paraResponse(AlertaRemessa alerta) {
        return new AlertaRemessaResponse(alerta.getId(), alerta.getRemessa().getId(), alerta.getTipo(),
                alerta.getMensagem(), alerta.getCriadoEm());
    }
}
