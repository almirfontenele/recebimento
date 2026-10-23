package com.fontecode.recebimento.recebimento.domain.model;

public enum TipoDadoCsv {
    CPF("Texto/Documento (CPF - 11 dígitos)"),
    E2E("Identificador End-to-End (E2E - Alfanumérico)"),
    DATA("Data/Hora (YYYY-MM-DD HH:mm:SS)"),
    VALOR("Valor Monetário Decimal (999990,99)");

    private final String descricao;

    TipoDadoCsv(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
