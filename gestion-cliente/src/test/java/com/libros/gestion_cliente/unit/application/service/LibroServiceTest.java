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
    void listarLibros_DeberiaRetornarLista() {
        when(libroRepository.findAll()).thenReturn(List.of(new Libro()));
        assertThat(libroService.listarLibros()).hasSize(1);
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
}