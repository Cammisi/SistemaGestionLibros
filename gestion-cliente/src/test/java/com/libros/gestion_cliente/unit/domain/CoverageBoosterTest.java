package com.libros.gestion_cliente.unit.domain;

import com.libros.gestion_cliente.domain.model.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import static org.assertj.core.api.Assertions.assertThat;

class CoverageBoosterTest {

    @Test
    void testearTodosLosEnums() {
        for (EstadoVenta e : EstadoVenta.values()) {
            assertThat(EstadoVenta.valueOf(e.name())).isEqualTo(e);
        }
        for (EstadoCuota e : EstadoCuota.values()) {
            assertThat(EstadoCuota.valueOf(e.name())).isEqualTo(e);
        }
        for (EstadoPedido e : EstadoPedido.values()) {
            assertThat(EstadoPedido.valueOf(e.name())).isEqualTo(e);
        }
    }

    @Test
    void testearRamasComplejas() {
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
    void testearEntidadesCompletas() {
        testearClaseCliente();
        testearClaseFamiliar();
        testearClaseLibro();
        testearClaseVentaManual(); // <--- Aquí recuperamos el % de Venta
        testearClaseDetalleVenta();
        testearClaseCuota();
        testearClasePedidoEspecial();
    }

    // Test manual exhaustivo para Venta
    private void testearClaseVentaManual() {
        Venta v = new Venta();
        v.setId(1L);
        v.setNroFactura("F-999");
        v.setCantidadCuotas(3);
        v.setMontoTotal(BigDecimal.valueOf(100));
        v.setFechaVenta(LocalDate.now());
        v.setEstado(EstadoVenta.FINALIZADA);
        v.setCliente(new Cliente());
        v.setDetalles(new ArrayList<>());

        assertThat(v.getId()).isEqualTo(1L);
        assertThat(v.getNroFactura()).isEqualTo("F-999");
        assertThat(v.getCantidadCuotas()).isEqualTo(3);
        assertThat(v.getMontoTotal()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(v.getFechaVenta()).isNotNull();
        assertThat(v.getEstado()).isEqualTo(EstadoVenta.FINALIZADA);
        assertThat(v.getCliente()).isNotNull();
        assertThat(v.getDetalles()).isNotNull();

        // Equals y HashCode
        Venta v2 = Venta.builder().id(1L).build();
        verificarEqualsHashCode(v, v2, new Venta());
    }

    private void testearClaseCliente() {
        Cliente c1 = Cliente.builder().id(1L).dni("A").build();
        Cliente c2 = Cliente.builder().id(1L).dni("A").build();
        Cliente c3 = Cliente.builder().id(2L).dni("B").build();
        verificarEqualsHashCode(c1, c2, c3);
    }

    private void testearClaseFamiliar() {
        Familiar f1 = Familiar.builder().id(1L).build();
        Familiar f2 = Familiar.builder().id(1L).build();
        Familiar f3 = Familiar.builder().id(2L).build();
        verificarEqualsHashCode(f1, f2, f3);
    }

    private void testearClaseLibro() {
        Libro l1 = Libro.builder().id(1L).build();
        Libro l2 = Libro.builder().id(1L).build();
        Libro l3 = Libro.builder().id(2L).build();
        verificarEqualsHashCode(l1, l2, l3);
    }

    private void testearClaseDetalleVenta() {
        DetalleVenta d1 = DetalleVenta.builder().id(1L).build();
        DetalleVenta d2 = DetalleVenta.builder().id(1L).build();
        DetalleVenta d3 = DetalleVenta.builder().id(2L).build();
        verificarEqualsHashCode(d1, d2, d3);
    }

    private void testearClaseCuota() {
        Cuota c1 = Cuota.builder().id(1L).build();
        Cuota c2 = Cuota.builder().id(1L).build();
        Cuota c3 = Cuota.builder().id(2L).build();
        verificarEqualsHashCode(c1, c2, c3);
    }

    private void testearClasePedidoEspecial() {
        PedidoEspecial p1 = PedidoEspecial.builder().id(1L).build();
        PedidoEspecial p2 = PedidoEspecial.builder().id(1L).build();
        PedidoEspecial p3 = PedidoEspecial.builder().id(2L).build();
        verificarEqualsHashCode(p1, p2, p3);
    }

    private void verificarEqualsHashCode(Object obj1, Object obj2, Object obj3) {
        assertThat(obj1).isEqualTo(obj2);
        assertThat(obj1.hashCode()).isEqualTo(obj2.hashCode());
        assertThat(obj1).isNotEqualTo(obj3);
        assertThat(obj1).isNotEqualTo(null);
        assertThat(obj1).isNotEqualTo(new Object());
        assertThat(obj1).isEqualTo(obj1);
        assertThat(obj1.toString()).isNotEmpty();
    }
}