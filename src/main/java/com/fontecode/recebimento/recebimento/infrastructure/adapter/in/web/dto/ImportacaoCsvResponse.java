package com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "Resultado da importação do arquivo CSV de recebimentos")
public record ImportacaoCsvResponse(

        @Schema(description = "Quantidade total de registros importados e persistidos", example = "3")
        int totalImportados,

        @Schema(description = "Mapeamento de cada coluna do CSV para o tipo de dado identificado automaticamente")
        Map<String, String> tiposIdentificadosPorColuna,

        @Schema(description = "Lista de recebimentos persistidos no banco H2")
        List<RecebimentoResponse> recebimentos
) {
}
