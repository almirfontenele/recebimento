package com.fontecode.recebimento.recebimento.domain.model;

import java.util.List;
import java.util.Map;

public record ResultadoImportacaoCsv(
        int totalImportados,
        Map<String, String> tiposIdentificadosPorColuna,
        List<Recebimento> recebimentos
) {
}
