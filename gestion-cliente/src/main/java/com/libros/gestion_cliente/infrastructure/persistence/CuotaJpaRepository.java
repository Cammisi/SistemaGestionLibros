package com.libros.gestion_cliente.infrastructure.persistence;

import com.libros.gestion_cliente.domain.model.Cuota;
import com.libros.gestion_cliente.domain.repository.CuotaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CuotaJpaRepository extends JpaRepository<Cuota, Long>, CuotaRepository {
    // QUERY OPTIMIZADA:
    // 1. Filtra por el ID del cliente dentro de la venta.
    // 2. Hace JOIN FETCH de 'venta' para que Hibernate la traiga lista para usar.
    @Override
    @Query("SELECT c FROM Cuota c JOIN FETCH c.venta v WHERE v.cliente.id = :clienteId")
    List<Cuota> findByClienteId(Long clienteId);
}