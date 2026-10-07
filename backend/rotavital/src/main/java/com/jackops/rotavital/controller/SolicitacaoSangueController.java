package com.jackops.rotavital.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jackops.rotavital.dto.AtualizacaoSolicitacaoRequest;
import com.jackops.rotavital.dto.CadastroSolicitacaoRequest;
import com.jackops.rotavital.dto.SolicitacaoResponse;
import com.jackops.rotavital.service.SolicitacaoSangueService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/solicitacoes")
public class SolicitacaoSangueController {
    private final SolicitacaoSangueService solicitacaoService;

    public SolicitacaoSangueController(SolicitacaoSangueService solicitacaoService) {
        this.solicitacaoService = solicitacaoService;
    }

    @PostMapping
    public ResponseEntity<SolicitacaoResponse> cadastrar(@Valid @RequestBody CadastroSolicitacaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitacaoService.cadastrar(request));
    }

    @GetMapping
    public List<SolicitacaoResponse> listar() {
        return solicitacaoService.listar();
    }

    @GetMapping("/{id}")
    public SolicitacaoResponse buscarPorId(@PathVariable Long id) {
        return solicitacaoService.buscarPorId(id);
    }

    @GetMapping("/fila")
    public List<SolicitacaoResponse> listarFila() {
        return solicitacaoService.listarFila();
    }

    @PostMapping("/fila/proxima")
    public SolicitacaoResponse chamarProximaDaFila() {
        return solicitacaoService.chamarProximaDaFila();
    }

    @PutMapping("/{id}")
    public SolicitacaoResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AtualizacaoSolicitacaoRequest request) {

        return solicitacaoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        solicitacaoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
