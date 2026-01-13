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
        // Verifica que el contexto de Spring levante sin errores
    }

    @Test
    void verificarMetodoMain() {
        // Este test invoca el método main para cubrir esas líneas en JaCoCo
        // Usamos assertThatCode para asegurar que no lance excepciones (Smoke Test)
        assertThatCode(() ->
                GestionClienteApplication.main(new String[]{})
        ).doesNotThrowAnyException();
    }
}