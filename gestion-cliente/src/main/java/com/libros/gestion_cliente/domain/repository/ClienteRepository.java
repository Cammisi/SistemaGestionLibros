package com.libros.gestion_cliente.domain.repository;

import com.libros.gestion_cliente.domain.model.Cliente;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository {
    Cliente save(Cliente cliente);
    Optional<Cliente> findById(Long id);
    Optional<Cliente> findByDni(String dni);
    List<Cliente> findAll();
    void deleteById(Long id);
    boolean existsByDni(String dni);
}