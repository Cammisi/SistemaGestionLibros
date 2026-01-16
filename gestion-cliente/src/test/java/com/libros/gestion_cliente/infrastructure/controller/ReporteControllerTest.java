package com.libros.gestion_cliente.infrastructure.controller;

import com.libros.gestion_cliente.application.service.ReporteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReporteController.class)
class ReporteControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ReporteService reporteService;

    @Test
    void descargarReporteMensual_DeberiaRetornarArchivoCsv() throws Exception {
        // GIVEN
        String csvContenido = "ID,Fecha\n1,2026-01-01";
        when(reporteService.generarReporteVentasMensuales(anyInt(), anyInt())).thenReturn(csvContenido);

        // WHEN & THEN
        mockMvc.perform(get("/api/reportes/ventas/mensual")
                        .param("mes", "1")
                        .param("anio", "2026"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"ventas_1_2026.csv\""))
                .andExpect(content().bytes(csvContenido.getBytes()));
    }
}