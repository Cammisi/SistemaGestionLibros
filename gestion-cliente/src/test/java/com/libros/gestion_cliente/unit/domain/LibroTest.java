package com.libros.gestion_cliente.unit.domain;

import com.libros.gestion_cliente.domain.model.Libro;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class LibroTest {

    @Test
    void deberiaCrearLibroConBuilderYValoresPorDefecto() {
        // WHEN
        Libro libro = Libro.builder()
                .titulo("Test")
                .isbn("123")
                .precioBase(new BigDecimal("100.00")) // CAMBIO
                .stock(5)
                .build();

        // THEN
        assertThat(libro.getCantVolumenes()).isEqualTo(1); // Default
        assertThat(libro.getTitulo()).isEqualTo("Test");
        assertThat(libro.getPrecioBase()).isEqualTo(new BigDecimal("100.00"));
    }
}