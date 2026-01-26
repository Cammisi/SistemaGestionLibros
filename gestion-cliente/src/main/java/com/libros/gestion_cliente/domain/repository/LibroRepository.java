package com.libros.gestion_cliente.domain.repository;

import com.libros.gestion_cliente.domain.model.Libro;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LibroRepository {
    Libro save(Libro libro);
    <S extends Libro> List<S> saveAll(Iterable<S> libros);
    Optional<Libro> findById(Long id);
    Optional<Libro> findByIsbn(String isbn);
    List<Libro> findByTituloContainingIgnoreCase(String tituloPart);
    List<Libro> findAll();
    void deleteById(Long id);
    boolean existsByIsbn(String isbn);
    Page<Libro> findAll(Pageable pageable);
    List<Libro> findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(String titulo, String autor);
}