package com.fontecode.recebimento.recebimento.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI recebimentoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Recebimentos")
                        .version("1.0.0")
                        .description("API CRUD para manipulação de dados de recebimentos (CPF, E2E, Data YYYY-MM-DD HH:mm:SS e Valor 999990,99) e importação inteligente de CSV persistindo na tabela SQLite 'recebimento'.")
                        .contact(new Contact()
                                .name("FonteCode")
                                .email("contato@fontecode.com")));
    }
}
