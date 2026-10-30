package com.jackops.rotavital.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.jackops.rotavital.dto.BolsaResponse;
import com.jackops.rotavital.dto.RespostaAlocacaoBolsa;
import com.jackops.rotavital.service.AlocacaoBolsaService;

@RestController
@RequestMapping("/api/alocacoes")
public class AlocacaoBolsaController {

    private final AlocacaoBolsaService alocacaoBolsaService;

    public AlocacaoBolsaController(AlocacaoBolsaService alocacaoBolsaService) {
        this.alocacaoBolsaService = alocacaoBolsaService;
    }

    /**
     * Aloca para a solicitação a bolsa compatível (ABO/Rh) que vence primeiro (FEFO).
     *
     * Respostas:
     * 200 - bolsa alocada;
     * 202 - nenhuma bolsa compatível disponível ({@code alocada = false});
     * 400 - {@code solicitacaoId} zero, negativo ou não numérico;
     * 404 - solicitação não encontrada ({@link com.jackops.rotavital.exception.RecursoNaoEncontradoException});
     * 409 - solicitação cancelada ou que já recebeu a quantidade pedida.
     * Os erros seguem o formato { "mensagem": "..." } do ApiExceptionHandler.
     */
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

    /**
     * Lista as bolsas alocadas para a solicitação, na ordem em que foram alocadas.
     */
    @GetMapping("/solicitacoes/{solicitacaoId}")
    public List<BolsaResponse> listarBolsasAlocadas(@PathVariable Long solicitacaoId) {
        return alocacaoBolsaService.listarBolsasAlocadas(solicitacaoId);
    }
}
