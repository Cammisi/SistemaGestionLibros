package com.libros.gestion_cliente.infrastructure.persistence;

import com.libros.gestion_cliente.domain.model.Libro;
import com.libros.gestion_cliente.domain.repository.LibroRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LibroJpaRepository extends JpaRepository<Libro, Long>, LibroRepository {
    // Spring implementa findByTituloContainingIgnoreCase automáticamente
    // traduciéndolo a: SELECT * FROM libros WHERE UPPER(titulo) LIKE UPPER('%titulo%')
}
