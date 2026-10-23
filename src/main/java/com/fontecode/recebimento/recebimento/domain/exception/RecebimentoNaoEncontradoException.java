package com.fontecode.recebimento.recebimento.domain.exception;

public class RecebimentoNaoEncontradoException extends RuntimeException {

    public RecebimentoNaoEncontradoException(Long id) {
        super("Recebimento não encontrado para o ID: " + id);
    }
}
