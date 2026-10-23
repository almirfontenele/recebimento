package com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Estrutura padrão de erro da API")
public record ErroApiResponse(

        @Schema(description = "Código HTTP de status", example = "400")
        int status,

        @Schema(description = "Mensagem descritiva do erro", example = "Dados inválidos na requisição.")
        String mensagem,

        @Schema(description = "Data e hora da ocorrência do erro")
        LocalDateTime timestamp
) {
}
