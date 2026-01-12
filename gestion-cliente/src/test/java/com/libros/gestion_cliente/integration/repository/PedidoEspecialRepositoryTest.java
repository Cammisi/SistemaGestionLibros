package com.libros.gestion_cliente.integration.repository;

import com.libros.gestion_cliente.domain.model.Cliente;
import com.libros.gestion_cliente.domain.model.EstadoPedido;
import com.libros.gestion_cliente.domain.model.PedidoEspecial;
import com.libros.gestion_cliente.domain.repository.ClienteRepository;
import com.libros.gestion_cliente.domain.repository.PedidoEspecialRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PedidoEspecialRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withInitScript("init.sql");

    @Autowired private PedidoEspecialRepository pedidoRepository;
    @Autowired private ClienteRepository clienteRepository;

    @Test
    void deberiaGestionarPedidos() {
        // GIVEN
        Cliente cliente = Cliente.builder().nombre("Maria").apellido("Gomez").dni("222").build();
        clienteRepository.save(cliente);

        PedidoEspecial pedido = PedidoEspecial.builder()
                .cliente(cliente)
                .descripcion("Libro raro de 1990")
                .estado(EstadoPedido.PENDIENTE_COMPRA)
                .build();

        // WHEN
        pedidoRepository.save(pedido);

        // THEN
        List<PedidoEspecial> encontrados = pedidoRepository.findByClienteId(cliente.getId());
        assertThat(encontrados).isNotEmpty();
        assertThat(encontrados.get(0).getDescripcion()).isEqualTo("Libro raro de 1990");
    }
}