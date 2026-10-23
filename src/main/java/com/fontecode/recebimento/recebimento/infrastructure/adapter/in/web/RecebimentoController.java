package com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web;

import com.fontecode.recebimento.recebimento.domain.exception.CsvImportException;
import com.fontecode.recebimento.recebimento.domain.model.Recebimento;
import com.fontecode.recebimento.recebimento.domain.model.RecebimentoFormatters;
import com.fontecode.recebimento.recebimento.domain.model.ResultadoImportacaoCsv;
import com.fontecode.recebimento.recebimento.domain.port.in.ImportarRecebimentosCsvUseCase;
import com.fontecode.recebimento.recebimento.domain.port.in.RecebimentoUseCase;
import com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web.dto.ImportacaoCsvResponse;
import com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web.dto.RecebimentoRequest;
import com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web.dto.RecebimentoResponse;
import com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web.dto.ValorTotalPorCpfResponse;
import com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web.dto.ValorTotalPorPeriodoResponse;
import com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web.mapper.RecebimentoWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/recebimentos")
@Tag(name = "Recebimentos", description = "API CRUD e importação CSV para manipulação de recebimentos")
public class RecebimentoController {

    private final RecebimentoUseCase recebimentoUseCase;
    private final ImportarRecebimentosCsvUseCase importarCsvUseCase;
    private final RecebimentoWebMapper webMapper;

    public RecebimentoController(
            RecebimentoUseCase recebimentoUseCase,
            ImportarRecebimentosCsvUseCase importarCsvUseCase,
            RecebimentoWebMapper webMapper
    ) {
        this.recebimentoUseCase = recebimentoUseCase;
        this.importarCsvUseCase = importarCsvUseCase;
        this.webMapper = webMapper;
    }

