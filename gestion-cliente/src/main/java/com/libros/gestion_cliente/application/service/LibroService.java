package com.libros.gestion_cliente.application.service;

import com.libros.gestion_cliente.application.dto.CrearLibroRequest;
import com.libros.gestion_cliente.domain.model.Libro;
import com.libros.gestion_cliente.domain.repository.LibroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LibroService {

    private final LibroRepository libroRepository;

    @Transactional
    public Libro crearLibro(CrearLibroRequest request) {
        if (libroRepository.existsByIsbn(request.getIsbn())) {
            throw new IllegalArgumentException("Ya existe un libro con el ISBN: " + request.getIsbn());
        }

        Libro libro = Libro.builder()
                .isbn(request.getIsbn())
                .titulo(request.getTitulo())
                .autor(request.getAutor())
                .tematica(request.getTematica())
                .precioBase(request.getPrecioBase())
                .stock(request.getStock())
                .cantVolumenes(request.getCantVolumenes() != null ? request.getCantVolumenes() : 1)
                .build();

        return libroRepository.save(libro);
    }

    @Transactional(readOnly = true)
    public Page<Libro> listarLibros(Pageable pageable) {
        return libroRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Libro buscarPorIsbn(String isbn) {
        return libroRepository.findByIsbn(isbn)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado con ISBN: " + isbn));
    }
}