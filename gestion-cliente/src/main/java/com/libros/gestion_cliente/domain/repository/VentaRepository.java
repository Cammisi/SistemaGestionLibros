package com.libros.gestion_cliente.domain.repository;

import com.libros.gestion_cliente.domain.model.Venta;
import com.libros.gestion_cliente.domain.model.EstadoVenta;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VentaRepository {
    Venta save(Venta venta);
    <S extends Venta> List<S> saveAll(Iterable<S> ventas);
    Optional<Venta> findById(Long id);
    List<Venta> findAll();
    // En VentaRepository
    List<Venta> findByFechaVentaBetween(LocalDate inicio, LocalDate fin);

    // CAMBIO: Recibe String porque nro_factura es VARCHAR
    Optional<Venta> findByNroFactura(String nroFactura);
    // Buscar si el cliente tiene alguna venta activa (EN_PROCESO o PAGANDO)
    boolean existsByClienteIdAndEstado(Long clienteId, EstadoVenta estado);

    // Para ver el historial de compras de un cliente
    List<Venta> findByClienteId(Long clienteId);
}