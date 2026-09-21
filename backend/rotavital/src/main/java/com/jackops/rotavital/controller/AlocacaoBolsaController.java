package com.jackops.rotavital.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        try {
            RespostaAlocacaoBolsa resposta = alocacaoBolsaService.alocarBolsaCompativel(solicitacaoId);
            if (resposta.isAlocada()) {
                return ResponseEntity.ok(resposta);
            }
            return ResponseEntity.accepted().body(resposta);
        } catch (IllegalArgumentException excecao) {
            return ResponseEntity.badRequest()
                    .body(new RespostaAlocacaoBolsa(null, false, excecao.getMessage()));
        }
    }
}
