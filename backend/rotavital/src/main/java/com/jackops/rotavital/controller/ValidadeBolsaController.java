package com.jackops.rotavital.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jackops.rotavital.dto.AlertaValidadeResponse;
import com.jackops.rotavital.dto.BaixaBolsaResponse;
import com.jackops.rotavital.dto.VerificacaoValidadeResponse;
import com.jackops.rotavital.service.ValidadeBolsaService;

@RestController
@RequestMapping("/api/validade")
public class ValidadeBolsaController {
    private final ValidadeBolsaService validadeBolsaService;

    public ValidadeBolsaController(ValidadeBolsaService validadeBolsaService) {
        this.validadeBolsaService = validadeBolsaService;
    }

    @GetMapping("/alertas")
    public List<AlertaValidadeResponse> listarAlertas() {
        return validadeBolsaService.listarAlertas();
    }

    @PostMapping("/verificar")
    public VerificacaoValidadeResponse verificar() {
        return validadeBolsaService.verificarValidade();
    }

    @PostMapping("/bolsas/{bolsaId}/baixa")
    public ResponseEntity<BaixaBolsaResponse> darBaixa(@PathVariable Long bolsaId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(validadeBolsaService.darBaixaPorVencimento(bolsaId));
    }

    @GetMapping("/baixas")
    public List<BaixaBolsaResponse> listarBaixas() {
        return validadeBolsaService.listarBaixas();
    }
}
