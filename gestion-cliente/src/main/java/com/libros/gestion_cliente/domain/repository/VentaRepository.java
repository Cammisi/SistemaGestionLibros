package com.libros.gestion_cliente.domain.repository;

import com.libros.gestion_cliente.domain.model.Venta;
import java.util.List;
import java.util.Optional;

public interface VentaRepository {
    Venta save(Venta venta);
    <S extends Venta> List<S> saveAll(Iterable<S> ventas);
    Optional<Venta> findById(Long id);
    List<Venta> findAll();

    // CAMBIO: Recibe String porque nro_factura es VARCHAR
    Optional<Venta> findByNroFactura(String nroFactura);
}