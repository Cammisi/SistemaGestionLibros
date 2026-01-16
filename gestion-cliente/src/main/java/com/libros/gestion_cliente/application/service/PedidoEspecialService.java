package com.libros.gestion_cliente.application.service;

import com.libros.gestion_cliente.application.dto.CrearPedidoRequest;
import com.libros.gestion_cliente.domain.model.Cliente;
import com.libros.gestion_cliente.domain.model.EstadoPedido;
import com.libros.gestion_cliente.domain.model.PedidoEspecial;
import com.libros.gestion_cliente.domain.repository.ClienteRepository;
import com.libros.gestion_cliente.domain.repository.PedidoEspecialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoEspecialService {

    private final PedidoEspecialRepository pedidoRepository;
    private final ClienteRepository clienteRepository;

    @Transactional
    public PedidoEspecial crearPedido(CrearPedidoRequest request) {
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        PedidoEspecial pedido = PedidoEspecial.builder()
                .cliente(cliente)
                .descripcion(request.getDescripcion())
                .estado(EstadoPedido.PENDIENTE)
                .build();

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public PedidoEspecial cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        PedidoEspecial pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // Regla: No revivir muertos
        if (pedido.getEstado() == EstadoPedido.ENTREGADO || pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new IllegalStateException("No se puede modificar un pedido finalizado");
        }

        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoEspecial> listarPorCliente(Long clienteId) {
        // Necesitarás agregar findByClienteId en el repositorio
        return pedidoRepository.findByClienteId(clienteId);
    }

    @Transactional(readOnly = true)
    public List<PedidoEspecial> listarPendientes() {
        return pedidoRepository.findByEstado(EstadoPedido.PENDIENTE);
    }
}