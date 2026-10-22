package com.jackops.rotavital.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import com.jackops.rotavital.dto.CadastroSolicitacaoRequest;
import com.jackops.rotavital.model.enums.NivelUrgencia;
import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

class SolicitacaoValidationTest {
    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();

    @AfterAll
    static void fecharValidador() { FACTORY.close(); }

    @ParameterizedTest(name = "rejeita {1}: {0}")
    @MethodSource("dadosInvalidos")
    void rejeitaCampoObrigatorioOuQuantidadeInvalida(CadastroSolicitacaoRequest request, String campo) {
        var campos = FACTORY.getValidator().validate(request).stream()
                .map(violacao -> violacao.getPropertyPath().toString()).collect(Collectors.toSet());
        assertEquals(Set.of(campo), campos);
    }

    static Stream<Arguments> dadosInvalidos() {
        return Stream.of(
                Arguments.of(request(null, TipoSanguineo.O_POSITIVO, TipoComponente.HEMACIAS, 1, NivelUrgencia.ELETIVA), "nomeHospital"),
                Arguments.of(request("", TipoSanguineo.O_POSITIVO, TipoComponente.HEMACIAS, 1, NivelUrgencia.ELETIVA), "nomeHospital"),
                Arguments.of(request("  ", TipoSanguineo.O_POSITIVO, TipoComponente.HEMACIAS, 1, NivelUrgencia.ELETIVA), "nomeHospital"),
                Arguments.of(request("Hospital", null, TipoComponente.HEMACIAS, 1, NivelUrgencia.ELETIVA), "tipoSanguineo"),
                Arguments.of(request("Hospital", TipoSanguineo.O_POSITIVO, null, 1, NivelUrgencia.ELETIVA), "tipoComponente"),
                Arguments.of(request("Hospital", TipoSanguineo.O_POSITIVO, TipoComponente.HEMACIAS, null, NivelUrgencia.ELETIVA), "quantidade"),
                Arguments.of(request("Hospital", TipoSanguineo.O_POSITIVO, TipoComponente.HEMACIAS, 0, NivelUrgencia.ELETIVA), "quantidade"),
                Arguments.of(request("Hospital", TipoSanguineo.O_POSITIVO, TipoComponente.HEMACIAS, -1, NivelUrgencia.ELETIVA), "quantidade"),
                Arguments.of(request("Hospital", TipoSanguineo.O_POSITIVO, TipoComponente.HEMACIAS, 1, null), "nivelUrgencia"));
    }

    @Test
    void aceitaSolicitacaoValida() {
        assertTrue(FACTORY.getValidator().validate(request("Hospital", TipoSanguineo.O_POSITIVO,
                TipoComponente.HEMACIAS, 1, NivelUrgencia.ELETIVA)).isEmpty());
    }

    private static CadastroSolicitacaoRequest request(String hospital, TipoSanguineo tipo,
            TipoComponente componente, Integer quantidade, NivelUrgencia urgencia) {
        return new CadastroSolicitacaoRequest(hospital, tipo, componente, quantidade, urgencia);
    }
}
