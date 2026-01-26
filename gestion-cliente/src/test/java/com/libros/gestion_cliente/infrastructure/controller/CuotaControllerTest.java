package com.libros.gestion_cliente.infrastructure.controller;

import com.libros.gestion_cliente.application.service.CuotaService;
import com.libros.gestion_cliente.domain.model.Cuota;
import com.libros.gestion_cliente.infrastructure.report.ReciboPdfService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CuotaController.class)
class CuotaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CuotaService cuotaService;

    @MockitoBean
    private ReciboPdfService reciboPdfService; // Mockeamos el servicio PDF

    @Test
    void registrarPago_DeberiaRetornar200_CuandoPagoEsExitoso() throws Exception {
        // GIVEN
        Cuota cuotaSimulada = Cuota.builder().id(1L).build();
        // Simulamos que el servicio devuelve algo (aunque el controller ignore el retorno, debe funcionar)
        when(cuotaService.registrarPago(eq(1L))).thenReturn(cuotaSimulada);

        // WHEN & THEN
        mockMvc.perform(post("/api/cuotas/1/pagar"))
                .andExpect(status().isOk());
    }

    @Test
    void descargarRecibo_DeberiaRetornarMensajeExito() throws Exception {
        // GIVEN
        Long idCuota = 1L;

        // CORRECCIÓN IMPORTANTE:
        // Tu controlador llama a 'generarReciboCuota(Long)', NO a 'generarRecibo(Cuota)'.
        // Le decimos a Mockito: "Cuando llamen a generarReciboCuota con el 1, no hagas nada (void)".
        doNothing().when(reciboPdfService).generarReciboCuota(idCuota);

        // WHEN & THEN
        mockMvc.perform(get("/api/cuotas/1/recibo"))
                .andExpect(status().isOk())
                // Verificamos que el texto coincida con el return del controlador
                .andExpect(content().string("Recibo generado correctamente en el Escritorio."));
    }
}