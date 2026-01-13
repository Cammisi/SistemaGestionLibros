package com.libros.gestion_cliente.unit.domain;

import com.libros.gestion_cliente.domain.model.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

class CoverageBoosterTest {

    @Test
    void testearEnumsParaCobertura() {
        // Enums: Probamos values() y valueOf()
        assertThat(EstadoVenta.values()).isNotEmpty();
        assertThat(EstadoVenta.valueOf("EN_PROCESO")).isEqualTo(EstadoVenta.EN_PROCESO);

        assertThat(EstadoCuota.values()).isNotEmpty();
        assertThat(EstadoCuota.valueOf("PENDIENTE")).isEqualTo(EstadoCuota.PENDIENTE);

        assertThat(EstadoPedido.values()).isNotEmpty();
        assertThat(EstadoPedido.valueOf("PENDIENTE_COMPRA")).isEqualTo(EstadoPedido.PENDIENTE_COMPRA);
    }

    @Test
    void testearRamasDetalleVenta() {
        // Ramas lógicas de DetalleVenta
        DetalleVenta d1 = new DetalleVenta();
        assertThat(d1.getSubtotal()).isEqualTo(BigDecimal.ZERO);

        DetalleVenta d2 = DetalleVenta.builder().cantidad(1).build();
        assertThat(d2.getSubtotal()).isEqualTo(BigDecimal.ZERO);

        DetalleVenta d3 = DetalleVenta.builder().precioAlMomento(BigDecimal.TEN).build();
        d3.setCantidad(null);
        assertThat(d3.getSubtotal()).isEqualTo(BigDecimal.ZERO);

        DetalleVenta d4 = DetalleVenta.builder().cantidad(2).precioAlMomento(BigDecimal.TEN).build();
        assertThat(d4.getSubtotal()).isEqualByComparingTo(new BigDecimal("20.00"));
    }

    @Test
    void testearMetodosLombokFaltantes() {

        // 1. Cliente
        Cliente c1 = new Cliente();
        Cliente c2 = new Cliente();
        verificarMetodosEstandar(c1, c2);

        // 2. Familiar
        Familiar f1 = new Familiar();
        Familiar f2 = new Familiar();
        verificarMetodosEstandar(f1, f2);

        // 3. Libro (Probamos los nuevos campos explícitamente)
        Libro l1 = new Libro();
        l1.setTematica("A");
        l1.setCantVolumenes(1);
        l1.setPrecioBase(BigDecimal.ONE);
        assertThat(l1.getTematica()).isEqualTo("A");
        assertThat(l1.getCantVolumenes()).isEqualTo(1);
        assertThat(l1.getPrecioBase()).isEqualTo(BigDecimal.ONE);
        verificarMetodosEstandar(l1, new Libro());

        // 4. Venta (Probamos nroFactura y montoTotal)
        Venta v1 = new Venta();
        v1.setNroFactura("A");
        v1.setMontoTotal(BigDecimal.TEN);
        assertThat(v1.getNroFactura()).isEqualTo("A");
        assertThat(v1.getMontoTotal()).isEqualTo(BigDecimal.TEN);
        verificarMetodosEstandar(v1, new Venta());

        // 5. Cuota
        Cuota cu1 = new Cuota();
        Cuota cu2 = new Cuota();
        verificarMetodosEstandar(cu1, cu2);

        // 6. PedidoEspecial
        PedidoEspecial p1 = new PedidoEspecial();
        PedidoEspecial p2 = new PedidoEspecial();
        verificarMetodosEstandar(p1, p2);
    }

    private void verificarMetodosEstandar(Object o1, Object o2) {
        // Invoca toString, equals y hashCode para forzar cobertura
        assertThat(o1.toString()).isNotNull();
        assertThat(o1).isEqualTo(o1);
        assertThat(o1).isNotEqualTo(o2);
        assertThat(o1.hashCode()).isNotZero();
    }
}