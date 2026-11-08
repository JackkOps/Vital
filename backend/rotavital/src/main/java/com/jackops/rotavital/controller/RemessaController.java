package com.jackops.rotavital.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jackops.rotavital.dto.CadastroRemessaRequest;
import com.jackops.rotavital.dto.LeituraTemperaturaRequest;
import com.jackops.rotavital.dto.LeituraTemperaturaResponse;
import com.jackops.rotavital.dto.RemessaResponse;
import com.jackops.rotavital.dto.ResultadoLeituraResponse;
import com.jackops.rotavital.dto.VerificacaoSensoresResponse;
import com.jackops.rotavital.service.RemessaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/remessas")
public class RemessaController {
    private final RemessaService remessaService;

    public RemessaController(RemessaService remessaService) {
        this.remessaService = remessaService;
    }

    @PostMapping
    public ResponseEntity<RemessaResponse> despachar(@Valid @RequestBody CadastroRemessaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(remessaService.despachar(request));
    }

    @GetMapping
    public List<RemessaResponse> listar() {
        return remessaService.listar();
    }

    @GetMapping("/{id}")
    public RemessaResponse buscar(@PathVariable Long id) {
        return remessaService.buscar(id);
    }

    /**
     * Recebe uma leitura de telemetria (simulada). 201 quando gravada, 200 quando é
     * reenvio de uma leitura já recebida (duplicada = true).
     */
    @PostMapping("/{id}/leituras")
    public ResponseEntity<ResultadoLeituraResponse> registrarLeitura(@PathVariable Long id,
            @Valid @RequestBody LeituraTemperaturaRequest request) {
        ResultadoLeituraResponse resultado = remessaService.registrarLeitura(id, request);
        return ResponseEntity.status(resultado.duplicada() ? HttpStatus.OK : HttpStatus.CREATED).body(resultado);
    }

    @GetMapping("/{id}/leituras")
    public List<LeituraTemperaturaResponse> listarLeituras(@PathVariable Long id) {
        return remessaService.listarLeituras(id);
    }

    @PostMapping("/{id}/entregar")
    public RemessaResponse entregar(@PathVariable Long id) {
        return remessaService.entregar(id);
    }

    /**
     * Executa agora a verificação de sensores sem sinal (também roda a cada minuto).
     */
    @PostMapping("/verificar-sensores")
    public VerificacaoSensoresResponse verificarSensores() {
        return remessaService.verificarSensores();
    }
}
