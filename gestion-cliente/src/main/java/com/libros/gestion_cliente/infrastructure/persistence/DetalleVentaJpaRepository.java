package com.libros.gestion_cliente.infrastructure.persistence;

import com.libros.gestion_cliente.domain.model.DetalleVenta;
import com.libros.gestion_cliente.domain.repository.DetalleVentaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleVentaJpaRepository extends JpaRepository<DetalleVenta, Long>, DetalleVentaRepository {
    // Spring genera el SQL automático para findByLibroId
}