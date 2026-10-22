package com.jackops.rotavital.exception;

/**
 * Indica que o recurso solicitado não existe. É respondida pela API com 404.
 */
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
