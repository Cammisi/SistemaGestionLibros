package com.libros.gestion_cliente.domain.repository;

import com.libros.gestion_cliente.domain.model.Venta;
import java.util.List;
import java.util.Optional;

public interface VentaRepository {
    Venta save(Venta venta);

    // Firma genérica para evitar el conflicto con JPA
    <S extends Venta> List<S> saveAll(Iterable<S> ventas);

    Optional<Venta> findById(Long id);
    List<Venta> findAll();

    // Búsqueda clave por número de factura
    Optional<Venta> findByNroFactura(Integer nroFactura);
}