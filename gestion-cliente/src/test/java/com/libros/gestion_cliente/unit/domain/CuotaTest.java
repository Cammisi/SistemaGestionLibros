package com.libros.gestion_cliente.unit.domain;

import com.libros.gestion_cliente.domain.model.Cuota;
import com.libros.gestion_cliente.domain.model.EstadoCuota;
import com.libros.gestion_cliente.domain.model.Venta;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CuotaTest {

    @Test
    void deberiaCrearCuotaConValoresPorDefecto() {
        // GIVEN
        Venta venta = new Venta();

        // WHEN
        Cuota cuota = Cuota.builder()
                .venta(venta)
                .numeroCuota(1)
                .montoCuota(new BigDecimal("100.00"))
                .fechaVencimiento(LocalDate.now())
                .build();

        // THEN
        assertThat(cuota.getEstado()).isEqualTo(EstadoCuota.PENDIENTE); // Default verificado
        assertThat(cuota.getVenta()).isEqualTo(venta);
        assertThat(cuota.getNumeroCuota()).isEqualTo(1);
        assertThat(cuota.getMontoCuota()).isEqualTo(new BigDecimal("100.00"));
        assertThat(cuota.getFechaVencimiento()).isNotNull();
    }

    @Test
    void deberiaActualizarEstadoPago() {
        // GIVEN
        Cuota cuota = new Cuota();

        // WHEN
        cuota.setEstado(EstadoCuota.PAGADA);
        cuota.setFechaPagoReal(LocalDate.now());
        cuota.setNroReciboPago("REC-123");

        // THEN
        assertThat(cuota.getEstado()).isEqualTo(EstadoCuota.PAGADA);
        assertThat(cuota.getFechaPagoReal()).isNotNull();
        assertThat(cuota.getNroReciboPago()).isEqualTo("REC-123");
    }
}