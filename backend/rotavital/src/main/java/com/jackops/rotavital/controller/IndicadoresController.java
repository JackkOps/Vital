package com.jackops.rotavital.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jackops.rotavital.dto.IndicadoresResponse;
import com.jackops.rotavital.service.IndicadoresService;

@RestController
@RequestMapping("/api/indicadores")
public class IndicadoresController {

    private final IndicadoresService indicadoresService;

    public IndicadoresController(IndicadoresService indicadoresService) {
        this.indicadoresService = indicadoresService;
    }

    @GetMapping
    public IndicadoresResponse consultar() {
        return indicadoresService.consultar();
    }
}