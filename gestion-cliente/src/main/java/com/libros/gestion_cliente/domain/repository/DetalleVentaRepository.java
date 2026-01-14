package com.libros.gestion_cliente.domain.repository;

import com.libros.gestion_cliente.domain.model.DetalleVenta;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DetalleVentaRepository {
    DetalleVenta save(DetalleVenta detalleVenta);
    Optional<DetalleVenta> findById(Long id);
    List<DetalleVenta> findAll();
    List<DetalleVenta> findByLibroId(Long libroId);
    // ¿Existe algún detalle con este libro para este cliente en cualquier venta?
    @Query("SELECT COUNT(d) > 0 FROM DetalleVenta d WHERE d.libro.id = :libroId AND d.venta.cliente.id = :clienteId")
    boolean haCompradoLibro(Long clienteId, Long libroId);
}