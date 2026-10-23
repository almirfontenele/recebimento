package com.fontecode.recebimento.recebimento.application.parser;

import com.fontecode.recebimento.recebimento.domain.exception.CsvImportException;
import com.fontecode.recebimento.recebimento.domain.exception.DomainValidationException;
import com.fontecode.recebimento.recebimento.domain.model.Recebimento;
import com.fontecode.recebimento.recebimento.domain.model.RecebimentoFormatters;
import com.fontecode.recebimento.recebimento.domain.model.ResultadoImportacaoCsv;
import com.fontecode.recebimento.recebimento.domain.model.TipoDadoCsv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.Map;
import java.util.Optional;

public class CsvRecebimentoParser {

    private static final int QUANTIDADE_CAMPOS_ESPERADA = 4;

    public ResultadoImportacaoCsv parse(InputStream inputStream) {
        if (inputStream == null) {
            throw new CsvImportException("O fluxo de dados do arquivo CSV não pode ser nulo.");
        }

        List<String> linhas = lerLinhasNaoVazias(inputStream);
        if (linhas.isEmpty()) {
            throw new CsvImportException("O arquivo CSV enviado está vazio.");
        }

        List<String> nomesColunas = null;
        int indiceInicial = 0;

        List<String> tokensPrimeiraLinha = tokenizarLinha(linhas.get(0));
        if (isLinhaCabecalho(tokensPrimeiraLinha)) {
            nomesColunas = tokensPrimeiraLinha;
            indiceInicial = 1;
        }

        if (indiceInicial >= linhas.size()) {
            throw new CsvImportException("O arquivo CSV contém apenas cabeçalho e nenhum registro de recebimento.");
        }

        List<Recebimento> recebimentos = new ArrayList<>();
        Map<String, String> tiposIdentificadosPorColuna = new LinkedHashMap<>();

        for (int i = indiceInicial; i < linhas.size(); i++) {
            int numeroLinha = i + 1;
            List<String> tokens = tokenizarLinha(linhas.get(i));

            if (tokens.size() != QUANTIDADE_CAMPOS_ESPERADA) {
                throw new CsvImportException(
                        String.format("Linha %d inválida: esperados 4 campos (cpf, e2e, data, valor), mas foram encontrados %d.",
                                numeroLinha, tokens.size())
                );
            }

            Map<TipoDadoCsv, String> valoresPorTipo = identificarTiposNaLinha(
                    tokens,
                    nomesColunas,
                    tiposIdentificadosPorColuna,
                    numeroLinha
            );

            try {
                Recebimento recebimento = Recebimento.aPartirDeTexto(
                        valoresPorTipo.get(TipoDadoCsv.CPF),
                        valoresPorTipo.get(TipoDadoCsv.E2E),
                        valoresPorTipo.get(TipoDadoCsv.DATA),
                        valoresPorTipo.get(TipoDadoCsv.VALOR)
                );
                recebimentos.add(recebimento);
            } catch (DomainValidationException ex) {
                throw new CsvImportException(
                        String.format("Erro de validação na linha %d do CSV: %s", numeroLinha, ex.getMessage()),
                        ex
                );
            }
        }

        return new ResultadoImportacaoCsv(
                recebimentos.size(),
                tiposIdentificadosPorColuna,
                recebimentos
        );
    }

    
private List<String> lerLinhasNaoVazias(InputStream inputStream) {
    try (var reader = new BufferedReader(
            new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

        return reader.lines()
                .map(this::removerBomUtf8)
                .map(String::trim)
                .filter(linha -> !linha.isEmpty())
                .toList();

    } catch (IOException ex) {
        throw new CsvImportException("Falha ao ler o arquivo CSV.", ex);
    }
}


  
private String removerBomUtf8(String texto) {
    return texto != null && texto.startsWith("\uFEFF")
            ? texto.substring(1)
            : texto;
}


    private boolean isLinhaCabecalho(List<String> tokens) {
        Predicate<String> isDadoValido = ((Predicate<String>)
  RecebimentoFormatters::isCpfValido)
                .or(RecebimentoFormatters::isDataValida)
                .or(RecebimentoFormatters::isValorValido);

        return tokens.stream().noneMatch(isDadoValido);
    }

   
private Map<TipoDadoCsv, String> identificarTiposNaLinha(
        List<String> tokens,
        List<String> nomesColunas,
        Map<String, String> tiposIdentificadosPorColuna,
        int numeroLinha
) {
    Map<TipoDadoCsv, String> valoresPorTipo =
            new EnumMap<>(TipoDadoCsv.class);

    for (int indiceColuna = 0; indiceColuna < tokens.size(); indiceColuna++) {
        String valorToken = tokens.get(indiceColuna);
        int numeroColuna = indiceColuna + 1;

        TipoDadoCsv tipo = identificarTipoDoValor(valorToken)
                .orElseThrow(() -> criarErroTipoNaoIdentificado(
                        numeroLinha, numeroColuna, valorToken));

        validarTipoNaoDuplicado(valoresPorTipo, tipo, numeroLinha);

        valoresPorTipo.put(tipo, valorToken);

        registrarTipoColuna(
                indiceColuna,
                nomesColunas,
                tiposIdentificadosPorColuna,
                tipo
        );
    }

    return valoresPorTipo;
}

private CsvImportException criarErroTipoNaoIdentificado(
        int numeroLinha,
        int numeroColuna,
        String valor
) {
    return new CsvImportException(
            String.format(
                    "Linha %d, coluna %d: não foi possível identificar " +
                    "o tipo de dado para o valor '%s'. " +
                    "Formatos aceitos: CPF (11 dígitos), " +
                    "E2E (identificador alfanumérico), " +
                    "Data (YYYY-MM-DD HH:mm:SS) ou Valor (999990,99).",
                    numeroLinha, numeroColuna, valor
            )
    );
}

private void validarTipoNaoDuplicado(
        Map<TipoDadoCsv, String> valoresPorTipo,
        TipoDadoCsv tipo,
        int numeroLinha
) {
    if (valoresPorTipo.containsKey(tipo)) {
        throw new CsvImportException(
                String.format(
                        "Linha %d possui mais de uma coluna identificada como %s.",
                        numeroLinha,
                        tipo.name()
                )
        );
    }
}

private void registrarTipoColuna(
        int indiceColuna,
        List<String> nomesColunas,
        Map<String, String> tiposIdentificadosPorColuna,
        TipoDadoCsv tipo
) {
    if (tiposIdentificadosPorColuna.size() >= QUANTIDADE_CAMPOS_ESPERADA) {
        return;
    }

    String nomeColuna = obterNomeColuna(nomesColunas, indiceColuna);
    String descricaoTipo = tipo.name() + " - " + tipo.getDescricao();

    tiposIdentificadosPorColuna.put(nomeColuna, descricaoTipo);
}


    private String obterNomeColuna(List<String> nomesColunas, int indiceColuna) {
        String prefixo = "coluna_" + (indiceColuna + 1);
        if (nomesColunas != null && indiceColuna < nomesColunas.size()) {
            String nomeCabecalho = nomesColunas.get(indiceColuna).trim();
            if (!nomeCabecalho.isEmpty()) {
                return prefixo + " (" + nomeCabecalho + ")";
            }
        }
        return prefixo;
    }

    public Optional<TipoDadoCsv> identificarTipoDoValor(String valorBruto) {
        if (valorBruto == null || valorBruto.isBlank()) {
            return Optional.empty();
        }
        String valor = valorBruto.trim();

        if (RecebimentoFormatters.isDataValida(valor)) {
            return Optional.of(TipoDadoCsv.DATA);
        }
        if (RecebimentoFormatters.isCpfValido(valor)) {
            return Optional.of(TipoDadoCsv.CPF);
        }
        if (RecebimentoFormatters.isValorValido(valor)) {
            return Optional.of(TipoDadoCsv.VALOR);
        }
        if (RecebimentoFormatters.isE2eValido(valor)) {
            return Optional.of(TipoDadoCsv.E2E);
        }
        return Optional.empty();
    }

    private List<String> tokenizarLinha(String linha) {
        char delimitador = detectarDelimitador(linha);
        List<String> tokensBrutos = separarRespeitandoAspas(linha, delimitador);

        if (delimitador == ',' && tokensBrutos.size() == QUANTIDADE_CAMPOS_ESPERADA + 1) {
            return recomporValorDecimalComVirgulaSeNecessario(tokensBrutos);
        }
        return tokensBrutos;
    }

    private char detectarDelimitador(String linha) {
        boolean dentroDeAspas = false;
        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (c == '"') {
                dentroDeAspas = !dentroDeAspas;
            } else if (!dentroDeAspas && c == ';') {
                return ';';
            }
        }
        return ',';
    }

