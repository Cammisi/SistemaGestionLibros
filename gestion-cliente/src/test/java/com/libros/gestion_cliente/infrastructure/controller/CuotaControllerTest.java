package com.libros.gestion_cliente.infrastructure.controller;

import com.libros.gestion_cliente.application.service.CuotaService;
import com.libros.gestion_cliente.domain.model.Cuota;
import com.libros.gestion_cliente.domain.model.EstadoCuota;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CuotaController.class)
class CuotaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CuotaService cuotaService;

    @MockitoBean
    private com.libros.gestion_cliente.infrastructure.report.ReciboPdfService reciboPdfService;

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

    @Test
    void descargarRecibo_DeberiaRetornarPDF() throws Exception {
        Long cuotaId = 1L;
        byte[] dummyPdf = new byte[]{1, 2, 3}; // Simulo un PDF

        doNothing().when(reciboPdfService).generarRecibo(any(Cuota.class));

        mockMvc.perform(get("/api/cuotas/{id}/recibo", cuotaId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"recibo_cuota_1.pdf\""));
    }
}