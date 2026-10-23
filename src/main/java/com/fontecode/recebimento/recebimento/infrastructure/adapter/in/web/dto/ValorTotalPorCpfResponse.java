package com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Valor total de recebimentos filtrado por CPF")
public record ValorTotalPorCpfResponse(

        @Schema(description = "CPF consultado (somente dígitos)", example = "12345678901")
        String cpf,

        @Schema(description = "Valor total formatado no padrão 999990,99", example = "1000241,74")
        String valorTotal
) {
}
