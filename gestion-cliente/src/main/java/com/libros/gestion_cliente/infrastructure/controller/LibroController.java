package com.libros.gestion_cliente.infrastructure.controller;

import com.libros.gestion_cliente.application.dto.CrearLibroRequest;
import com.libros.gestion_cliente.application.service.LibroService;
import com.libros.gestion_cliente.domain.model.Libro;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/libros")
@RequiredArgsConstructor
@Tag(name = "Libros", description = "Gestión del inventario de libros")
public class LibroController {

    private final LibroService libroService;

    @PostMapping
    @Operation(summary = "Registrar nuevo libro")
    public ResponseEntity<Libro> crearLibro(@Valid @RequestBody CrearLibroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(libroService.crearLibro(request));
    }

    @GetMapping
    @Operation(summary = "Listar inventario paginado", description = "Ejemplo: /api/libros?page=0&size=10&sort=titulo,asc")
    public ResponseEntity<Page<Libro>> listarLibros(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(libroService.listarLibros(pageable));
    }

    @GetMapping("/{isbn}")
    @Operation(summary = "Buscar libro por ISBN")
    public ResponseEntity<Libro> buscarPorIsbn(@PathVariable String isbn) {
        return ResponseEntity.ok(libroService.buscarPorIsbn(isbn));
    }
}