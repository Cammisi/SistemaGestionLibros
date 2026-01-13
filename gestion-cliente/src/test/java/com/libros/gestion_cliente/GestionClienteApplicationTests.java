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
        // Verifica que Spring arranca bien
    }

    @Test
    void verificarMetodoMain() {
        // Este test ejecuta el método main para que JaCoCo lo marque como cubierto
        // Usamos un array vacío de argumentos
        assertThatCode(() ->
                GestionClienteApplication.main(new String[]{})
        ).doesNotThrowAnyException();
    }
}