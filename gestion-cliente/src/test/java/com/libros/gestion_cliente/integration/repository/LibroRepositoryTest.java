package com.libros.gestion_cliente.integration.repository;

import com.libros.gestion_cliente.domain.model.Libro;
import com.libros.gestion_cliente.domain.repository.LibroRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LibroRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private LibroRepository libroRepository;

    @Test
    void deberiaGuardarYBuscarPorIsbn() {
        // GIVEN
        Libro libro = Libro.builder()
                .titulo("El Principito")
                .autor("Antoine de Saint-Exupéry")
                .isbn("978-3-16-148410-0")
                .precio(new BigDecimal("15.50"))
                .stock(10)
                .editorial("Salamandra")
                .build();

        // WHEN
        libroRepository.save(libro);

        // THEN
        Optional<Libro> encontrado = libroRepository.findByIsbn("978-3-16-148410-0");
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getTitulo()).isEqualTo("El Principito");
        assertThat(encontrado.get().getCantidadVolumenes()).isEqualTo(1); // Valor por defecto
    }

    @Test
    void deberiaBuscarPorTituloFlexible() {
        // GIVEN
        Libro libro1 = Libro.builder().titulo("Java a Fondo").autor("X").isbn("1").precio(BigDecimal.TEN).stock(1).build();
        Libro libro2 = Libro.builder().titulo("Aprende Java en 21 días").autor("Y").isbn("2").precio(BigDecimal.TEN).stock(1).build();
        Libro libro3 = Libro.builder().titulo("Cocina fácil").autor("Z").isbn("3").precio(BigDecimal.TEN).stock(1).build();

        libroRepository.saveAll(List.of(libro1, libro2, libro3));

        // WHEN
        List<Libro> resultados = libroRepository.findByTituloContainingIgnoreCase("JAVA");

        // THEN
        assertThat(resultados).hasSize(2); // Debería traer los dos primeros
        assertThat(resultados).extracting(Libro::getTitulo)
                .contains("Java a Fondo", "Aprende Java en 21 días");
    }
}