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
        // 1. PREPARACIÓN (GIVEN)
        Cliente cliente = Cliente.builder().nombre("Pepe").apellido("Test").dni("999").build();
        clienteRepository.save(cliente);

        Libro libroTarget = Libro.builder().titulo("Libro Objetivo").autor("A").isbn("111").precio(BigDecimal.TEN).stock(10).build();
        Libro libroOtro = Libro.builder().titulo("Otro Libro").autor("B").isbn("222").precio(BigDecimal.TEN).stock(10).build();
        libroRepository.saveAll(List.of(libroTarget, libroOtro));

        // Creamos una venta con ambos libros
        Venta venta = Venta.builder().cliente(cliente).nroFactura(500).build();

        DetalleVenta d1 = DetalleVenta.builder().libro(libroTarget).cantidad(2).precioUnitario(BigDecimal.TEN).build();
        DetalleVenta d2 = DetalleVenta.builder().libro(libroOtro).cantidad(1).precioUnitario(BigDecimal.TEN).build();

        venta.addDetalle(d1);
        venta.addDetalle(d2);

        ventaRepository.save(venta); // Esto guarda los detalles automáticamente

        // 2. EJECUCIÓN (WHEN) - Buscamos solo los detalles del "Libro Objetivo"
        List<DetalleVenta> resultados = detalleVentaRepository.findByLibroId(libroTarget.getId());

        // 3. VERIFICACIÓN (THEN)
        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).getLibro().getTitulo()).isEqualTo("Libro Objetivo");
        assertThat(resultados.get(0).getCantidad()).isEqualTo(2);
    }
}