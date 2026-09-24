package com.jackops.rotavital.model.relatorio;

import java.time.LocalDate;

import com.jackops.rotavital.model.enums.StatusBolsa;
import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;

/**
 * Projecao somente leitura de uma bolsa para relatorios, incluindo a localizacao da unidade que a armazena.
 * A localizacao ainda nao faz parte da entidade {@code Bolsa}; ela entra com a historia de Roteirizacao.
 */
public record BolsaEmEstoque(TipoSanguineo tipoSanguineo, TipoComponente tipoComponente, LocalDate dataValidade,
        StatusBolsa status, int unidade, double latitude, double longitude) {
}
