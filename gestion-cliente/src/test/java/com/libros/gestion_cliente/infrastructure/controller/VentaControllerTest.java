package com.libros.gestion_cliente.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.libros.gestion_cliente.application.dto.CrearDetalleVentaRequest; // NUEVO DTO
import com.libros.gestion_cliente.application.dto.CrearVentaRequest;
import com.libros.gestion_cliente.application.service.VentaService;
import com.libros.gestion_cliente.domain.model.Cliente;
import com.libros.gestion_cliente.domain.model.Venta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean; // Importante: MockBean en vez de Mock
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VentaController.class)
class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean // Spring Boot Test usa @MockBean para reemplazar el bean en el contexto
    private VentaService ventaService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registrarVenta_DeberiaRetornar201_CuandoEsExitoso() throws Exception {
        // GIVEN
        CrearDetalleVentaRequest detalle = CrearDetalleVentaRequest.builder()
                .libroId(1L)
                .cantidad(2)
                .build();

        CrearVentaRequest request = CrearVentaRequest.builder()
                .clienteId(1L)
                .cantidadCuotas(1)
                .detalles(List.of(detalle)) // IMPORTANTE: Lista no nula
                .build();

        Venta ventaCreada = Venta.builder()
                .id(1L)
                .montoTotal(new BigDecimal("200.00"))
                .cliente(Cliente.builder().id(1L).build())
                .build();

        when(ventaService.registrarVenta(any(CrearVentaRequest.class))).thenReturn(ventaCreada);

        // WHEN & THEN
        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.montoTotal").value(200.00));
    }

    @Test
    void registrarVenta_DeberiaRetornar400_CuandoHayErrorDeNegocio() throws Exception {
        // GIVEN: Request válido pero servicio lanza error
        CrearDetalleVentaRequest detalle = CrearDetalleVentaRequest.builder().libroId(1L).cantidad(1).build();
        CrearVentaRequest request = CrearVentaRequest.builder()
                .clienteId(1L)
                .detalles(List.of(detalle))
                .cantidadCuotas(1)
                .build();

        when(ventaService.registrarVenta(any())).thenThrow(new IllegalStateException("El cliente tiene deuda"));

        // WHEN & THEN
        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error de Negocio"))
                .andExpect(jsonPath("$.message").value("El cliente tiene deuda"));
    }

    @Test
    void registrarVenta_DeberiaRetornar400_CuandoFaltanDatosObligatorios() throws Exception {
        // GIVEN: Request con lista vacía o nula (Validación falla)
        CrearVentaRequest requestInvalido = CrearVentaRequest.builder()
                .clienteId(1L)
                .detalles(null) // Esto dispara @NotNull -> 400 Bad Request (Validation)
                .build();

        // WHEN & THEN
        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error de Validación"));
    }

    @Test
    void registrarVenta_DeberiaCapturarJsonInvalido() throws Exception {
        // JSON roto (falta cerrar llave y comillas) para forzar HttpMessageNotReadableException
        String jsonRoto = "{ \"clienteId\": 1, \"cantidadCuotas\": ";

        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRoto))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Formato JSON Inválido"));
    }
}