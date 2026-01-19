package com.libros.gestion_cliente.domain.repository;

import com.libros.gestion_cliente.domain.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

public interface ClienteRepository {
    Cliente save(Cliente cliente);
    Optional<Cliente> findById(Long id);
    Optional<Cliente> findByDni(String dni);
    List<Cliente> findAll();
    Page<Cliente> findAll(Pageable pageable);
    void deleteById(Long id);
    boolean existsByDni(String dni);
    // Búsqueda flexible (ej: "Santa" encuentra "Santa Fe")
    List<Cliente> findByLocalidadContainingIgnoreCase(String localidad);

    // Búsqueda por interés (ej: "Cocina")
    List<Cliente> findByInteresesPersonalesContainingIgnoreCase(String interes);

    // Busca clientes donde NO exista ninguna venta asociada con estado 'EN_PROCESO'
    @Query("SELECT c FROM Cliente c WHERE NOT EXISTS (SELECT v FROM Venta v WHERE v.cliente = c AND v.estado = 'EN_PROCESO') ORDER BY c.localidad")
    List<Cliente> findClientesLibresDeDeuda();
}