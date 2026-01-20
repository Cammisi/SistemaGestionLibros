package com.libros.gestion_cliente.domain.repository;

import com.libros.gestion_cliente.application.dto.ReporteItem;
import com.libros.gestion_cliente.domain.model.Venta;
import com.libros.gestion_cliente.domain.model.EstadoVenta;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VentaRepository {
    // Métodos CRUD básicos
    Venta save(Venta venta);
    <S extends Venta> List<S> saveAll(Iterable<S> ventas);
    Optional<Venta> findById(Long id);
    List<Venta> findAll();
    List<Venta> findAll(Sort sort);

    // Consultas específicas
    List<Venta> findByFechaVentaBetween(LocalDate inicio, LocalDate fin);
    Optional<Venta> findByNroFactura(String nroFactura);
    boolean existsByClienteIdAndEstado(Long clienteId, EstadoVenta estado);
    List<Venta> findByClienteId(Long clienteId);

    // --- MÉTODOS ESPECIALES (Las firmas van aquí) ---

    // 1. Historial con Cliente cargado (JOIN FETCH)
    List<Venta> findAllWithCliente(Sort sort);

    // 2. Reporte: Top Libros
    List<ReporteItem> obtenerLibrosMasVendidos();

    // 3. Reporte: Top Clientes
    List<ReporteItem> obtenerMejoresClientes();

    // ... otros métodos ...

    // Obtener los títulos de los libros comprados por un cliente
    @Query("SELECT DISTINCT l.titulo FROM DetalleVenta dv JOIN dv.venta v JOIN dv.libro l WHERE v.cliente.id = :clienteId")
    List<String> findLibrosCompradosPorCliente(Long clienteId);
}