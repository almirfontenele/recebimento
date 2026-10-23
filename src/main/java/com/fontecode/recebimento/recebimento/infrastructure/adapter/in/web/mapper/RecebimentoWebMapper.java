package com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web.mapper;

import com.fontecode.recebimento.recebimento.domain.model.Recebimento;
import com.fontecode.recebimento.recebimento.domain.model.RecebimentoFormatters;
import com.fontecode.recebimento.recebimento.domain.model.ResultadoImportacaoCsv;
import com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web.dto.ImportacaoCsvResponse;
import com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web.dto.RecebimentoRequest;
import com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web.dto.RecebimentoResponse;
import com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web.dto.ValorTotalPorCpfResponse;
import com.fontecode.recebimento.recebimento.infrastructure.adapter.in.web.dto.ValorTotalPorPeriodoResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class RecebimentoWebMapper {

    public Recebimento toDomain(RecebimentoRequest request) {
        return Recebimento.aPartirDeTexto(
                request.cpf(),
                request.e2e(),
                request.data(),
                request.valor()
        );
    }

    public RecebimentoResponse toResponse(Recebimento domain) {
        return new RecebimentoResponse(
                domain.getId(),
                domain.getCpf(),
                domain.getE2e(),
                domain.getDataFormatada(),
                domain.getValorFormatado()
        );
    }

    public List<RecebimentoResponse> toResponseList(List<Recebimento> recebimentos) {
        return recebimentos.stream()
                .map(this::toResponse)
                .toList();
    }

    public ImportacaoCsvResponse toImportacaoResponse(ResultadoImportacaoCsv resultado) {
        return new ImportacaoCsvResponse(
                resultado.totalImportados(),
                resultado.tiposIdentificadosPorColuna(),
                toResponseList(resultado.recebimentos())
        );
    }

    public ValorTotalPorCpfResponse toValorTotalPorCpfResponse(String cpf, BigDecimal valorTotal) {
        return new ValorTotalPorCpfResponse(
                RecebimentoFormatters.normalizarEValidarCpf(cpf),
                RecebimentoFormatters.formatarValor(valorTotal)
        );
    }

    public ValorTotalPorPeriodoResponse toValorTotalPorPeriodoResponse(
            LocalDateTime dataInicial,
            LocalDateTime dataFinal,
            BigDecimal valorTotal
    ) {
        return new ValorTotalPorPeriodoResponse(
                RecebimentoFormatters.formatarData(dataInicial),
                RecebimentoFormatters.formatarData(dataFinal),
                RecebimentoFormatters.formatarValor(valorTotal)
        );
    }
}
