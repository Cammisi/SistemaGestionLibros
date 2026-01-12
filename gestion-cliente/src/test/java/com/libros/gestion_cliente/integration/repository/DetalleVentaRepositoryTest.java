package com.libros.gestion_cliente.integration.repository;

import com.libros.gestion_cliente.domain.model.*;
import com.libros.gestion_cliente.domain.repository.ClienteRepository;
import com.libros.gestion_cliente.domain.repository.DetalleVentaRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DetalleVentaRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;
    @Autowired
    private VentaRepository ventaRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private LibroRepository libroRepository;

    @Test
    void deberiaEncontrarDetallesPorLibro() {
        Cliente cliente = Cliente.builder().nombre("Pepe").apellido("Test").dni("999").build();
        clienteRepository.save(cliente);

        Libro libroTarget = Libro.builder().titulo("Libro Objetivo").autor("A").isbn("111").precioBase(BigDecimal.TEN).build();
        Libro libroOtro = Libro.builder().titulo("Otro Libro").autor("B").isbn("222").precioBase(BigDecimal.TEN).build();
        libroRepository.saveAll(List.of(libroTarget, libroOtro));

        Venta venta = Venta.builder().cliente(cliente).nroFactura("FAC-999").build();

        DetalleVenta d1 = DetalleVenta.builder().libro(libroTarget).cantidad(2).precioAlMomento(BigDecimal.TEN).build();
        DetalleVenta d2 = DetalleVenta.builder().libro(libroOtro).cantidad(1).precioAlMomento(BigDecimal.TEN).build();

        venta.addDetalle(d1);
        venta.addDetalle(d2);

        ventaRepository.save(venta);

        List<DetalleVenta> resultados = detalleVentaRepository.findByLibroId(libroTarget.getId());

        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).getLibro().getTitulo()).isEqualTo("Libro Objetivo");
    }
}