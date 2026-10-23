package com.fontecode.recebimento.recebimento.application.parser;

import com.fontecode.recebimento.recebimento.domain.exception.CsvImportException;
import com.fontecode.recebimento.recebimento.domain.model.ResultadoImportacaoCsv;
import com.fontecode.recebimento.recebimento.domain.model.TipoDadoCsv;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvRecebimentoParserTest {

    private CsvRecebimentoParser parser;

    @BeforeEach
    void setUp() {
        parser = new CsvRecebimentoParser();
    }

    @Test
    @DisplayName("Deve identificar automaticamente os tipos de dados mesmo com colunas fora da ordem padrão")
    void deveIdentificarTiposComColunasForaDeOrdem() {
        String csv = """
                valor_recebido;momento;documento;e2e
                999990,99;2026-10-21 14:30:00;12345678901;E1234567820261021143000000000001
                1250,50;2026-10-22 08:15:30;987.654.321-00;E1234567820261022081530000000002
                """;

        ResultadoImportacaoCsv resultado = parser.parse(
                new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8))
        );

        assertEquals(2, resultado.totalImportados());
        assertEquals(4, resultado.tiposIdentificadosPorColuna().size());
        assertTrue(resultado.tiposIdentificadosPorColuna().get("coluna_1 (valor_recebido)").startsWith(TipoDadoCsv.VALOR.name()));
        assertTrue(resultado.tiposIdentificadosPorColuna().get("coluna_2 (momento)").startsWith(TipoDadoCsv.DATA.name()));
        assertTrue(resultado.tiposIdentificadosPorColuna().get("coluna_3 (documento)").startsWith(TipoDadoCsv.CPF.name()));
        assertTrue(resultado.tiposIdentificadosPorColuna().get("coluna_4 (e2e)").startsWith(TipoDadoCsv.E2E.name()));

        assertEquals("12345678901", resultado.recebimentos().get(0).getCpf());
        assertEquals("E1234567820261021143000000000001", resultado.recebimentos().get(0).getE2e());
        assertEquals("2026-10-21 14:30:00", resultado.recebimentos().get(0).getDataFormatada());
        assertEquals("999990,99", resultado.recebimentos().get(0).getValorFormatado());

        assertEquals("98765432100", resultado.recebimentos().get(1).getCpf());
        assertEquals("E1234567820261022081530000000002", resultado.recebimentos().get(1).getE2e());
        assertEquals("1250,50", resultado.recebimentos().get(1).getValorFormatado());
    }

    @Test
    @DisplayName("Deve importar CSV sem cabeçalho e separado por vírgula com valor no formato 999990,99")
    void deveImportarCsvSemCabecalhoSeparadoPorVirgula() {
        String csv = "12345678901,E1234567820261021143000000000001,2026-10-21 14:30:00,999990,99\n";

        ResultadoImportacaoCsv resultado = parser.parse(
                new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8))
        );

        assertEquals(1, resultado.totalImportados());
        assertEquals("12345678901", resultado.recebimentos().get(0).getCpf());
        assertEquals("E1234567820261021143000000000001", resultado.recebimentos().get(0).getE2e());
        assertEquals("2026-10-21 14:30:00", resultado.recebimentos().get(0).getDataFormatada());
        assertEquals("999990,99", resultado.recebimentos().get(0).getValorFormatado());
    }

    @Test
    @DisplayName("Deve importar CSV sem cabeçalho com valor inteiro sem casas decimais")
    void deveImportarCsvSemCabecalhoComValorInteiro() {
        String csv = "057.431.730-95;E00000000202610212221T3D9YCBFKY8;2026-01-08 02:22:00;446\n";

        ResultadoImportacaoCsv resultado = parser.parse(
                new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8))
        );

        assertEquals(1, resultado.totalImportados());
        assertEquals("05743173095", resultado.recebimentos().get(0).getCpf());
        assertEquals("E00000000202610212221T3D9YCBFKY8", resultado.recebimentos().get(0).getE2e());
        assertEquals("2026-01-08 02:22:00", resultado.recebimentos().get(0).getDataFormatada());
        assertEquals("446,00", resultado.recebimentos().get(0).getValorFormatado());
    }

    @Test
    @DisplayName("Deve lançar exceção quando um tipo de dado não puder ser identificado")
    void deveLancarExcecaoParaDadoDesconhecido() {
        String csv = "12345678901;E1234567820261021143000000000001;DATA_INVALIDA;999990,99";

        assertThrows(CsvImportException.class, () ->
                parser.parse(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)))
        );
    }
}
