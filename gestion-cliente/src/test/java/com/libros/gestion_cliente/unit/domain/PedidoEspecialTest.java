package com.libros.gestion_cliente.unit.domain;

import com.libros.gestion_cliente.domain.model.Cliente;
import com.libros.gestion_cliente.domain.model.EstadoPedido;
import com.libros.gestion_cliente.domain.model.PedidoEspecial;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PedidoEspecialTest {

    @Test
    void deberiaCrearPedidoConEstadoInicialPendiente() {
        // GIVEN
        Cliente cliente = new Cliente();

        // WHEN
        PedidoEspecial pedido = PedidoEspecial.builder()
                .cliente(cliente)
                .descripcion("Libro importado")
                .build();

        // THEN
        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.PENDIENTE); // Default verificado
        assertThat(pedido.getCliente()).isEqualTo(cliente);
        assertThat(pedido.getDescripcion()).isEqualTo("Libro importado");
    }

    @Test
    void deberiaCambiarEstadoAEntregado() {
        // GIVEN
        PedidoEspecial pedido = new PedidoEspecial();

        // WHEN
        pedido.setEstado(EstadoPedido.ENTREGADO);

        // THEN
        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.ENTREGADO);
    }
}