package com.libros.gestion_cliente.unit.application.service;

import com.libros.gestion_cliente.application.dto.CrearLibroRequest;
import com.libros.gestion_cliente.application.service.LibroService;
import com.libros.gestion_cliente.domain.model.Libro;
import com.libros.gestion_cliente.domain.repository.LibroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibroServiceTest {

    @Mock private LibroRepository libroRepository;
    @InjectMocks private LibroService libroService;

    @Test
    void crearLibro_DeberiaGuardar_CuandoIsbnNoExiste() {
        CrearLibroRequest request = new CrearLibroRequest();
        request.setIsbn("123");
        request.setTitulo("Java");
        request.setPrecioBase(BigDecimal.TEN);
        request.setStock(10);
        request.setAutor("Yo");

        when(libroRepository.existsByIsbn("123")).thenReturn(false);
        when(libroRepository.save(any(Libro.class))).thenAnswer(i -> i.getArgument(0));

        Libro result = libroService.crearLibro(request);

        assertThat(result.getIsbn()).isEqualTo("123");
        verify(libroRepository).save(any(Libro.class));
    }

    @Test
    void crearLibro_DeberiaLanzarExcepcion_CuandoIsbnExiste() {
        CrearLibroRequest request = new CrearLibroRequest();
        request.setIsbn("123");

        when(libroRepository.existsByIsbn("123")).thenReturn(true);

        assertThatThrownBy(() -> libroService.crearLibro(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe");
    }

    @Test
    void listarLibros_DeberiaRetornarPagina() {
        // GIVEN
        Pageable pageable = Pageable.unpaged();
        // Mockeamos que el repositorio devuelve una Page
        Page<Libro> paginaMock = new PageImpl<>(List.of(new Libro()));
        when(libroRepository.findAll(pageable)).thenReturn(paginaMock);

        // WHEN
        Page<Libro> resultado = libroService.listarLibros(pageable);

        // THEN
        assertThat(resultado).hasSize(1);
    }

    @Test
    void buscarPorIsbn_DeberiaRetornarLibro_CuandoExiste() {
        when(libroRepository.findByIsbn("123")).thenReturn(Optional.of(new Libro()));
        assertThat(libroService.buscarPorIsbn("123")).isNotNull();
    }

    @Test
    void buscarPorIsbn_DeberiaLanzarExcepcion_CuandoNoExiste() {
        when(libroRepository.findByIsbn("999")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> libroService.buscarPorIsbn("999"))
                .isInstanceOf(RuntimeException.class);
    }

    // --- NUEVO TEST 1: Cubre la rama FALSE del ternario (request es NULL -> usa 1) ---
    @Test
    void crearLibro_DeberiaAsignar1Volumen_CuandoInputEsNull() {
        // GIVEN
        CrearLibroRequest request = new CrearLibroRequest();
        request.setIsbn("LIB-001");
        request.setCantVolumenes(null); // <--- FORZAMOS NULL

        when(libroRepository.existsByIsbn("LIB-001")).thenReturn(false);
        // Capturamos el libro que se intenta guardar para inspeccionarlo
        when(libroRepository.save(any(Libro.class))).thenAnswer(i -> i.getArgument(0));

        // WHEN
        Libro resultado = libroService.crearLibro(request);

        // THEN
        assertThat(resultado.getCantVolumenes()).isEqualTo(1); // Debe ser el default
    }

    // --- NUEVO TEST 2: Cubre la rama TRUE del ternario (request tiene VALOR -> usa VALOR) ---
    @Test
    void crearLibro_DeberiaAsignarVolumenesDelRequest_CuandoNoEsNull() {
        // GIVEN
        CrearLibroRequest request = new CrearLibroRequest();
        request.setIsbn("LIB-002");
        request.setCantVolumenes(5); // <--- FORZAMOS VALOR

        when(libroRepository.existsByIsbn("LIB-002")).thenReturn(false);
        when(libroRepository.save(any(Libro.class))).thenAnswer(i -> i.getArgument(0));

        // WHEN
        Libro resultado = libroService.crearLibro(request);

        // THEN
        assertThat(resultado.getCantVolumenes()).isEqualTo(5); // Debe respetar el input
    }
}