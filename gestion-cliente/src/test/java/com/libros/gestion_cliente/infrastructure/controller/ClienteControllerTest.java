package com.libros.gestion_cliente.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.libros.gestion_cliente.application.dto.CrearClienteRequest;
import com.libros.gestion_cliente.application.service.ClienteService;
import com.libros.gestion_cliente.domain.model.Cliente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClienteService clienteService;

    // --- TEST 1: CREAR CLIENTE EXITOSO (201) ---
    @Test
    void crearCliente_DeberiaRetornar201_CuandoEsValido() throws Exception {
        CrearClienteRequest request = new CrearClienteRequest();
        request.setNombre("Ana");
        request.setApellido("Gomez");
        request.setDni("11223344");

        Cliente clienteMock = Cliente.builder().id(1L).nombre("Ana").dni("11223344").build();

        when(clienteService.crearCliente(any(CrearClienteRequest.class))).thenReturn(clienteMock);

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Ana"));
    }

    // --- TEST 2: VALIDACIÓN FALLIDA (400) ---
    @Test
    void crearCliente_DeberiaRetornar400_CuandoFaltanDatos() throws Exception {
        CrearClienteRequest requestInvalido = new CrearClienteRequest();
        // No seteamos nada (Nombre, Apellido, DNI son obligatorios)

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error de Validación"));
    }

    // --- TEST 3: DNI DUPLICADO (400) ---
    @Test
    void crearCliente_DeberiaRetornar400_CuandoDniYaExiste() throws Exception {
        CrearClienteRequest request = new CrearClienteRequest();
        request.setNombre("Pedro");
        request.setApellido("Picapiedra");
        request.setDni("11223344"); // DNI Repetido

        // Simulamos que el servicio lanza IllegalArgumentException (capturada por GlobalExceptionHandler)
        when(clienteService.crearCliente(any())).thenThrow(new IllegalArgumentException("DNI existente"));

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // IllegalArgumentException devuelve 400 en tu Handler
                .andExpect(jsonPath("$.message").value("DNI existente"));
    }

    // --- TEST 4: LISTAR CLIENTES (200) ---
    @Test
    void listarClientes_DeberiaRetornarPagina() throws Exception {
        // GIVEN
        Cliente cliente = Cliente.builder().id(1L).dni("123").nombre("Test").build();
        // Simulamos una página de respuesta
        when(clienteService.listarClientes(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(cliente)));

        // WHEN & THEN
        mockMvc.perform(get("/api/clientes")
                        .param("page", "0")
                        .param("size", "10")) // Opcional, Spring usa defaults
                .andExpect(status().isOk())
                // OJO: Al ser paginado, los datos están dentro de "content"
                .andExpect(jsonPath("$.content[0].dni").value("123"))
                .andExpect(jsonPath("$.content[0].nombre").value("Test"));
    }

    // --- TEST 5: BUSCAR POR DNI (200) ---
    @Test
    void buscarPorDni_DeberiaRetornarCliente_CuandoExiste() throws Exception {
        String dni = "123";
        Cliente cliente = Cliente.builder().dni(dni).nombre("Test").build();
        when(clienteService.buscarPorDni(dni)).thenReturn(cliente);

        mockMvc.perform(get("/api/clientes/{dni}", dni))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value(dni));
    }

    // --- TEST 6: BUSCAR POR DNI NO ENCONTRADO (404) ---
    @Test
    void buscarPorDni_DeberiaRetornar404_CuandoNoExiste() throws Exception {
        String dni = "999";
        when(clienteService.buscarPorDni(dni))
                .thenThrow(new RuntimeException("Cliente no encontrado con DNI: " + dni));

        mockMvc.perform(get("/api/clientes/{dni}", dni))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recurso no encontrado"));
    }

    // --- TEST 7: EL TEST QUE FALTA PARA EL 100% DE BRANCHES ---
    // Este test fuerza que ex.getMessage() sea NULL
    @Test
    void buscarPorDni_DeberiaRetornar500_CuandoExcepcionNoTieneMensaje() throws Exception {
        String dni = "000";
        // Simulamos una RuntimeException sin mensaje (null)
        // Esto hará que el if (ex.getMessage() != null) sea FALSE
        when(clienteService.buscarPorDni(dni)).thenThrow(new RuntimeException((String) null));

        mockMvc.perform(get("/api/clientes/{dni}", dni))
                .andExpect(status().isInternalServerError()) // Esperamos 500
                .andExpect(jsonPath("$.error").value("Error Interno"));
        // El message probablemente sea null en la respuesta JSON también, o vacío
    }
}