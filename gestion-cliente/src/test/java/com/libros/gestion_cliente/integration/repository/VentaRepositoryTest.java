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
        // GIVEN
        Cliente cliente = Cliente.builder().nombre("Ana").apellido("Garcia").dni("22222222").build();
        clienteRepository.save(cliente);

        Libro libro = Libro.builder()
                .titulo("Clean Code")
                .autor("Uncle Bob")
                .isbn("978-0132350884")
                .precioBase(new BigDecimal("50.00"))
                .stock(10)
                .build();
        libroRepository.save(libro);

        // WHEN: Creamos Venta
        Venta venta = Venta.builder()
                .cliente(cliente)
                .nroFactura("F-0001-XXXX")
                .estado(EstadoVenta.EN_PROCESO)
                .build();

        DetalleVenta detalle = DetalleVenta.builder()
                .libro(libro)
                .cantidad(2)
                .precioAlMomento(libro.getPrecioBase())
                .build();

        venta.addDetalle(detalle);

        Venta guardada = ventaRepository.save(venta);


        assertThat(guardada.getId()).isNotNull();

        assertThat(guardada.getMontoTotal()).isEqualByComparingTo(new BigDecimal("100.00"));

        Optional<Venta> recuperada = ventaRepository.findByNroFactura("F-0001-XXXX");
        assertThat(recuperada).isPresent();
        assertThat(recuperada.get().getEstado()).isEqualTo(EstadoVenta.EN_PROCESO);
    }
}