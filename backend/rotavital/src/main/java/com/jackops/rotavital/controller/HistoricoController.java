package com.jackops.rotavital.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jackops.rotavital.dto.OperacaoHistoricoResponse;
import com.jackops.rotavital.service.HistoricoService;

@RestController
@RequestMapping("/api/historico")
public class HistoricoController {
    private final HistoricoService historicoService;

    public HistoricoController(HistoricoService historicoService) {
        this.historicoService = historicoService;
    }

    @GetMapping
    public List<OperacaoHistoricoResponse> listar() {
        return historicoService.listar();
    }

    @DeleteMapping("/ultima")
    public OperacaoHistoricoResponse removerUltima() {
        return historicoService.removerUltima();
    }
}
