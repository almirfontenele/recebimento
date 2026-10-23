package com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Valor total de recebimentos filtrado por período (data inicial e data final)")
public record ValorTotalPorPeriodoResponse(

        @Schema(description = "Data e hora inicial considerada no formato YYYY-MM-DD HH:mm:SS", example = "2026-10-21 00:00:00")
        String dataInicial,

        @Schema(description = "Data e hora final considerada no formato YYYY-MM-DD HH:mm:SS", example = "2026-10-22 23:59:59")
        String dataFinal,

        @Schema(description = "Valor total formatado no padrão 999990,99", example = "1000241,74")
        String valorTotal
) {
}
