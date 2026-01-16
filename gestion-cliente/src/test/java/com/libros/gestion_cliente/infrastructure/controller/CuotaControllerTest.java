package com.libros.gestion_cliente.infrastructure.controller;

import com.libros.gestion_cliente.application.service.CuotaService;
import com.libros.gestion_cliente.domain.model.Cuota;
import com.libros.gestion_cliente.domain.model.EstadoCuota;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CuotaController.class)
class CuotaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CuotaService cuotaService;

    // --- TEST 1: PAGO EXITOSO (200 OK) ---
    @Test
    void registrarPago_DeberiaRetornar200_CuandoPagoEsExitoso() throws Exception {
        Long cuotaId = 1L;
        // Mock del objeto que devolvería el servicio
        Cuota cuotaPagada = Cuota.builder().id(cuotaId).estado(EstadoCuota.PAGADA).build();

        when(cuotaService.registrarPago(cuotaId)).thenReturn(cuotaPagada);

        mockMvc.perform(post("/api/cuotas/{id}/pagar", cuotaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PAGADA"));
    }

    // --- TEST 2: CUOTA YA PAGADA (400 Bad Request) ---
    // Recordando que IllegalStateException lo mapeamos a 400 en GlobalExceptionHandler
    @Test
    void registrarPago_DeberiaRetornar400_CuandoYaEstabaPagada() throws Exception {
        Long cuotaId = 2L;
        when(cuotaService.registrarPago(cuotaId))
                .thenThrow(new IllegalStateException("La cuota ya está pagada"));

        mockMvc.perform(post("/api/cuotas/{id}/pagar", cuotaId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error de Negocio"))
                .andExpect(jsonPath("$.message").value("La cuota ya está pagada"));
    }

    // --- TEST 3: CUOTA NO EXISTE (404 Not Found) ---
    // Recordando que RuntimeException con "no encontrado" lo mapeamos a 404
    @Test
    void registrarPago_DeberiaRetornar404_CuandoNoExiste() throws Exception {
        Long cuotaId = 99L;
        when(cuotaService.registrarPago(cuotaId))
                .thenThrow(new RuntimeException("Cuota no encontrada: " + cuotaId));

        mockMvc.perform(post("/api/cuotas/{id}/pagar", cuotaId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recurso no encontrado"));
    }
}