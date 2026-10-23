package com.fontecode.recebimento.recebimento.domain.port.in;

import com.fontecode.recebimento.recebimento.domain.model.ResultadoImportacaoCsv;

import java.io.InputStream;

public interface ImportarRecebimentosCsvUseCase {

    ResultadoImportacaoCsv importar(InputStream csvInputStream);
}
