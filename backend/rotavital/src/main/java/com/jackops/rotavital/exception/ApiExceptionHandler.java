package com.jackops.rotavital.exception;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> tratarValidacao(MethodArgumentNotValidException exception) {
        List<FieldError> erros = exception.getBindingResult().getFieldErrors();
        String mensagem = erros.isEmpty() ? "Requisição inválida" : erros.get(0).getDefaultMessage();
        return resposta(HttpStatus.BAD_REQUEST, mensagem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> tratarCorpoInvalido(HttpMessageNotReadableException exception) {
        return resposta(HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido: verifique o formato do JSON e os valores informados");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> tratarParametroInvalido(MethodArgumentTypeMismatchException exception) {
        return resposta(HttpStatus.BAD_REQUEST, "Valor inválido para o parâmetro: " + exception.getName());
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<Map<String, String>> tratarRegraDeNegocio(RegraDeNegocioException exception) {
        return resposta(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> tratarStatus(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .body(Collections.singletonMap("mensagem", exception.getReason()));
    }

    /**
     * Mantém o formato { "mensagem" } para os demais erros do Spring MVC (rota inexistente,
     * método ou tipo de conteúdo não suportado, parâmetro ausente), preservando o status de
     * cada um, e para falhas inesperadas, que viram 500 e são registradas no log.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> tratarDemais(Exception exception) {
        if (exception instanceof ErrorResponse erro) {
            HttpStatusCode status = erro.getStatusCode();
            if (status.is5xxServerError()) {
                LOGGER.error("Erro ao processar a requisição", exception);
            }
            return ResponseEntity.status(status)
                    .headers(erro.getHeaders())
                    .body(Collections.singletonMap("mensagem", mensagemPadrao(status)));
        }
        LOGGER.error("Erro inesperado ao processar a requisição", exception);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor");
    }

    private String mensagemPadrao(HttpStatusCode status) {
        return switch (status.value()) {
            case 404 -> "Recurso não encontrado";
            case 405 -> "Método HTTP não suportado para este endereço";
            case 415 -> "Tipo de conteúdo não suportado: envie application/json";
            default -> status.is4xxClientError() ? "Requisição inválida" : "Erro interno no servidor";
        };
    }

    private ResponseEntity<Map<String, String>> resposta(HttpStatus status, String mensagem) {
        Map<String, String> corpo = Collections.singletonMap("mensagem", mensagem);
        return ResponseEntity.status(status).body(corpo);
    }
}
