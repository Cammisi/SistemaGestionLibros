package com.libros.gestion_cliente.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.libros.gestion_cliente.application.dto.CrearVentaRequest;
import com.libros.gestion_cliente.application.service.VentaService;
import com.libros.gestion_cliente.domain.model.Venta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VentaController.class) // Solo levanta el contexto web para este controlador
class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc; // Simula peticiones HTTP

    @Autowired
    private ObjectMapper objectMapper; // Convierte objetos a JSON

    @MockitoBean
    private VentaService ventaService; // Simulamos el servicio (ya está probado aparte)

    @Test
    void registrarVenta_DeberiaRetornar201_CuandoEsExitoso() throws Exception {
        // GIVEN
        CrearVentaRequest request = new CrearVentaRequest();
        request.setClienteId(1L);
        request.setCantidadCuotas(3);
        CrearVentaRequest.ItemVenta item = new CrearVentaRequest.ItemVenta();
        item.setLibroId(10L);
        item.setCantidad(1);
        request.setItems(List.of(item));

        // Simulamos que el servicio devuelve una venta vacía (no nos importan los detalles aquí, solo el flujo HTTP)
        when(ventaService.registrarVenta(any(CrearVentaRequest.class))).thenReturn(new Venta());

        // WHEN & THEN
        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) // Esperamos 201
                .andExpect(jsonPath("$").exists()); // Esperamos un JSON de respuesta
    }

    @Test
    void registrarVenta_DeberiaRetornar400_CuandoHayErrorDeNegocio() throws Exception {
        // GIVEN
        CrearVentaRequest request = new CrearVentaRequest();
        request.setClienteId(1L);
        request.setCantidadCuotas(1);
        CrearVentaRequest.ItemVenta item = new CrearVentaRequest.ItemVenta();
        item.setLibroId(1L);
        item.setCantidad(1);
        request.setItems(List.of(item));

        // Simulamos que el servicio lanza excepción por Stock o Deuda
        when(ventaService.registrarVenta(any())).thenThrow(new IllegalStateException("Stock insuficiente"));

        // WHEN & THEN
        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // Esperamos 400
                .andExpect(jsonPath("$.error").value("Error de Negocio"))
                .andExpect(jsonPath("$.message").value("Stock insuficiente"));
    }


    @Test
    void registrarVenta_DeberiaRetornar400_CuandoElJsonEsInvalido() throws Exception {
        // ARRANGE: Creamos un request inválido (sin clienteId)
        CrearVentaRequest requestInvalido = new CrearVentaRequest();
        requestInvalido.setClienteId(null); // <--- ERROR PROVOCADO
        requestInvalido.setCantidadCuotas(3);
        // La lista de items podría estar vacía o nula también para provocar errores

        // ACT & ASSERT
        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest()); // Esperamos 400

        // No hace falta mockear el servicio aquí porque @Valid
        // detiene la ejecución ANTES de llamar al servicio.
    }

    @Test
    void registrarVenta_DeberiaRetornar400_CuandoFallaLaLogicaDeNegocio() throws Exception {
        // ARRANGE: Un request válido en estructura...
        CrearVentaRequest request = new CrearVentaRequest();
        request.setClienteId(1L);
        request.setCantidadCuotas(1);
        CrearVentaRequest.ItemVenta item = new CrearVentaRequest.ItemVenta();
        item.setLibroId(1L);
        item.setCantidad(9999); // Pide mucho stock
        request.setItems(List.of(item));

        // ...PERO el servicio dice "NO" (Simulamos la excepción)
        // Usamos any() para no complicarnos con el matching del objeto exacto
        Mockito.when(ventaService.registrarVenta(Mockito.any()))
                .thenThrow(new IllegalStateException("Stock insuficiente para el libro"));

        // ACT & ASSERT
        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // 400
                .andExpect(jsonPath("$.error").value("Error de Negocio")) // Validamos tu JSON de error
                .andExpect(jsonPath("$.message").value("Stock insuficiente para el libro"));
    }

    @Test
    void registrarVenta_DeberiaRetornar404_CuandoNoEncuentraRecurso() throws Exception {
        // GIVEN
        CrearVentaRequest request = new CrearVentaRequest();
        request.setClienteId(99L);
        request.setCantidadCuotas(1);
        CrearVentaRequest.ItemVenta item = new CrearVentaRequest.ItemVenta();
        item.setLibroId(1L);
        item.setCantidad(1);
        request.setItems(List.of(item));

        // Simulamos que el servicio no encuentra al cliente
        when(ventaService.registrarVenta(any())).thenThrow(new RuntimeException("Cliente no encontrado"));

        // WHEN & THEN
        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound()) // Esperamos 404
                .andExpect(jsonPath("$.error").value("Recurso no encontrado"));
    }

    // --- NUEVO TEST 1: Cubre handleJsonError (0% -> 100%) ---
    @Test
    void registrarVenta_DeberiaRetornar400_CuandoJsonEstaMalFormado() throws Exception {
        // GIVEN: Un JSON con sintaxis rota (le falta la llave de cierre)
        String jsonMalformado = "{ \"clienteId\": 1, \"cantidadCuotas\": 3 ";

        // WHEN & THEN
        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMalformado))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Formato JSON Inválido"));
    }

    // --- NUEVO TEST 2: Cubre el 'else' de handleRuntime (68% -> 100%) ---
    @Test
    void registrarVenta_DeberiaRetornar500_CuandoOcurreErrorInesperado() throws Exception {
        // GIVEN: Un request válido...
        CrearVentaRequest request = new CrearVentaRequest();
        request.setClienteId(1L);
        request.setCantidadCuotas(1);
        CrearVentaRequest.ItemVenta item = new CrearVentaRequest.ItemVenta();
        item.setLibroId(1L);
        item.setCantidad(1);
        request.setItems(List.of(item));

        // ...PERO el servicio lanza un error GENÉRICO (sin el texto "no encontrado")
        when(ventaService.registrarVenta(any()))
                .thenThrow(new RuntimeException("Error fatal de base de datos"));

        // WHEN & THEN
        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError()) // Esperamos 500
                .andExpect(jsonPath("$.error").value("Error Interno"))
                .andExpect(jsonPath("$.message").value("Error fatal de base de datos"));
    }
}