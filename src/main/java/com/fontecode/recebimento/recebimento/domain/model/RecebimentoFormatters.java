package com.fontecode.recebimento.recebimento.domain.model;

import com.fontecode.recebimento.recebimento.domain.exception.DomainValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Pattern;

public final class RecebimentoFormatters {

    public static final String DATA_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern(DATA_PATTERN);
    private static final DateTimeFormatter DATA_DIA_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final Pattern CPF_DIGITS_PATTERN = Pattern.compile("^\\d{11}$");
    private static final Pattern CPF_FORMATTED_PATTERN = Pattern.compile("^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$");
    private static final Pattern E2E_PATTERN = Pattern.compile("^[A-Za-z0-9-]+$");
    private static final Pattern DATA_REGEX_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}$");
    private static final Pattern DATA_DIA_REGEX_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final Pattern VALOR_BR_PATTERN = Pattern.compile("^-?(?:\\d+|\\d{1,3}(?:\\.\\d{3})*),\\d{1,2}$");
    private static final Pattern VALOR_DOT_PATTERN = Pattern.compile("^-?\\d+\\.\\d{1,2}$");
    private static final Pattern VALOR_INT_PATTERN = Pattern.compile("^-?\\d+$");

    private RecebimentoFormatters() {
    }

    public static String normalizarEValidarCpf(String rawCpf) {
        if (rawCpf == null || rawCpf.isBlank()) {
            throw new DomainValidationException("O CPF é obrigatório.");
        }
        String trimmed = rawCpf.trim();
        if (!CPF_DIGITS_PATTERN.matcher(trimmed).matches() && !CPF_FORMATTED_PATTERN.matcher(trimmed).matches()) {
            throw new DomainValidationException(
                    "CPF inválido: '" + rawCpf + "'. Informe 11 dígitos numéricos ou no formato 000.000.000-00."
            );
        }
        return trimmed.replaceAll("\\D", "");
    }

    public static boolean isCpfValido(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return false;
        }
        String trimmed = candidate.trim();
        return CPF_DIGITS_PATTERN.matcher(trimmed).matches() || CPF_FORMATTED_PATTERN.matcher(trimmed).matches();
    }

    public static String normalizarEValidarE2e(String rawE2e) {
        if (rawE2e == null || rawE2e.isBlank()) {
            throw new DomainValidationException("O código E2E é obrigatório.");
        }
        String trimmed = rawE2e.trim();
        if (!E2E_PATTERN.matcher(trimmed).matches()) {
            throw new DomainValidationException(
                    "Código E2E inválido: '" + rawE2e + "'. Informe um identificador alfanumérico válido."
            );
        }
        return trimmed;
    }

    public static boolean isE2eValido(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return false;
        }
        String trimmed = candidate.trim();
        return E2E_PATTERN.matcher(trimmed).matches()
                && !isCpfValido(trimmed)
                && !isDataValida(trimmed)
                && !isValorValido(trimmed);
    }

    public static LocalDateTime parseData(String rawData) {
        if (rawData == null || rawData.isBlank()) {
            throw new DomainValidationException("A data é obrigatória no formato YYYY-MM-DD HH:mm:SS.");
        }
        String normalized = rawData.trim().replaceAll("\\s+", " ");
        try {
            return LocalDateTime.parse(normalized, DATA_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new DomainValidationException(
                    "Data inválida: '" + rawData + "'. Utilize o formato YYYY-MM-DD HH:mm:SS (ex: 2026-10-21 14:30:00).",
                    ex
            );
        }
    }

    public static LocalDateTime parseDataInicial(String rawData) {
        if (rawData == null || rawData.isBlank()) {
            throw new DomainValidationException("A data inicial é obrigatória no formato YYYY-MM-DD HH:mm:SS ou YYYY-MM-DD.");
        }
        String trimmed = rawData.trim();
        if (DATA_DIA_REGEX_PATTERN.matcher(trimmed).matches()) {
            try {
                return LocalDate.parse(trimmed, DATA_DIA_FORMATTER).atStartOfDay();
            } catch (DateTimeParseException ex) {
                throw new DomainValidationException(
                        "Data inicial inválida: '" + rawData + "'. Utilize o formato YYYY-MM-DD HH:mm:SS ou YYYY-MM-DD.",
                        ex
                );
            }
        }
        return parseData(trimmed);
    }

    public static LocalDateTime parseDataFinal(String rawData) {
        if (rawData == null || rawData.isBlank()) {
            throw new DomainValidationException("A data final é obrigatória no formato YYYY-MM-DD HH:mm:SS ou YYYY-MM-DD.");
        }
        String trimmed = rawData.trim();
        if (DATA_DIA_REGEX_PATTERN.matcher(trimmed).matches()) {
            try {
                return LocalDate.parse(trimmed, DATA_DIA_FORMATTER).atTime(23, 59, 59);
            } catch (DateTimeParseException ex) {
                throw new DomainValidationException(
                        "Data final inválida: '" + rawData + "'. Utilize o formato YYYY-MM-DD HH:mm:SS ou YYYY-MM-DD.",
                        ex
                );
            }
        }
        return parseData(trimmed);
    }

    public static boolean isDataValida(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return false;
        }
        String normalized = candidate.trim().replaceAll("\\s+", " ");
        if (!DATA_REGEX_PATTERN.matcher(normalized).matches()) {
            return false;
        }
        try {
            LocalDateTime.parse(normalized, DATA_FORMATTER);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    public static String formatarData(LocalDateTime data) {
        if (data == null) {
            return null;
        }
        return data.format(DATA_FORMATTER);
    }

    public static BigDecimal parseValor(String rawValor) {
        if (rawValor == null || rawValor.isBlank()) {
            throw new DomainValidationException("O valor é obrigatório no formato 999990,99.");
        }
        String trimmed = rawValor.trim();
        String normalized;
        if (VALOR_BR_PATTERN.matcher(trimmed).matches()) {
            normalized = trimmed.replace(".", "").replace(",", ".");
        } else if (VALOR_DOT_PATTERN.matcher(trimmed).matches() || VALOR_INT_PATTERN.matcher(trimmed).matches()) {
            normalized = trimmed;
        } else {
            throw new DomainValidationException(
                    "Valor inválido: '" + rawValor + "'. Utilize o formato 999990,99."
            );
        }

        try {
            BigDecimal valor = new BigDecimal(normalized).setScale(2, RoundingMode.HALF_UP);
            if (valor.compareTo(BigDecimal.ZERO) < 0) {
                throw new DomainValidationException("O valor do recebimento não pode ser negativo.");
            }
            return valor;
        } catch (NumberFormatException ex) {
            throw new DomainValidationException(
                    "Valor inválido: '" + rawValor + "'. Utilize o formato 999990,99.",
                    ex
            );
        }
    }

    public static boolean isValorValido(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return false;
        }
        String trimmed = candidate.trim();
        if (isCpfValido(trimmed)) {
            return false;
        }
        return VALOR_BR_PATTERN.matcher(trimmed).matches()
                || VALOR_DOT_PATTERN.matcher(trimmed).matches()
                || VALOR_INT_PATTERN.matcher(trimmed).matches();
    }

    public static String formatarValor(BigDecimal valor) {
        if (valor == null) {
            return null;
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("pt-BR"));
        symbols.setDecimalSeparator(',');
        DecimalFormat decimalFormat = new DecimalFormat("0.00", symbols);
        decimalFormat.setGroupingUsed(false);
        return decimalFormat.format(valor.setScale(2, RoundingMode.HALF_UP));
    }
}
