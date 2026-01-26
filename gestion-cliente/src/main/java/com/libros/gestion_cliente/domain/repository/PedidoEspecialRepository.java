package com.libros.gestion_cliente.domain.repository;

import com.libros.gestion_cliente.domain.model.EstadoPedido;
import com.libros.gestion_cliente.domain.model.PedidoEspecial;
import org.springframework.data.jpa.repository.JpaRepository; // Importar esto
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoEspecialRepository extends JpaRepository<PedidoEspecial, Long> {

    List<PedidoEspecial> findByClienteId(Long clienteId);
    List<PedidoEspecial> findByEstado(EstadoPedido estado);

    // --- TUS QUERIES OPTIMIZADAS (LEFT JOIN FETCH) ---
    @Query("SELECT p FROM PedidoEspecial p LEFT JOIN FETCH p.cliente")
    List<PedidoEspecial> findAllConCliente();

    @Query("SELECT p FROM PedidoEspecial p LEFT JOIN FETCH p.cliente WHERE p.estado = :estado")
    List<PedidoEspecial> findByEstadoConCliente(EstadoPedido estado);
}