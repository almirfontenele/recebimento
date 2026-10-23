package com.fontecode.recebimento.recebimento.domain.port.out;

import com.fontecode.recebimento.recebimento.domain.model.Recebimento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecebimentoRepositoryPort {

    Recebimento salvar(Recebimento recebimento);

    List<Recebimento> salvarTodos(List<Recebimento> recebimentos);

    Optional<Recebimento> buscarPorId(Long id);

    List<Recebimento> listarTodos();

    void removerPorId(Long id);

    boolean existePorId(Long id);

    BigDecimal somarValorPorCpf(String cpf);

    BigDecimal somarValorPorPeriodo(LocalDateTime dataInicial, LocalDateTime dataFinal);
}
