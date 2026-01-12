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
                .precio(new BigDecimal("100.00"))
                .stock(5)
                .build();

        // THEN
        assertThat(libro.getCantidadVolumenes()).isEqualTo(1); // Default verificado
        assertThat(libro.getTitulo()).isEqualTo("Test");
        assertThat(libro.getPrecio()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void deberiaActualizarStock() {
        Libro libro = new Libro();
        libro.setStock(10);
        assertThat(libro.getStock()).isEqualTo(10);
    }
}