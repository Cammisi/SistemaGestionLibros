package com.libros.gestion_cliente.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.libros.gestion_cliente.application.dto.CrearLibroRequest;
import com.libros.gestion_cliente.application.service.LibroService;
import com.libros.gestion_cliente.domain.model.Libro;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibroController.class)
class LibroControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private LibroService libroService;

    @Test
    void crearLibro_DeberiaRetornar201_CuandoEsValido() throws Exception {
        CrearLibroRequest request = new CrearLibroRequest();
        request.setIsbn("999");
        request.setTitulo("Clean Code");
        request.setAutor("Uncle Bob");
        request.setPrecioBase(new BigDecimal("50.00"));
        request.setStock(10);

        Libro libroMock = Libro.builder().isbn("999").titulo("Clean Code").build();
        when(libroService.crearLibro(any())).thenReturn(libroMock);

        mockMvc.perform(post("/api/libros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isbn").value("999"));
    }

    @Test
    void crearLibro_DeberiaRetornar400_CuandoFaltanDatos() throws Exception {
        CrearLibroRequest request = new CrearLibroRequest();
        // Request vacío
        mockMvc.perform(post("/api/libros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarLibros_DeberiaRetornar200() throws Exception {
        when(libroService.listarLibros()).thenReturn(List.of(new Libro()));
        mockMvc.perform(get("/api/libros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void buscarPorIsbn_DeberiaRetornar200() throws Exception {
        when(libroService.buscarPorIsbn("123")).thenReturn(new Libro());
        mockMvc.perform(get("/api/libros/123"))
                .andExpect(status().isOk());
    }
}