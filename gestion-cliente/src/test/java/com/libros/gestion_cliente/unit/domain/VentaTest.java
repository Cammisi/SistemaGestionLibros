package com.libros.gestion_cliente.unit.domain;

import com.libros.gestion_cliente.domain.model.DetalleVenta;
import com.libros.gestion_cliente.domain.model.EstadoVenta;
import com.libros.gestion_cliente.domain.model.Venta;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class VentaTest {

    @Test
    void deberiaCalcularTotalAutomaticamenteAlAgregarDetalles() {
        Venta venta = new Venta();

        DetalleVenta d1 = DetalleVenta.builder()
                .cantidad(2)
                .precioAlMomento(new BigDecimal("10.00"))
                .build();

        DetalleVenta d2 = DetalleVenta.builder()
                .cantidad(1)
                .precioAlMomento(new BigDecimal("5.50"))
                .build();

        venta.addDetalle(d1);
        venta.addDetalle(d2);

        assertThat(venta.getMontoTotal()).isEqualByComparingTo(new BigDecimal("25.50"));
    }

    @Test
    void deberiaTenerValoresPorDefecto() {
        Venta venta = Venta.builder().nroFactura("A001").build();

        assertThat(venta.getEstado()).isEqualTo(EstadoVenta.EN_PROCESO);
        assertThat(venta.getFechaVenta()).isNotNull();
        assertThat(venta.getMontoTotal()).isEqualTo(BigDecimal.ZERO);
    }
}