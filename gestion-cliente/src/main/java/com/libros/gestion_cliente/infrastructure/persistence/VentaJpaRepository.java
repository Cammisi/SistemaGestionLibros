package com.libros.gestion_cliente.infrastructure.persistence;

import com.libros.gestion_cliente.domain.model.Venta;
import com.libros.gestion_cliente.domain.repository.VentaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VentaJpaRepository extends JpaRepository<Venta, Long>, VentaRepository {
    // Spring genera automáticamente el SQL para findByNroFactura
}