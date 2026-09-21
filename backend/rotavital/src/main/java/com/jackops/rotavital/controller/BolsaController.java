package com.jackops.rotavital.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jackops.rotavital.dto.BolsaResponse;
import com.jackops.rotavital.dto.CadastroBolsaRequest;
import com.jackops.rotavital.service.BolsaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bolsas")
public class BolsaController {
    private final BolsaService bolsaService;

    public BolsaController(BolsaService bolsaService) {
        this.bolsaService = bolsaService;
    }

    @PostMapping
    public ResponseEntity<BolsaResponse> cadastrar(@Valid @RequestBody CadastroBolsaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bolsaService.cadastrar(request));
    }

    @GetMapping
    public List<BolsaResponse> listarEstoque() {
        return bolsaService.listarEstoque();
    }
}
