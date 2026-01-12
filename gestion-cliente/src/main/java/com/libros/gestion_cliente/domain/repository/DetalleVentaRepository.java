package com.libros.gestion_cliente.domain.repository;

import com.libros.gestion_cliente.domain.model.DetalleVenta;
import java.util.List;
import java.util.Optional;

public interface DetalleVentaRepository {
    DetalleVenta save(DetalleVenta detalleVenta);
    Optional<DetalleVenta> findById(Long id);
    List<DetalleVenta> findAll();
    List<DetalleVenta> findByLibroId(Long libroId);
}