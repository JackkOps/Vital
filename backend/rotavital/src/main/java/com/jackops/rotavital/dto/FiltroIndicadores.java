package com.jackops.rotavital.dto;

import java.time.LocalDate;

import com.jackops.rotavital.model.enums.TipoComponente;

/**
 * Filtros do painel. Todos são opcionais: sem datas vale o período padrão e,
 * sem hospital ou componente, entram todos.
 */
public record FiltroIndicadores(
    LocalDate dataInicio,
    LocalDate dataFim,
    String hospital,
    TipoComponente tipoComponente) {

    public static FiltroIndicadores semFiltros() {
        return new FiltroIndicadores(null, null, null, null);
    }
}
