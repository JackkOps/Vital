package com.jackops.rotavital.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.jackops.rotavital.dto.RelatorioCoberturaResponse;
import com.jackops.rotavital.service.CoberturaEstoqueService;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioCoberturaController {
    private static final int MAX_BOLSAS = 2_000_000;
    private static final int MAX_SOLICITACOES = 100_000;
    private static final int MAX_THREADS = 64;

    private final CoberturaEstoqueService coberturaEstoqueService;

    public RelatorioCoberturaController(CoberturaEstoqueService coberturaEstoqueService) {
        this.coberturaEstoqueService = coberturaEstoqueService;
    }

    @GetMapping("/cobertura")
    public RelatorioCoberturaResponse cobertura(
            @RequestParam(defaultValue = "100000") int bolsas,
            @RequestParam(defaultValue = "1000") int solicitacoes,
            @RequestParam(defaultValue = "1") int threads,
            @RequestParam(defaultValue = "false") boolean virtual) {
        validar(bolsas, 1, MAX_BOLSAS, "bolsas");
        validar(solicitacoes, 1, MAX_SOLICITACOES, "solicitacoes");
        validar(threads, 1, MAX_THREADS, "threads");
        return coberturaEstoqueService.gerarRelatorio(bolsas, solicitacoes, threads, virtual);
    }

    private void validar(int valor, int minimo, int maximo, String parametro) {
        if (valor < minimo || valor > maximo) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O parametro '" + parametro + "' deve estar entre " + minimo + " e " + maximo + ".");
        }
    }
}
