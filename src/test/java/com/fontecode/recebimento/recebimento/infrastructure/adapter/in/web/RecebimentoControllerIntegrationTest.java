package com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web;

import com.fontecode.recebimento.recebimento.domain.exception.DomainValidationException;
import com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web.dto.ImportacaoCsvResponse;
import com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web.dto.RecebimentoRequest;
import com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web.dto.RecebimentoResponse;
import com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web.dto.ValorTotalPorCpfResponse;
import com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web.dto.ValorTotalPorPeriodoResponse;
import com.fontecode.recebimento.recebimento.infrastructure.adapter.out.persistence.repository.SpringDataRecebimentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class RecebimentoControllerIntegrationTest {

    @Autowired
    private RecebimentoController controller;

    @Autowired
    private SpringDataRecebimentoRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Deve executar fluxo CRUD completo e persistir na tabela SQLite chamada 'recebimento'")
    void deveExecutarFluxoCrudCompleto() {
        RecebimentoRequest criarRequest = new RecebimentoRequest(
                "12345678901",
                "E1234567820261021143000000000001",
                "2026-10-21 14:30:00",
                "999990,99"
        );

        ResponseEntity<RecebimentoResponse> criadoResp = controller.criar(criarRequest);
        assertEquals(HttpStatus.CREATED, criadoResp.getStatusCode());
        RecebimentoResponse criado = criadoResp.getBody();
        assertNotNull(criado);
        assertNotNull(criado.id());
        assertEquals("12345678901", criado.cpf());
        assertEquals("E1234567820261021143000000000001", criado.e2e());
        assertEquals("2026-10-21 14:30:00", criado.data());
        assertEquals("999990,99", criado.valor());

        Integer countNaTabelaRecebimento = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM recebimento WHERE id = ?",
                Integer.class,
                criado.id()
        );
        assertEquals(1, countNaTabelaRecebimento);

        ResponseEntity<RecebimentoResponse> buscadoResp = controller.buscarPorId(criado.id());
        assertEquals(HttpStatus.OK, buscadoResp.getStatusCode());
        assertEquals("E1234567820261021143000000000001", buscadoResp.getBody().e2e());
        assertEquals("999990,99", buscadoResp.getBody().valor());

        RecebimentoRequest atualizarRequest = new RecebimentoRequest(
                "98765432100",
                "E1234567820261022100000000000002",
                "2026-10-22 10:00:00",
                "1500,50"
        );
        ResponseEntity<RecebimentoResponse> atualizadoResp = controller.atualizar(criado.id(), atualizarRequest);
        assertEquals(HttpStatus.OK, atualizadoResp.getStatusCode());
        assertEquals("98765432100", atualizadoResp.getBody().cpf());
        assertEquals("E1234567820261022100000000000002", atualizadoResp.getBody().e2e());
        assertEquals("2026-10-22 10:00:00", atualizadoResp.getBody().data());
        assertEquals("1500,50", atualizadoResp.getBody().valor());

        ResponseEntity<List<RecebimentoResponse>> listaResp = controller.listarTodos();
        assertEquals(1, listaResp.getBody().size());

        ResponseEntity<Void> removidoResp = controller.remover(criado.id());
        assertEquals(HttpStatus.NO_CONTENT, removidoResp.getStatusCode());
        assertEquals(0, repository.count());
    }

    @Test
    @DisplayName("Deve importar arquivo CSV identificando tipos de dados e persistindo na tabela SQLite 'recebimento'")
    void deveImportarCsvEPersistirNoBanco() {
        String conteudoCsv = """
                cpf;e2e;data;valor
                12345678901;E1234567820261021143000000000001;2026-10-21 14:30:00;999990,99
                98765432100;E1234567820261022091545000000002;2026-10-22 09:15:45;250,75
                """;

        MockMultipartFile arquivoCsv = new MockMultipartFile(
                "arquivo",
                "recebimentos.csv",
                "text/csv",
                conteudoCsv.getBytes(StandardCharsets.UTF_8)
        );

        ResponseEntity<ImportacaoCsvResponse> response = controller.importarCsv(arquivoCsv);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ImportacaoCsvResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(2, body.totalImportados());
        assertEquals(4, body.tiposIdentificadosPorColuna().size());

        Integer totalBanco = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM recebimento", Integer.class);
        assertEquals(2, totalBanco);
        assertTrue(body.recebimentos().stream().anyMatch(r -> "999990,99".equals(r.valor()) && "E1234567820261021143000000000001".equals(r.e2e())));
    }

    @Test
    @DisplayName("Deve retornar o valor total de recebimentos por CPF (com ou sem máscara) e 0,00 quando não houver registros")
    void deveRetornarValorTotalPorCpf() {
        controller.criar(new RecebimentoRequest("12345678901", "E1001", "2026-10-21 10:00:00", "1000,50"));
        controller.criar(new RecebimentoRequest("123.456.789-01", "E1002", "2026-10-21 15:00:00", "250,25"));
        controller.criar(new RecebimentoRequest("98765432100", "E1003", "2026-10-21 18:00:00", "500,00"));

        ResponseEntity<ValorTotalPorCpfResponse> respSemMascara = controller.obterValorTotalPorCpf("12345678901");
        assertEquals(HttpStatus.OK, respSemMascara.getStatusCode());
        assertNotNull(respSemMascara.getBody());
        assertEquals("12345678901", respSemMascara.getBody().cpf());
        assertEquals("1250,75", respSemMascara.getBody().valorTotal());

        ResponseEntity<ValorTotalPorCpfResponse> respComMascara = controller.obterValorTotalPorCpf("123.456.789-01");
        assertEquals(HttpStatus.OK, respComMascara.getStatusCode());
        assertNotNull(respComMascara.getBody());
        assertEquals("12345678901", respComMascara.getBody().cpf());
        assertEquals("1250,75", respComMascara.getBody().valorTotal());

        ResponseEntity<ValorTotalPorCpfResponse> respSemRegistros = controller.obterValorTotalPorCpf("11122233344");
        assertEquals(HttpStatus.OK, respSemRegistros.getStatusCode());
        assertNotNull(respSemRegistros.getBody());
        assertEquals("0,00", respSemRegistros.getBody().valorTotal());
    }

    @Test
    @DisplayName("Deve retornar o valor total de recebimentos por data inicial e data final")
    void deveRetornarValorTotalPorPeriodo() {
        controller.criar(new RecebimentoRequest("12345678901", "E2001", "2026-10-20 23:59:59", "100,00"));
        controller.criar(new RecebimentoRequest("12345678901", "E2002", "2026-10-21 10:00:00", "300,40"));
        controller.criar(new RecebimentoRequest("98765432100", "E2003", "2026-10-22 18:30:00", "700,60"));
        controller.criar(new RecebimentoRequest("98765432100", "E2004", "2026-10-23 00:00:01", "50,00"));

        ResponseEntity<ValorTotalPorPeriodoResponse> respComHora = controller.obterValorTotalPorPeriodo(
                "2026-10-21 00:00:00",
                "2026-10-22 23:59:59"
        );
        assertEquals(HttpStatus.OK, respComHora.getStatusCode());
        assertNotNull(respComHora.getBody());
        assertEquals("2026-10-21 00:00:00", respComHora.getBody().dataInicial());
        assertEquals("2026-10-22 23:59:59", respComHora.getBody().dataFinal());
        assertEquals("1001,00", respComHora.getBody().valorTotal());

        ResponseEntity<ValorTotalPorPeriodoResponse> respApenasData = controller.obterValorTotalPorPeriodo(
                "2026-10-21",
                "2026-10-22"
        );
        assertEquals(HttpStatus.OK, respApenasData.getStatusCode());
        assertNotNull(respApenasData.getBody());
        assertEquals("2026-10-21 00:00:00", respApenasData.getBody().dataInicial());
        assertEquals("2026-10-22 23:59:59", respApenasData.getBody().dataFinal());
        assertEquals("1001,00", respApenasData.getBody().valorTotal());

        assertThrows(DomainValidationException.class, () -> controller.obterValorTotalPorPeriodo(
                "2026-10-25 00:00:00",
                "2026-10-21 00:00:00"
        ));
    }
}
