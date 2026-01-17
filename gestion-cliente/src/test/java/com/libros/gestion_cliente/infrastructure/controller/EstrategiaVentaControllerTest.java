package com.libros.gestion_cliente.infrastructure.controller;

import com.libros.gestion_cliente.domain.model.Cliente;
import com.libros.gestion_cliente.domain.model.DetalleVenta;
import com.libros.gestion_cliente.domain.model.Libro;
import com.libros.gestion_cliente.domain.model.Venta;
import com.libros.gestion_cliente.domain.repository.ClienteRepository;
import com.libros.gestion_cliente.domain.repository.VentaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EstrategiaVentaController.class)
class EstrategiaVentaControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ClienteRepository clienteRepository;
    @MockitoBean private VentaRepository ventaRepository;

    @Test
    void listarPorLocalidad_DeberiaRetornarLista() throws Exception {
        when(clienteRepository.findByLocalidadContainingIgnoreCase("Santa Fe"))
                .thenReturn(List.of(new Cliente()));

        mockMvc.perform(get("/api/estrategia/localidad").param("zona", "Santa Fe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void buscarInteresados_DeberiaRetornarLista() throws Exception {
        when(clienteRepository.findByInteresesPersonalesContainingIgnoreCase("Cocina"))
                .thenReturn(List.of(new Cliente()));

        mockMvc.perform(get("/api/estrategia/interesados").param("tema", "Cocina"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void listarLibresDeDeuda_DeberiaRetornarLista() throws Exception {
        when(clienteRepository.findClientesLibresDeDeuda())
                .thenReturn(List.of(new Cliente()));

        mockMvc.perform(get("/api/estrategia/libres-deuda"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void verHistorial_DeberiaRetornarMapaDeCompras() throws Exception {
        // GIVEN
        Libro libro = Libro.builder().titulo("Cocina Facil").build();
        DetalleVenta detalle = DetalleVenta.builder().libro(libro).build();
        Venta venta = Venta.builder().fechaVenta(LocalDate.now()).detalles(List.of(detalle)).build();

        when(ventaRepository.findByClienteId(1L)).thenReturn(List.of(venta));

        // WHEN & THEN
        mockMvc.perform(get("/api/estrategia/historial/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].libro").value("Cocina Facil"));
    }
}