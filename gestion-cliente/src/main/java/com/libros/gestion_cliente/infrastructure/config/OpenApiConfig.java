package com.libros.gestion_cliente.infrastructure.config;

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
                        .title("API de Gestión de Libros")
                        .version("1.0")
                        .description("Documentación de la API para el sistema de venta de libros y gestión de clientes.")
                        .contact(new Contact()
                                .name("José")
                                .email("joseccammisi@gmail.com")));
    }
}