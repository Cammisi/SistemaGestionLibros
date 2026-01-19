package com.libros.gestion_cliente;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.Disabled;

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
        // Este test verifica que Spring levante, ese dejalo, suele funcionar bien
        // si no invoca la UI directamente.
    }

    @Test
    @Disabled("Deshabilitado en CI/CD porque requiere entorno gráfico (Display)") // <--- AGREGA ESTO
    void verificarMetodoMain() {
        // GestionClienteApplication.main(new String[]{});
    }
}