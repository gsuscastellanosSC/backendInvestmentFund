package com.btgpactual.funds.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BTG Pactual - Funds Management API")
                        .version("1.0")
                        .description("API reactiva para la gestión de suscripciones a fondos de inversión.")
                        .contact(new Contact()
                                .name("Jesus Castellanos")
                                .email("jesuscastellanospaez@gmail.com")));
    }
}
