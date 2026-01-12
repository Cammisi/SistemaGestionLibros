package com.libros.gestion_cliente.unit.domain;

import com.libros.gestion_cliente.domain.model.DetalleVenta;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class DetalleVentaTest {

    @Test
    void deberiaCalcularSubtotalCorrectamente() {
        DetalleVenta detalle = DetalleVenta.builder()
                .cantidad(3)
                .precioAlMomento(new BigDecimal("10.50")) // CAMBIO
                .build();

        BigDecimal subtotal = detalle.getSubtotal();

        assertThat(subtotal).isEqualByComparingTo(new BigDecimal("31.50"));
    }

    @Test
    void deberiaDevolverCeroSiFaltanDatos() {
        DetalleVenta detalleSinPrecio = DetalleVenta.builder().cantidad(5).build();
        assertThat(detalleSinPrecio.getSubtotal()).isEqualTo(BigDecimal.ZERO);
    }
}