package com.libros.gestion_cliente.unit.domain;

import com.libros.gestion_cliente.domain.model.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

class CoverageBoosterTest {

    // --- 1. TEST DE ENUMS (EXHAUSTIVO) ---
    @Test
    void testearTodosLosEnums() {
        // Recorremos TODOS los valores para asegurar 100% de cobertura en los Enums
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

    // --- 2. TEST DE RAMAS LÓGICAS (DETALLE VENTA) ---
    @Test
    void testearRamasComplejas() {
        // Caso 1: Todo Nulo
        DetalleVenta d1 = new DetalleVenta();
        assertThat(d1.getSubtotal()).isEqualTo(BigDecimal.ZERO);

        // Caso 2: Solo Cantidad
        DetalleVenta d2 = new DetalleVenta();
        d2.setCantidad(5);
        assertThat(d2.getSubtotal()).isEqualTo(BigDecimal.ZERO);

        // Caso 3: Solo Precio
        DetalleVenta d3 = new DetalleVenta();
        d3.setPrecioAlMomento(BigDecimal.TEN);
        d3.setCantidad(null); // Forzamos nulo
        assertThat(d3.getSubtotal()).isEqualTo(BigDecimal.ZERO);

        // Caso 4: Todo OK
        DetalleVenta d4 = new DetalleVenta();
        d4.setCantidad(2);
        d4.setPrecioAlMomento(BigDecimal.TEN);
        assertThat(d4.getSubtotal()).isEqualByComparingTo(new BigDecimal("20.00"));
    }

    // --- 3. TEST DE ENTIDADES (POJO TESTER) ---
    // Probamos Getters, Setters, Equals, HashCode y ToString de TODAS las clases
    @Test
    void testearEntidadesCompletas() {
        testearClaseCliente();
        testearClaseFamiliar();
        testearClaseLibro();
        testearClaseVenta();
        testearClaseDetalleVenta();
        testearClaseCuota();
        testearClasePedidoEspecial();
    }

    private void testearClaseCliente() {
        Cliente c1 = Cliente.builder().id(1L).dni("A").build();
        Cliente c2 = Cliente.builder().id(1L).dni("A").build(); // Idéntico a c1
        Cliente c3 = Cliente.builder().id(2L).dni("B").build(); // Distinto

        verificarMetodos(c1, c2, c3);
    }

    private void testearClaseFamiliar() {
        Familiar f1 = Familiar.builder().id(1L).nombre("A").build();
        Familiar f2 = Familiar.builder().id(1L).nombre("A").build();
        Familiar f3 = Familiar.builder().id(2L).nombre("B").build();

        verificarMetodos(f1, f2, f3);
    }

    private void testearClaseLibro() {
        Libro l1 = Libro.builder().id(1L).isbn("111").build();
        Libro l2 = Libro.builder().id(1L).isbn("111").build();
        Libro l3 = Libro.builder().id(2L).isbn("222").build();

        verificarMetodos(l1, l2, l3);
    }

    private void testearClaseVenta() {
        Venta v1 = Venta.builder().id(1L).nroFactura("A").build();
        Venta v2 = Venta.builder().id(1L).nroFactura("A").build();
        Venta v3 = Venta.builder().id(2L).nroFactura("B").build();

        verificarMetodos(v1, v2, v3);
    }

    private void testearClaseDetalleVenta() {
        DetalleVenta d1 = DetalleVenta.builder().id(1L).build();
        DetalleVenta d2 = DetalleVenta.builder().id(1L).build();
        DetalleVenta d3 = DetalleVenta.builder().id(2L).build();

        verificarMetodos(d1, d2, d3);
    }

    private void testearClaseCuota() {
        Cuota c1 = Cuota.builder().id(1L).numeroCuota(1).build();
        Cuota c2 = Cuota.builder().id(1L).numeroCuota(1).build();
        Cuota c3 = Cuota.builder().id(2L).numeroCuota(2).build();

        // Setters específicos
        c1.setFechaPagoReal(LocalDate.now());
        c1.setNroReciboPago("R1");
        assertThat(c1.getFechaPagoReal()).isNotNull();
        assertThat(c1.getNroReciboPago()).isEqualTo("R1");

        verificarMetodos(c1, c2, c3);
    }

    private void testearClasePedidoEspecial() {
        PedidoEspecial p1 = PedidoEspecial.builder().id(1L).descripcion("A").build();
        PedidoEspecial p2 = PedidoEspecial.builder().id(1L).descripcion("A").build();
        PedidoEspecial p3 = PedidoEspecial.builder().id(2L).descripcion("B").build();

        verificarMetodos(p1, p2, p3);
    }

    // --- MÉTODO GENÉRICO PARA VALIDAR LOMBOK ---
    private void verificarMetodos(Object obj1, Object obj2, Object obj3) {
        // 1. Equals y HashCode (Caso: Iguales)
        assertThat(obj1).isEqualTo(obj2);
        assertThat(obj1.hashCode()).isEqualTo(obj2.hashCode());

        // 2. Equals (Caso: Diferentes)
        assertThat(obj1).isNotEqualTo(obj3);

        // 3. Equals (Casos Borde)
        assertThat(obj1).isNotEqualTo(null);
        assertThat(obj1).isNotEqualTo(new Object());
        assertThat(obj1).isEqualTo(obj1); // Mismo objeto en memoria

        // 4. ToString
        assertThat(obj1.toString()).isNotEmpty();
    }
}