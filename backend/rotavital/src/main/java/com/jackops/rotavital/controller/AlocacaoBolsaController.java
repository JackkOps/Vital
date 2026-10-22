package com.jackops.rotavital.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.jackops.rotavital.dto.RespostaAlocacaoBolsa;
import com.jackops.rotavital.service.AlocacaoBolsaService;

@RestController
@RequestMapping("/api/alocacoes")
public class AlocacaoBolsaController {

    private final AlocacaoBolsaService alocacaoBolsaService;

    public AlocacaoBolsaController(AlocacaoBolsaService alocacaoBolsaService) {
        this.alocacaoBolsaService = alocacaoBolsaService;
    }

    @PostMapping("/solicitacoes/{solicitacaoId}/alocar")
    public ResponseEntity<RespostaAlocacaoBolsa> alocar(@PathVariable Long solicitacaoId) {
        if (solicitacaoId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Identificador da solicitação inválido: informe um número inteiro maior que zero");
        }

        RespostaAlocacaoBolsa resposta = alocacaoBolsaService.alocarBolsaCompativel(solicitacaoId);
        if (resposta.isAlocada()) {
            return ResponseEntity.ok(resposta);
        }
        return ResponseEntity.accepted().body(resposta);
    }
}
