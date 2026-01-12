package com.libros.gestion_cliente.domain.repository;

import com.libros.gestion_cliente.domain.model.Libro;
import java.util.List;
import java.util.Optional;

public interface LibroRepository {
    Libro save(Libro libro);
    <S extends Libro> List<S> saveAll(Iterable<S> libros);
    Optional<Libro> findById(Long id);
    Optional<Libro> findByIsbn(String isbn);
    List<Libro> findAll();
    void deleteById(Long id);
    boolean existsByIsbn(String isbn);

    // Búsqueda flexible para el mostrador (ej: buscar "Quijote" y que traiga "Don Quijote")
    List<Libro> findByTituloContainingIgnoreCase(String titulo);
}