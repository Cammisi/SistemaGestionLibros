package com.libros.gestion_cliente.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.libros.gestion_cliente.application.dto.CrearFamiliarRequest;
import com.libros.gestion_cliente.application.service.FamiliarService;
import com.libros.gestion_cliente.domain.model.Familiar;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FamiliarController.class)
class FamiliarControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private FamiliarService familiarService;

    @Test
    void agregarFamiliar_DeberiaRetornar201() throws Exception {
        CrearFamiliarRequest request = new CrearFamiliarRequest();
        request.setClienteId(1L);
        request.setNombre("Pepe");
        request.setApellido("Argento");
        request.setRelacion("Padre");

        Familiar familiar = Familiar.builder().id(1L).nombre("Pepe").build();
        when(familiarService.agregarFamiliar(any())).thenReturn(familiar);

        mockMvc.perform(post("/api/familiares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Pepe"));
    }

    @Test
    void listarPorCliente_DeberiaRetornar200() throws Exception {
        when(familiarService.listarPorCliente(1L)).thenReturn(List.of(new Familiar()));

        mockMvc.perform(get("/api/familiares/cliente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}