    private List<String> separarRespeitandoAspas(String linha, char delimitador) {
        List<String> tokens = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        boolean dentroDeAspas = false;

        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (c == '"') {
                dentroDeAspas = !dentroDeAspas;
            } else if (c == delimitador && !dentroDeAspas) {
                tokens.add(atual.toString().trim());
                atual.setLength(0);
            } else {
                atual.append(c);
            }
        }
        tokens.add(atual.toString().trim());
        return tokens;
    }

    private List<String> recomporValorDecimalComVirgulaSeNecessario(List<String> tokens) {
        List<String> resultado = new ArrayList<>();
        int i = 0;
        while (i < tokens.size()) {
            if (i + 1 < tokens.size()
                    && resultado.size() + (tokens.size() - i - 1) >= QUANTIDADE_CAMPOS_ESPERADA
                    && isParteInteiraValor(tokens.get(i))
                    && isCentavosDoisDigitos(tokens.get(i + 1))) {
                resultado.add(tokens.get(i) + "," + tokens.get(i + 1));
                i += 2;
            } else {
                resultado.add(tokens.get(i));
                i++;
            }
        }
        return resultado;
    }

    private boolean isParteInteiraValor(String token) {
        return token != null && token.matches("^\\d{1,10}$") && !RecebimentoFormatters.isCpfValido(token);
    }

    private boolean isCentavosDoisDigitos(String token) {
        return token != null && token.matches("^\\d{2}$");
    }
}
