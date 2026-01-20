package com.libros.gestion_cliente.infrastructure.persistence;

import com.libros.gestion_cliente.domain.model.Cliente;
import com.libros.gestion_cliente.domain.repository.ClienteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteJpaRepository extends JpaRepository<Cliente, Long>, ClienteRepository {
    // No necesitas re-escribir nada aquí.
    // Spring Data ve que 'ClienteRepository' tiene una @Query en 'findClientesLibresDeDeuda'
    // y la implementa automáticamente al arrancar.
    // También implementa 'findByInteresesPersonalesContainingIgnoreCase' analizando el nombre.
}