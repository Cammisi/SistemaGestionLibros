package com.libros.gestion_cliente.unit.application.service;

import com.libros.gestion_cliente.application.dto.CrearClienteRequest;
import com.libros.gestion_cliente.application.service.ClienteService;
import com.libros.gestion_cliente.domain.model.Cliente;
import com.libros.gestion_cliente.domain.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    // --- 1. Crear Cliente: Camino Feliz ---
    @Test
    void crearCliente_DeberiaGuardar_CuandoDniNoExiste() {
        // GIVEN
        CrearClienteRequest request = new CrearClienteRequest();
        request.setNombre("Juan");
        request.setApellido("Perez");
        request.setDni("12345678");

        // Simulamos que NO existe el DNI
        when(clienteRepository.existsByDni("12345678")).thenReturn(false);
        // Simulamos el guardado (devolvemos el mismo objeto que entra)
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        Cliente resultado = clienteService.crearCliente(request);

        // THEN
        assertThat(resultado.getDni()).isEqualTo("12345678");
        assertThat(resultado.getFechaAlta()).isNotNull(); // Verifica que se asignó fecha
        verify(clienteRepository).save(any(Cliente.class));
    }

    // --- 2. Crear Cliente: Error DNI Duplicado ---
    @Test
    void crearCliente_DeberiaLanzarExcepcion_CuandoDniYaExiste() {
        // GIVEN
        CrearClienteRequest request = new CrearClienteRequest();
        request.setDni("12345678");

        // Simulamos que SÍ existe el DNI (Esto fuerza a entrar en el 'if')
        when(clienteRepository.existsByDni("12345678")).thenReturn(true);

        // WHEN / THEN
        assertThatThrownBy(() -> clienteService.crearCliente(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un cliente con el DNI");

        // Verificamos que NUNCA se llamó a guardar
        verify(clienteRepository, never()).save(any());
    }

    // --- 3. Listar Clientes ---
    @Test
    void listarClientes_DeberiaRetornarPagina() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);
        List<Cliente> clientes = List.of(new Cliente());
        // El repositorio ahora espera findAll(Pageable)
        when(clienteRepository.findAll(pageable)).thenReturn(new PageImpl<>(clientes));

        // WHEN
        // Llamamos al servicio con el pageable
        Page<Cliente> resultado = clienteService.listarClientes(pageable);

        // THEN
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        verify(clienteRepository).findAll(pageable);
    }

    // --- 4. Buscar por DNI: Encontrado ---
    @Test
    void buscarPorDni_DeberiaRetornarCliente_CuandoExiste() {
        String dni = "111";
        Cliente cliente = Cliente.builder().dni(dni).build();

        when(clienteRepository.findByDni(dni)).thenReturn(Optional.of(cliente));

        Cliente resultado = clienteService.buscarPorDni(dni);

        assertThat(resultado.getDni()).isEqualTo(dni);
    }

    // --- 5. Buscar por DNI: No Encontrado (Excepción) ---
    @Test
    void buscarPorDni_DeberiaLanzarExcepcion_CuandoNoExiste() {
        String dni = "999";

        when(clienteRepository.findByDni(dni)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.buscarPorDni(dni))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cliente no encontrado");
    }

    @Test
    void crearCliente_DeberiaUsarFechaAltaManual_SiSeProporciona() {
        // GIVEN
        LocalDate fechaManual = LocalDate.of(2020, 1, 1);
        CrearClienteRequest request = new CrearClienteRequest();
        request.setNombre("Test");
        request.setFechaAlta(fechaManual); // CASO 1: Fecha manual

        when(clienteRepository.save(any(Cliente.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Cliente resultado = clienteService.crearCliente(request);

        // THEN
        assertThat(resultado.getFechaAlta()).isEqualTo(fechaManual);
    }

    @Test
    void crearCliente_DeberiaUsarFechaActual_SiNoSeProporciona() {
        // GIVEN
        CrearClienteRequest request = new CrearClienteRequest();
        request.setNombre("Test");
        request.setFechaAlta(null); // CASO 2: Fecha null (default)

        when(clienteRepository.save(any(Cliente.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Cliente resultado = clienteService.crearCliente(request);

        // THEN
        assertThat(resultado.getFechaAlta()).isNotNull();
        // Podríamos validar que sea hoy, pero con que no sea null basta para coverage
    }
}