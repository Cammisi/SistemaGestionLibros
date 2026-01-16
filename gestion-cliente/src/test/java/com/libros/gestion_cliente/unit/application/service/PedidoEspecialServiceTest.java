package com.libros.gestion_cliente.unit.application.service;

import com.libros.gestion_cliente.application.dto.CrearPedidoRequest;
import com.libros.gestion_cliente.application.service.PedidoEspecialService;
import com.libros.gestion_cliente.domain.model.Cliente;
import com.libros.gestion_cliente.domain.model.EstadoPedido;
import com.libros.gestion_cliente.domain.model.PedidoEspecial;
import com.libros.gestion_cliente.domain.repository.ClienteRepository;
import com.libros.gestion_cliente.domain.repository.PedidoEspecialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoEspecialServiceTest {

    @Mock private PedidoEspecialRepository pedidoRepository;
    @Mock private ClienteRepository clienteRepository;
    @InjectMocks private PedidoEspecialService pedidoService;

    // --- 1. Crear Pedido ---
    @Test
    void crearPedido_DeberiaGuardar_CuandoClienteExiste() {
        // GIVEN
        CrearPedidoRequest request = new CrearPedidoRequest();
        request.setClienteId(1L);
        request.setDescripcion("Libro Importado");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(new Cliente()));
        when(pedidoRepository.save(any(PedidoEspecial.class))).thenAnswer(i -> i.getArgument(0));

        // WHEN
        PedidoEspecial resultado = pedidoService.crearPedido(request);

        // THEN
        assertThat(resultado.getEstado()).isEqualTo(EstadoPedido.PENDIENTE);
        assertThat(resultado.getDescripcion()).isEqualTo("Libro Importado");
        verify(pedidoRepository).save(any(PedidoEspecial.class));
    }

    @Test
    void crearPedido_DeberiaLanzarExcepcion_CuandoClienteNoExiste() {
        CrearPedidoRequest request = new CrearPedidoRequest();
        request.setClienteId(99L);

        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.crearPedido(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cliente no encontrado");
    }

    // --- 2. Cambiar Estado ---
    @Test
    void cambiarEstado_DeberiaActualizar_CuandoEsValido() {
        PedidoEspecial pedido = PedidoEspecial.builder().id(1L).estado(EstadoPedido.PENDIENTE).build();

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(PedidoEspecial.class))).thenAnswer(i -> i.getArgument(0));

        PedidoEspecial resultado = pedidoService.cambiarEstado(1L, EstadoPedido.DISPONIBLE);

        assertThat(resultado.getEstado()).isEqualTo(EstadoPedido.DISPONIBLE);
    }

    @Test
    void cambiarEstado_DeberiaLanzarExcepcion_SiPedidoYaFinalizo() {
        // GIVEN: Un pedido que ya fue ENTREGADO
        PedidoEspecial pedido = PedidoEspecial.builder().id(1L).estado(EstadoPedido.ENTREGADO).build();

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        // WHEN / THEN: Intentar cambiarlo debe fallar
        assertThatThrownBy(() -> pedidoService.cambiarEstado(1L, EstadoPedido.PENDIENTE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se puede modificar un pedido finalizado");
    }

    @Test
    void cambiarEstado_DeberiaLanzarExcepcion_SiNoExiste() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.cambiarEstado(99L, EstadoPedido.DISPONIBLE))
                .isInstanceOf(RuntimeException.class);
    }

    // --- 3. Listados ---
    @Test
    void listarPorCliente_DeberiaRetornarLista() {
        when(pedidoRepository.findByClienteId(1L)).thenReturn(List.of(new PedidoEspecial()));
        assertThat(pedidoService.listarPorCliente(1L)).hasSize(1);
    }

    @Test
    void listarPendientes_DeberiaRetornarLista() {
        when(pedidoRepository.findByEstado(EstadoPedido.PENDIENTE)).thenReturn(List.of(new PedidoEspecial()));
        assertThat(pedidoService.listarPendientes()).hasSize(1);
    }

    @Test
    void cambiarEstado_DeberiaLanzarExcepcion_SiPedidoEstabaCancelado() {
        // GIVEN: Un pedido que ya fue CANCELADO
        PedidoEspecial pedido = PedidoEspecial.builder().id(1L).estado(EstadoPedido.CANCELADO).build();

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        // WHEN / THEN: Intentar revivirlo debe fallar
        // Esto fuerza a Java a evaluar la segunda parte del OR ( || pedido.getEstado() == CANCELADO )
        assertThatThrownBy(() -> pedidoService.cambiarEstado(1L, EstadoPedido.PENDIENTE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se puede modificar un pedido finalizado");
    }
}