package com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web.exception;

import com.fontecode.recebimento.recebimento.domain.exception.CsvImportException;
import com.fontecode.recebimento.recebimento.domain.exception.DomainValidationException;
import com.fontecode.recebimento.recebimento.domain.exception.RecebimentoNaoEncontradoException;
import com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web.dto.ErroApiResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecebimentoNaoEncontradoException.class)
    public ResponseEntity<ErroApiResponse> handleNaoEncontrado(RecebimentoNaoEncontradoException ex) {
        return construirResposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({DomainValidationException.class, CsvImportException.class})
    public ResponseEntity<ErroApiResponse> handleValidacaoDominio(RuntimeException ex) {
        return construirResposta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroApiResponse> handleArgumentoInvalido(MethodArgumentNotValidException ex) {
        String mensagens = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(" "));
        return construirResposta(HttpStatus.BAD_REQUEST, mensagens);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroApiResponse> handleParametroAusente(MissingServletRequestParameterException ex) {
        return construirResposta(
                HttpStatus.BAD_REQUEST,
                "Parâmetro obrigatório ausente: '" + ex.getParameterName() + "'."
        );
    }

    private ResponseEntity<ErroApiResponse> construirResposta(HttpStatus status, String mensagem) {
        ErroApiResponse erro = new ErroApiResponse(status.value(), mensagem, LocalDateTime.now());
        return ResponseEntity.status(status).body(erro);
    }
}
