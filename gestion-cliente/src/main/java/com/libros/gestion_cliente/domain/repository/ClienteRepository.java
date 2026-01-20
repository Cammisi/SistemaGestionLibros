package com.libros.gestion_cliente.domain.repository;

import com.libros.gestion_cliente.domain.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query; // Importante para la consulta custom

import java.util.List;
import java.util.Optional;

public interface ClienteRepository {
    // --- Métodos CRUD Básicos ---
    Cliente save(Cliente cliente);
    Optional<Cliente> findById(Long id);
    Optional<Cliente> findByDni(String dni);
    List<Cliente> findAll();
    Page<Cliente> findAll(Pageable pageable);
    void deleteById(Long id);
    boolean existsByDni(String dni);

    // --- Métodos de Búsqueda Flexible ---

    // Búsqueda por localidad (Spring Data lo implementa solo por el nombre)
    List<Cliente> findByLocalidadContainingIgnoreCase(String localidad);

    // --- NUEVOS MÉTODOS PARA REPORTES ---

    // 1. Marketing: Búsqueda por interés (ej: "Cocina")
    // Spring Data crea la consulta automáticamente basándose en el nombre del método
    List<Cliente> findByInteresesPersonalesContainingIgnoreCase(String interes);

    // 2. Hoja de Ruta: Clientes Libres de Deuda (HU-02)
    // Usamos @Query aquí para definir la lógica compleja
    @Query("SELECT c FROM Cliente c WHERE NOT EXISTS " +
            "(SELECT v FROM Venta v WHERE v.cliente = c AND v.estado = 'EN_PROCESO') " +
            "ORDER BY c.localidad, c.apellido")
    List<Cliente> findClientesLibresDeDeuda();
}