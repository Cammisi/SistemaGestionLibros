package com.libros.gestion_cliente.domain.repository;

import com.libros.gestion_cliente.domain.model.EstadoPedido;
import com.libros.gestion_cliente.domain.model.PedidoEspecial;
import java.util.List;
import java.util.Optional;

public interface PedidoEspecialRepository {
    PedidoEspecial save(PedidoEspecial pedido);
    Optional<PedidoEspecial> findById(Long id);
    List<PedidoEspecial> findAll();

    List<PedidoEspecial> findByClienteId(Long clienteId);
    List<PedidoEspecial> findByEstado(EstadoPedido estado);
}