    @PostMapping
    @Operation(
            summary = "Criar um novo recebimento",
            description = "Cadastra um recebimento com CPF, E2E, data (YYYY-MM-DD HH:mm:SS) e valor (999990,99)."
    )
    @ApiResponse(responseCode = "201", description = "Recebimento criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    public ResponseEntity<RecebimentoResponse> criar(@Valid @RequestBody RecebimentoRequest request) {
        Recebimento criado = recebimentoUseCase.criar(webMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(webMapper.toResponse(criado));
    }

    @GetMapping
    @Operation(summary = "Listar todos os recebimentos", description = "Retorna todos os recebimentos cadastrados na tabela SQLite.")
    @ApiResponse(responseCode = "200", description = "Lista de recebimentos retornada com sucesso")
    public ResponseEntity<List<RecebimentoResponse>> listarTodos() {
        List<Recebimento> recebimentos = recebimentoUseCase.listarTodos();
        return ResponseEntity.ok(webMapper.toResponseList(recebimentos));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar recebimento por ID", description = "Retorna os detalhes de um recebimento específico pelo seu ID.")
    @ApiResponse(responseCode = "200", description = "Recebimento encontrado")
    @ApiResponse(responseCode = "404", description = "Recebimento não encontrado")
    public ResponseEntity<RecebimentoResponse> buscarPorId(@PathVariable Long id) {
        Recebimento recebimento = recebimentoUseCase.buscarPorId(id);
        return ResponseEntity.ok(webMapper.toResponse(recebimento));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar um recebimento existente", description = "Atualiza os dados de CPF, E2E, data e valor de um recebimento pelo ID.")
    @ApiResponse(responseCode = "200", description = "Recebimento atualizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "404", description = "Recebimento não encontrado")
    public ResponseEntity<RecebimentoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody RecebimentoRequest request
    ) {
        Recebimento atualizado = recebimentoUseCase.atualizar(id, webMapper.toDomain(request));
        return ResponseEntity.ok(webMapper.toResponse(atualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover um recebimento", description = "Remove um recebimento do banco de dados pelo ID.")
    @ApiResponse(responseCode = "204", description = "Recebimento removido com sucesso")
    @ApiResponse(responseCode = "404", description = "Recebimento não encontrado")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        recebimentoUseCase.remover(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/valor-total/cpf/{cpf}")
    @Operation(
            summary = "Retornar valor total por CPF",
            description = "Calcula e retorna a soma dos valores de todos os recebimentos associados ao CPF informado (11 dígitos ou formatado 000.000.000-00)."
    )
    @ApiResponse(responseCode = "200", description = "Valor total por CPF calculado com sucesso")
    @ApiResponse(responseCode = "400", description = "CPF inválido")
    public ResponseEntity<ValorTotalPorCpfResponse> obterValorTotalPorCpf(
            @Parameter(description = "CPF do titular (11 dígitos ou formatado 000.000.000-00)", example = "12345678901", required = true)
            @PathVariable String cpf
    ) {
        BigDecimal valorTotal = recebimentoUseCase.calcularValorTotalPorCpf(cpf);
        return ResponseEntity.ok(webMapper.toValorTotalPorCpfResponse(cpf, valorTotal));
    }

    @GetMapping("/valor-total/periodo")
    @Operation(
            summary = "Retornar valor total por data inicial e data final",
            description = "Calcula e retorna a soma dos valores dos recebimentos dentro do intervalo entre dataInicial e dataFinal (formatos aceitos: YYYY-MM-DD HH:mm:SS ou YYYY-MM-DD)."
    )
    @ApiResponse(responseCode = "200", description = "Valor total por período calculado com sucesso")
    @ApiResponse(responseCode = "400", description = "Datas inválidas ou data inicial maior que a data final")
    public ResponseEntity<ValorTotalPorPeriodoResponse> obterValorTotalPorPeriodo(
            @Parameter(description = "Data inicial no formato YYYY-MM-DD HH:mm:SS ou YYYY-MM-DD", example = "2026-10-21 00:00:00", required = true)
            @RequestParam("dataInicial") String dataInicial,
            @Parameter(description = "Data final no formato YYYY-MM-DD HH:mm:SS ou YYYY-MM-DD", example = "2026-10-22 23:59:59", required = true)
            @RequestParam("dataFinal") String dataFinal
    ) {
        LocalDateTime inicio = RecebimentoFormatters.parseDataInicial(dataInicial);
        LocalDateTime fim = RecebimentoFormatters.parseDataFinal(dataFinal);
        BigDecimal valorTotal = recebimentoUseCase.calcularValorTotalPorPeriodo(inicio, fim);
        return ResponseEntity.ok(webMapper.toValorTotalPorPeriodoResponse(inicio, fim, valorTotal));
    }

    @PostMapping(value = "/importar-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Importar recebimentos via arquivo CSV",
            description = "Lê um arquivo CSV, identifica automaticamente o tipo de dado de cada coluna (CPF, E2E, Data YYYY-MM-DD HH:mm:SS, Valor 999990,99) independentemente da ordem das colunas e persiste os registros na tabela SQLite 'recebimento'."
    )
    @ApiResponse(responseCode = "201", description = "Arquivo CSV importado e persistido com sucesso")
    @ApiResponse(responseCode = "400", description = "Arquivo CSV inválido ou vazio")
    public ResponseEntity<ImportacaoCsvResponse> importarCsv(
            @Parameter(
                    description = "Arquivo CSV contendo os dados de recebimento (cpf, e2e, data, valor)",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestParam("arquivo") MultipartFile arquivo
    ) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new CsvImportException("É necessário enviar um arquivo CSV não vazio no parâmetro 'arquivo'.");
        }
        try {
            ResultadoImportacaoCsv resultado = importarCsvUseCase.importar(arquivo.getInputStream());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(webMapper.toImportacaoResponse(resultado));
        } catch (IOException ex) {
            throw new CsvImportException("Não foi possível processar o arquivo CSV enviado.", ex);
        }
    }
}
