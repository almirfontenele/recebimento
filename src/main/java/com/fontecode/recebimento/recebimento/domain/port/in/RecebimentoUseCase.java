package com.fontecode.recebimento.recebimento.domain.port.in;

import com.fontecode.recebimento.recebimento.domain.model.Recebimento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface RecebimentoUseCase {

    Recebimento criar(Recebimento recebimento);

    Recebimento buscarPorId(Long id);

    List<Recebimento> listarTodos();

    Recebimento atualizar(Long id, Recebimento recebimento);

    void remover(Long id);

    BigDecimal calcularValorTotalPorCpf(String cpf);

    BigDecimal calcularValorTotalPorPeriodo(LocalDateTime dataInicial, LocalDateTime dataFinal);
}
