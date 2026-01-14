package com.libros.gestion_cliente;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@Testcontainers
class GestionClienteApplicationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withInitScript("init.sql")
            .withUrlParam("stringtype", "unspecified");

    @Test
    void contextLoads() {
        // Este test verifica que el contexto "de test" levanta bien gracias a @ServiceConnection
    }

    @Test
    void verificarMetodoMain() {
        // TRUCO: Le pasamos las coordenadas del contenedor Docker al sistema
        // para que cuando main() arranque la app, sepa a dónde conectarse.
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgres.getUsername());
        System.setProperty("spring.datasource.password", postgres.getPassword());

        // Importante: Usamos puerto 0 para que no choque si el 8080 está ocupado
        System.setProperty("server.port", "0");

        // Ahora sí, ejecutamos main() sin miedo
        assertThatCode(() ->
                GestionClienteApplication.main(new String[]{})
        ).doesNotThrowAnyException();
    }
}