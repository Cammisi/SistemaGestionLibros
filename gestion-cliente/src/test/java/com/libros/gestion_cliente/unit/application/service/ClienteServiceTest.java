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
    void listarClientes_DeberiaRetornarLista() {
        when(clienteRepository.findAll()).thenReturn(List.of(new Cliente(), new Cliente()));

        List<Cliente> lista = clienteService.listarClientes();

        assertThat(lista).hasSize(2);
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
}