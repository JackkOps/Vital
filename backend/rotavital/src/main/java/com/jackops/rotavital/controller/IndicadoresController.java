package com.jackops.rotavital.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jackops.rotavital.dto.FiltroIndicadores;
import com.jackops.rotavital.dto.IndicadoresResponse;
import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.service.IndicadoresService;

@RestController
@RequestMapping("/api/indicadores")
public class IndicadoresController {

    private final IndicadoresService indicadoresService;

    public IndicadoresController(IndicadoresService indicadoresService) {
        this.indicadoresService = indicadoresService;
    }

    /**
     * Painel de indicadores. Filtros opcionais: dataInicio e dataFim (AAAA-MM-DD),
     * hospital e tipoComponente.
     */
    @GetMapping
    public IndicadoresResponse consultar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String hospital,
            @RequestParam(required = false) TipoComponente tipoComponente) {
        return indicadoresService.consultar(new FiltroIndicadores(dataInicio, dataFim, hospital, tipoComponente));
    }
}
