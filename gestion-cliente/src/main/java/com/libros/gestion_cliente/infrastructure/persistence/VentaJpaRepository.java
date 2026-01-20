package com.libros.gestion_cliente.infrastructure.persistence;

import com.libros.gestion_cliente.application.dto.ReporteItem;
import com.libros.gestion_cliente.domain.model.Venta;
import com.libros.gestion_cliente.domain.repository.VentaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaJpaRepository extends JpaRepository<Venta, Long>, VentaRepository {

    // Spring Data implementa automáticamente los findBy... gracias a los nombres
    // findByNroFactura, findByFechaVentaBetween, etc. no necesitan @Query aquí
    // si siguen la convención de nombres estándar.

    // --- IMPLEMENTACIONES MANUALES CON JPQL ---

    @Override
    @Query("SELECT v FROM Venta v JOIN FETCH v.cliente")
    List<Venta> findAllWithCliente(Sort sort);

    @Override
    @Query("SELECT new com.libros.gestion_cliente.application.dto.ReporteItem(l.titulo, SUM(d.cantidad)) " +
            "FROM DetalleVenta d JOIN d.libro l " +
            "GROUP BY l.titulo " +
            "ORDER BY SUM(d.cantidad) DESC LIMIT 5")
    List<ReporteItem> obtenerLibrosMasVendidos();

    @Override
    @Query("SELECT new com.libros.gestion_cliente.application.dto.ReporteItem(c.nombre || ' ' || c.apellido, SUM(v.montoTotal)) " +
            "FROM Venta v JOIN v.cliente c " +
            "WHERE v.estado = 'FINALIZADA' OR v.estado = 'EN_PROCESO' " +
            "GROUP BY c.id, c.nombre, c.apellido " +
            "ORDER BY SUM(v.montoTotal) DESC LIMIT 5")
    List<ReporteItem> obtenerMejoresClientes();
}