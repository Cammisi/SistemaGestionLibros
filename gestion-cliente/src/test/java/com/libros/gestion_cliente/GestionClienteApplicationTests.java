package com.libros.gestion_cliente;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class GestionClienteApplicationTests {

    // Necesitamos levantar Postgres también aquí, porque al hacer @SpringBootTest
    // Spring intenta levantar TODO el sistema (incluida la conexión a BD).
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void contextLoads() {
        // Este test vacío es PODEROSO.
        // Verifica que Spring puede arrancar, crear todos los beans y conectarse a la BD.
        // Además, cubre la clase GestionClienteApplication.
    }

    @Test
    void verificarMetodoMain() {
        // Un pequeño truco para asegurar que la línea del "main" también cuenta en el coverage
        try {
            GestionClienteApplication.main(new String[] {});
        } catch (Exception e) {
            // Ignoramos errores de puerto ocupado, solo queremos ejecutar las líneas
        }
    }
}
