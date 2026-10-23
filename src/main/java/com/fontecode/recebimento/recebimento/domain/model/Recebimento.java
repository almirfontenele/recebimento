package com.fontecode.recebimento.recebimento.domain.model;

import com.fontecode.recebimento.recebimento.domain.exception.DomainValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

public class Recebimento {

    private final Long id;
    private final String cpf;
    private final String e2e;
    private final LocalDateTime data;
    private final BigDecimal valor;

    public Recebimento(Long id, String cpf, String e2e, LocalDateTime data, BigDecimal valor) {
        this.id = id;
        this.cpf = RecebimentoFormatters.normalizarEValidarCpf(cpf);
        this.e2e = RecebimentoFormatters.normalizarEValidarE2e(e2e);
        this.data = validarData(data);
        this.valor = validarValor(valor);
    }

    public static Recebimento novo(String cpf, String e2e, LocalDateTime data, BigDecimal valor) {
        return new Recebimento(null, cpf, e2e, data, valor);
    }

    public static Recebimento aPartirDeTexto(String cpf, String e2e, String dataTexto, String valorTexto) {
        return new Recebimento(
                null,
                cpf,
                e2e,
                RecebimentoFormatters.parseData(dataTexto),
                RecebimentoFormatters.parseValor(valorTexto)
        );
    }

    public Recebimento comId(Long novoId) {
        return new Recebimento(novoId, this.cpf, this.e2e, this.data, this.valor);
    }

    private static LocalDateTime validarData(LocalDateTime data) {
        if (data == null) {
            throw new DomainValidationException("A data do recebimento é obrigatória.");
        }
        return data;
    }

    private static BigDecimal validarValor(BigDecimal valor) {
        if (valor == null) {
            throw new DomainValidationException("O valor do recebimento é obrigatório.");
        }
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException("O valor do recebimento não pode ser negativo.");
        }
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public String getCpf() {
        return cpf;
    }

    public String getE2e() {
        return e2e;
    }

    public LocalDateTime getData() {
        return data;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getDataFormatada() {
        return RecebimentoFormatters.formatarData(data);
    }

    public String getValorFormatado() {
        return RecebimentoFormatters.formatarValor(valor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recebimento that = (Recebimento) o;
        return Objects.equals(id, that.id)
                && Objects.equals(cpf, that.cpf)
                && Objects.equals(e2e, that.e2e)
                && Objects.equals(data, that.data)
                && Objects.equals(valor, that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cpf, e2e, data, valor);
    }
}
