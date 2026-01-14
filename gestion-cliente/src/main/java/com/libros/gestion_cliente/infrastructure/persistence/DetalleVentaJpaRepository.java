package com.libros.gestion_cliente.infrastructure.persistence;

import com.libros.gestion_cliente.domain.model.DetalleVenta;
import com.libros.gestion_cliente.domain.repository.DetalleVentaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface DetalleVentaJpaRepository extends JpaRepository<DetalleVenta, Long>, DetalleVentaRepository {
    // Spring genera el SQL automático para findByLibroId
    // Aquí implementamos la query JPQL definida en la interfaz del dominio
    @Override
    @Query("SELECT COUNT(d) > 0 FROM DetalleVenta d WHERE d.libro.id = :libroId AND d.venta.cliente.id = :clienteId")
    boolean haCompradoLibro(@Param("clienteId") Long clienteId, @Param("libroId") Long libroId);
}