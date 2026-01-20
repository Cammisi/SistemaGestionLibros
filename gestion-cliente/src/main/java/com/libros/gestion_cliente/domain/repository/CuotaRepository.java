package com.libros.gestion_cliente.domain.repository;

import com.libros.gestion_cliente.domain.model.Cuota;
import com.libros.gestion_cliente.domain.model.EstadoCuota;
import java.util.List;
import java.util.Optional;

public interface CuotaRepository {
    Cuota save(Cuota cuota);
    <S extends Cuota> List<S> saveAll(Iterable<S> cuotas);
    Optional<Cuota> findById(Long id);
    List<Cuota> findAll();

    // Consultas útiles para el negocio
    List<Cuota> findByVentaId(Long ventaId);
    List<Cuota> findByEstado(EstadoCuota estado);
    long countByVentaIdAndEstado(Long ventaId, EstadoCuota estado);
    List<Cuota> findByClienteId(Long clienteId);
}