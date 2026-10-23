package com.fontecode.recebimento.recebimento.application.service;

import com.fontecode.recebimento.recebimento.application.parser.CsvRecebimentoParser;
import com.fontecode.recebimento.recebimento.domain.exception.DomainValidationException;
import com.fontecode.recebimento.recebimento.domain.exception.RecebimentoNaoEncontradoException;
import com.fontecode.recebimento.recebimento.domain.model.Recebimento;
import com.fontecode.recebimento.recebimento.domain.model.RecebimentoFormatters;
import com.fontecode.recebimento.recebimento.domain.model.ResultadoImportacaoCsv;
import com.fontecode.recebimento.recebimento.domain.port.in.ImportarRecebimentosCsvUseCase;
import com.fontecode.recebimento.recebimento.domain.port.in.RecebimentoUseCase;
import com.fontecode.recebimento.recebimento.domain.port.out.RecebimentoRepositoryPort;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

public class RecebimentoService implements RecebimentoUseCase, ImportarRecebimentosCsvUseCase {

    private final RecebimentoRepositoryPort repositoryPort;
    private final CsvRecebimentoParser csvParser;

    public RecebimentoService(RecebimentoRepositoryPort repositoryPort, CsvRecebimentoParser csvParser) {
        this.repositoryPort = repositoryPort;
        this.csvParser = csvParser;
    }

    @Override
    public Recebimento criar(Recebimento recebimento) {
        validarRecebimentoNaoNulo(recebimento);
        Recebimento novoRecebimento = Recebimento.novo(
                recebimento.getCpf(),
                recebimento.getE2e(),
                recebimento.getData(),
                recebimento.getValor()
        );
        return repositoryPort.salvar(novoRecebimento);
    }

    @Override
    public Recebimento buscarPorId(Long id) {
        validarId(id);
        return repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new RecebimentoNaoEncontradoException(id));
    }

    @Override
    public List<Recebimento> listarTodos() {
        return repositoryPort.listarTodos();
    }

    @Override
    public Recebimento atualizar(Long id, Recebimento recebimento) {
        validarId(id);
        validarRecebimentoNaoNulo(recebimento);

        if (!repositoryPort.existePorId(id)) {
            throw new RecebimentoNaoEncontradoException(id);
        }

        Recebimento recebimentoAtualizado = recebimento.comId(id);
        return repositoryPort.salvar(recebimentoAtualizado);
    }

    @Override
    public void remover(Long id) {
        validarId(id);
        if (!repositoryPort.existePorId(id)) {
            throw new RecebimentoNaoEncontradoException(id);
        }
        repositoryPort.removerPorId(id);
    }

    @Override
    public BigDecimal calcularValorTotalPorCpf(String cpf) {
        String cpfNormalizado = RecebimentoFormatters.normalizarEValidarCpf(cpf);
        BigDecimal total = repositoryPort.somarValorPorCpf(cpfNormalizado);
        return (total != null ? total : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calcularValorTotalPorPeriodo(LocalDateTime dataInicial, LocalDateTime dataFinal) {
        if (dataInicial == null) {
            throw new DomainValidationException("A data inicial é obrigatória.");
        }
        if (dataFinal == null) {
            throw new DomainValidationException("A data final é obrigatória.");
        }
        if (dataInicial.isAfter(dataFinal)) {
            throw new DomainValidationException("A data inicial não pode ser maior que a data final.");
        }
        BigDecimal total = repositoryPort.somarValorPorPeriodo(dataInicial, dataFinal);
        return (total != null ? total : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public ResultadoImportacaoCsv importar(InputStream csvInputStream) {
        ResultadoImportacaoCsv resultadoParse = csvParser.parse(csvInputStream);
        List<Recebimento> salvos = repositoryPort.salvarTodos(resultadoParse.recebimentos());
        return new ResultadoImportacaoCsv(
                salvos.size(),
                resultadoParse.tiposIdentificadosPorColuna(),
                salvos
        );
    }

    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new DomainValidationException("O ID do recebimento deve ser um número positivo.");
        }
    }

    private void validarRecebimentoNaoNulo(Recebimento recebimento) {
        if (recebimento == null) {
            throw new DomainValidationException("Os dados do recebimento são obrigatórios.");
        }
    }
}
