package com.libros.gestion_cliente.infrastructure.persistence;

import com.libros.gestion_cliente.domain.model.Familiar;
import com.libros.gestion_cliente.domain.repository.FamiliarRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FamiliarJpaRepository extends JpaRepository<Familiar, Long>, FamiliarRepository {
    // Spring genera automáticamente el SQL para findByClienteId y findByApellido
}
