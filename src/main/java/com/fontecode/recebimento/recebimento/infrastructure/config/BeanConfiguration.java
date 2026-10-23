package com.fontecode.recebimento.recebimento.infrastructure.config;

import com.fontecode.recebimento.recebimento.application.parser.CsvRecebimentoParser;
import com.fontecode.recebimento.recebimento.application.service.RecebimentoService;
import com.fontecode.recebimento.recebimento.domain.port.out.RecebimentoRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public CsvRecebimentoParser csvRecebimentoParser() {
        return new CsvRecebimentoParser();
    }

    @Bean
    public RecebimentoService recebimentoService(
            RecebimentoRepositoryPort repositoryPort,
            CsvRecebimentoParser csvRecebimentoParser
    ) {
        return new RecebimentoService(repositoryPort, csvRecebimentoParser);
    }
}
