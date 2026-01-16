package com.libros.gestion_cliente.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.libros.gestion_cliente.application.dto.CrearPedidoRequest;
import com.libros.gestion_cliente.application.service.PedidoEspecialService;
import com.libros.gestion_cliente.domain.model.EstadoPedido;
import com.libros.gestion_cliente.domain.model.PedidoEspecial;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoEspecialController.class)
class PedidoEspecialControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private PedidoEspecialService pedidoService;

    @Test
    void crearPedido_DeberiaRetornar201() throws Exception {
        CrearPedidoRequest request = new CrearPedidoRequest();
        request.setClienteId(1L);
        request.setDescripcion("Libro Raro");

        PedidoEspecial pedido = PedidoEspecial.builder().id(1L).descripcion("Libro Raro").estado(EstadoPedido.PENDIENTE).build();
        when(pedidoService.crearPedido(any())).thenReturn(pedido);

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void actualizarEstado_DeberiaRetornar200() throws Exception {
        PedidoEspecial pedido = PedidoEspecial.builder().id(1L).estado(EstadoPedido.DISPONIBLE).build();
        when(pedidoService.cambiarEstado(eq(1L), eq(EstadoPedido.DISPONIBLE))).thenReturn(pedido);

        mockMvc.perform(patch("/api/pedidos/1/estado")
                        .param("estado", "DISPONIBLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DISPONIBLE"));
    }

    @Test
    void listarPendientes_DeberiaRetornarLista() throws Exception {
        when(pedidoService.listarPendientes()).thenReturn(List.of(new PedidoEspecial()));

        mockMvc.perform(get("/api/pedidos/pendientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}