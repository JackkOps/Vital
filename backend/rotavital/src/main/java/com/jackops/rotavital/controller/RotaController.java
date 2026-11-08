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

import com.jackops.rotavital.dto.CalculoRotaRequest;
import com.jackops.rotavital.dto.RedeDistribuicaoResponse;
import com.jackops.rotavital.dto.RotaResponse;
import com.jackops.rotavital.service.RotaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/rotas")
public class RotaController {
    private final RotaService rotaService;

    public RotaController(RotaService rotaService) {
        this.rotaService = rotaService;
    }

    /**
     * Locais (vértices) e trechos (arestas) da rede de distribuição.
     */
    @GetMapping("/rede")
    public RedeDistribuicaoResponse rede() {
        return rotaService.rede();
    }

    /**
     * Calcula a rota de menor tempo. Respostas:
     * 201 - rota calculada (status CONFIRMADA ou AGUARDANDO_CONFIRMACAO, com o alerta de atraso);
     * 400 - sem destino, destinos demais ou janela inválida;
     * 404 - origem, destino ou solicitação inexistente;
     * 409 - solicitação cancelada ou hospital fora da rede;
     * 422 - não existe caminho no grafo até algum destino.
     */
    @PostMapping
    public ResponseEntity<RotaResponse> calcular(@Valid @RequestBody CalculoRotaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rotaService.calcular(request));
    }

    @GetMapping
    public List<RotaResponse> listar() {
        return rotaService.listar();
    }

    @GetMapping("/{id}")
    public RotaResponse buscar(@PathVariable Long id) {
        return rotaService.buscar(id);
    }

    /**
     * Operador assume o risco de atraso e confirma a rota.
     */
    @PostMapping("/{id}/confirmar")
    public RotaResponse confirmar(@PathVariable Long id) {
        return rotaService.confirmar(id);
    }
}
