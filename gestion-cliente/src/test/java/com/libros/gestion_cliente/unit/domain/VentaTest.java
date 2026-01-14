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
                .precioAlMomento(new BigDecimal("10.00"))
                .build(); // Subtotal 20.00

        DetalleVenta d2 = DetalleVenta.builder()
                .cantidad(1)
                .precioAlMomento(new BigDecimal("5.50"))
                .build(); // Subtotal 5.50

        // WHEN
        venta.addDetalle(d1);
        venta.addDetalle(d2);

        // THEN
        assertThat(venta.getMontoTotal()).isEqualByComparingTo(new BigDecimal("25.50"));
    }

    // --- ESTE ES EL TEST CLAVE QUE FALTA PARA EL 100% ---
    @Test
    void deberiaRestarTotalAlRemoverDetalle() {
        // GIVEN
        Venta venta = new Venta();
        DetalleVenta d1 = DetalleVenta.builder()
                .cantidad(1)
                .precioAlMomento(BigDecimal.TEN)
                .build();

        venta.addDetalle(d1);
        // Verificamos que sumó primero
        assertThat(venta.getMontoTotal()).isEqualByComparingTo(BigDecimal.TEN);

        // WHEN: Removemos el detalle
        venta.removeDetalle(d1);

        // THEN: El total debe volver a cero
        assertThat(venta.getMontoTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(venta.getDetalles()).isEmpty();
    }
    // ----------------------------------------------------

    @Test
    void deberiaTenerValoresPorDefecto() {
        Venta venta = Venta.builder().nroFactura("A001").build();

        assertThat(venta.getEstado()).isEqualTo(EstadoVenta.EN_PROCESO);
        assertThat(venta.getFechaVenta()).isNotNull();
        assertThat(venta.getMontoTotal()).isEqualTo(BigDecimal.ZERO);
    }
}