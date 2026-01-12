package com.libros.gestion_cliente.integration.repository;

import com.libros.gestion_cliente.domain.model.*;
import com.libros.gestion_cliente.domain.repository.ClienteRepository;
import com.libros.gestion_cliente.domain.repository.LibroRepository;
import com.libros.gestion_cliente.domain.repository.VentaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class VentaRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private VentaRepository ventaRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private LibroRepository libroRepository;

    @Test
    void deberiaGuardarVentaConDetallesYCalcularTotal() {
        // 1. GIVEN: Necesitamos un Cliente y un Libro guardados previamente
        Cliente cliente = Cliente.builder().nombre("Ana").apellido("Garcia").dni("22222222").build();
        clienteRepository.save(cliente);

        Libro libro = Libro.builder()
                .titulo("Clean Code")
                .autor("Uncle Bob")
                .isbn("978-0132350884")
                .precio(new BigDecimal("50.00")) // Precio actual del libro
                .stock(10)
                .build();
        libroRepository.save(libro);

        // 2. Creamos la Venta (Cabecera)
        Venta venta = Venta.builder()
                .cliente(cliente)
                .nroFactura(1001)
                .estado(EstadoVenta.PAGANDO)
                .build();

        // 3. Agregamos un detalle (2 libros a $50.00 c/u)
        DetalleVenta detalle = DetalleVenta.builder()
                .libro(libro)
                .cantidad(2)
                .precioUnitario(libro.getPrecio()) // Snapshot del precio
                .build();

        venta.addDetalle(detalle); // Esto debería disparar el recálculo del total

        // 4. WHEN: Guardamos la venta (Cascade guardará el detalle)
        Venta guardada = ventaRepository.save(venta);

        // 5. THEN
        assertThat(guardada.getId()).isNotNull();
        assertThat(guardada.getTotal()).isEqualByComparingTo(new BigDecimal("100.00")); // 2 * 50
        assertThat(guardada.getDetalles()).hasSize(1);

        // Verificamos recuperar por Nro Factura
        Optional<Venta> recuperada = ventaRepository.findByNroFactura(1001);
        assertThat(recuperada).isPresent();
        assertThat(recuperada.get().getEstado()).isEqualTo(EstadoVenta.PAGANDO);
    }
}