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
        // GIVEN
        Venta venta = new Venta();

        DetalleVenta d1 = DetalleVenta.builder()
                .cantidad(2)
                .precioUnitario(new BigDecimal("10.00"))
                .build(); // Subtotal 20.00

        DetalleVenta d2 = DetalleVenta.builder()
                .cantidad(1)
                .precioUnitario(new BigDecimal("5.50"))
                .build(); // Subtotal 5.50

        // WHEN
        venta.addDetalle(d1);
        venta.addDetalle(d2);

        // THEN
        // 20.00 + 5.50 = 25.50
        assertThat(venta.getTotal()).isEqualByComparingTo(new BigDecimal("25.50"));
    }

    @Test
    void deberiaRestarTotalAlRemoverDetalle() {
        // GIVEN
        Venta venta = new Venta();
        DetalleVenta d1 = DetalleVenta.builder().cantidad(1).precioUnitario(BigDecimal.TEN).build();
        venta.addDetalle(d1);
        assertThat(venta.getTotal()).isEqualByComparingTo(BigDecimal.TEN);

        // WHEN
        venta.removeDetalle(d1);

        // THEN
        assertThat(venta.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(venta.getDetalles()).isEmpty();
    }

    @Test
    void deberiaTenerValoresPorDefecto() {
        // Probamos el Builder y sus defaults (Estado y Fecha)
        Venta venta = Venta.builder().nroFactura(99).build();

        assertThat(venta.getEstado()).isEqualTo(EstadoVenta.FINALIZADA); // Default
        assertThat(venta.getCantidadCuotas()).isEqualTo(1); // Default
        assertThat(venta.getFecha()).isNotNull(); // Default
        assertThat(venta.getTotal()).isEqualTo(BigDecimal.ZERO); // Default
    }
}