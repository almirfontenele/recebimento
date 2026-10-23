package com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação de um recebimento")
public record RecebimentoResponse(

        @Schema(description = "Identificador único do recebimento", example = "1")
        Long id,

        @Schema(description = "CPF do titular (somente dígitos)", example = "12345678901")
        String cpf,

        @Schema(description = "Identificador End-to-End (E2E) da transação", example = "E1234567820261021143000000000001")
        String e2e,

        @Schema(description = "Data e hora no formato YYYY-MM-DD HH:mm:SS", example = "2026-10-21 14:30:00")
        String data,

        @Schema(description = "Valor formatado no padrão 999990,99", example = "999990,99")
        String valor
) {
}
