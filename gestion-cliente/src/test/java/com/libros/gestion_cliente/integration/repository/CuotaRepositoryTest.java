package com.libros.gestion_cliente.integration.repository;

import com.libros.gestion_cliente.domain.model.*;
import com.libros.gestion_cliente.domain.repository.ClienteRepository;
import com.libros.gestion_cliente.domain.repository.CuotaRepository;
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
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CuotaRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired private CuotaRepository cuotaRepository;
    @Autowired private VentaRepository ventaRepository;
    @Autowired private ClienteRepository clienteRepository;

    @Test
    void deberiaGuardarYBuscarCuotas() {
        // GIVEN: Necesitamos Cliente y Venta previos
        Cliente cliente = Cliente.builder().nombre("Juan").apellido("Perez").dni("111").build();
        clienteRepository.save(cliente);

        Venta venta = Venta.builder().cliente(cliente).nroFactura("FAC-001").build();
        ventaRepository.save(venta);

        Cuota c1 = Cuota.builder()
                .venta(venta)
                .numeroCuota(1)
                .montoCuota(new BigDecimal("500.00"))
                .fechaVencimiento(LocalDate.now().plusDays(30))
                .estado(EstadoCuota.PENDIENTE)
                .build();

        // WHEN
        cuotaRepository.save(c1);

        // THEN
        List<Cuota> pendientes = cuotaRepository.findByEstado(EstadoCuota.PENDIENTE);
        assertThat(pendientes).hasSize(1);
        assertThat(pendientes.get(0).getMontoCuota()).isEqualByComparingTo(new BigDecimal("500.00"));
    }
}