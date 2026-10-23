package com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados de entrada para criação ou atualização de um recebimento")
public record RecebimentoRequest(

        @Schema(description = "CPF do titular (11 dígitos ou formatado)", example = "12345678901")
        @NotBlank(message = "O CPF é obrigatório.")
        String cpf,

        @Schema(description = "Identificador End-to-End (E2E) da transação", example = "E1234567820261021143000000000001")
        @NotBlank(message = "O código E2E é obrigatório.")
        String e2e,

        @Schema(description = "Data e hora do recebimento no formato YYYY-MM-DD HH:mm:SS", example = "2026-10-21 14:30:00")
        @NotBlank(message = "A data é obrigatória no formato YYYY-MM-DD HH:mm:SS.")
        String data,

        @Schema(description = "Valor do recebimento no formato 999990,99", example = "999990,99")
        @NotBlank(message = "O valor é obrigatório no formato 999990,99.")
        String valor
) {
}
