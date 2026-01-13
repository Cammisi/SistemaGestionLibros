package com.libros.gestion_cliente.unit.domain;

import com.libros.gestion_cliente.domain.model.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class CoverageBoosterTest {

    @Test
    void testearEnumsParaCobertura() {

        assertThat(EstadoVenta.values()).isNotEmpty();
        assertThat(EstadoVenta.valueOf("EN_PROCESO")).isEqualTo(EstadoVenta.EN_PROCESO);

        assertThat(EstadoCuota.values()).isNotEmpty();
        assertThat(EstadoCuota.valueOf("PENDIENTE")).isEqualTo(EstadoCuota.PENDIENTE);

        assertThat(EstadoPedido.values()).isNotEmpty();
        assertThat(EstadoPedido.valueOf("PENDIENTE_COMPRA")).isEqualTo(EstadoPedido.PENDIENTE_COMPRA);
    }

    @Test
    void testearRamasDetalleVenta() {
        // Probamos TODAS las combinaciones posibles del IF
        // if (precioAlMomento == null || cantidad == null)

        // Caso 1: Ambos Nulos
        DetalleVenta d1 = new DetalleVenta();
        assertThat(d1.getSubtotal()).isEqualTo(BigDecimal.ZERO);

        // Caso 2: Solo Precio Nulo (Cantidad tiene valor)
        DetalleVenta d2 = DetalleVenta.builder().cantidad(1).build();
        assertThat(d2.getSubtotal()).isEqualTo(BigDecimal.ZERO);

        // Caso 3: Solo Cantidad Nula (Precio tiene valor)
        DetalleVenta d3 = DetalleVenta.builder().precioAlMomento(BigDecimal.TEN).build();
        d3.setCantidad(null); // Forzamos nulo porque el builder tiene default 1
        assertThat(d3.getSubtotal()).isEqualTo(BigDecimal.ZERO);

        // Caso 4: Ninguno Nulo (Éxito)
        DetalleVenta d4 = DetalleVenta.builder().cantidad(2).precioAlMomento(BigDecimal.TEN).build();
        assertThat(d4.getSubtotal()).isEqualByComparingTo(new BigDecimal("20.00"));
    }
}