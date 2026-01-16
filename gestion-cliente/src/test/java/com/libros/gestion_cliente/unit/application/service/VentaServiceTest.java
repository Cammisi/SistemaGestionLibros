package com.libros.gestion_cliente.unit.application.service;

import com.libros.gestion_cliente.application.dto.CrearVentaRequest;
import com.libros.gestion_cliente.application.service.VentaService;
import com.libros.gestion_cliente.domain.model.*;
import com.libros.gestion_cliente.domain.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock private VentaRepository ventaRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private LibroRepository libroRepository;
    @Mock private DetalleVentaRepository detalleVentaRepository;
    @Mock private CuotaRepository cuotaRepository;

    @InjectMocks
    private VentaService ventaService;

    // --- TEST 1: CAMINO FELIZ (Todo OK) ---
    @Test
    void registrarVenta_DeberiaCrearVentaYCuotas_CuandoTodoEsCorrecto() {
        Long clienteId = 1L;
        Long libroId = 10L;

        Cliente cliente = Cliente.builder().id(clienteId).nombre("Juan").build();
        Libro libro = Libro.builder()
                .id(libroId)
                .titulo("Java Senior")
                .precioBase(new BigDecimal("100.00"))
                .stock(10)
                .build();

        CrearVentaRequest.ItemVenta item = new CrearVentaRequest.ItemVenta();
        item.setLibroId(libroId);
        item.setCantidad(1);

        CrearVentaRequest request = new CrearVentaRequest();
        request.setClienteId(clienteId);
        request.setCantidadCuotas(3);
        request.setItems(List.of(item));

        // Mocks
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(ventaRepository.existsByClienteIdAndEstado(eq(clienteId), any())).thenReturn(false);
        when(libroRepository.findById(libroId)).thenReturn(Optional.of(libro));
        when(detalleVentaRepository.haCompradoLibro(eq(clienteId), eq(libroId))).thenReturn(false);
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecución
        Venta resultado = ventaService.registrarVenta(request);

        // Verificaciones
        assertThat(resultado.getCliente()).isEqualTo(cliente);
        assertThat(resultado.getMontoTotal()).isEqualByComparingTo(new BigDecimal("100.00"));
        verify(libroRepository).save(libro);
        verify(cuotaRepository, times(3)).save(any(Cuota.class));
    }

    // --- TEST 2: FALLO POR CLIENTE INEXISTENTE ---
    @Test
    void registrarVenta_DeberiaLanzarExcepcion_CuandoClienteNoExiste() {
        Long clienteIdInexistente = 99L;
        CrearVentaRequest request = new CrearVentaRequest();
        request.setClienteId(clienteIdInexistente);

        when(clienteRepository.findById(clienteIdInexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventaService.registrarVenta(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cliente no encontrado");

        verify(ventaRepository, never()).existsByClienteIdAndEstado(any(), any());
    }

    // --- TEST 3: FALLO POR LIBRO INEXISTENTE ---
    @Test
    void registrarVenta_DeberiaLanzarExcepcion_CuandoLibroNoExiste() {
        Long clienteId = 1L;
        Long libroIdInexistente = 99L;

        CrearVentaRequest.ItemVenta item = new CrearVentaRequest.ItemVenta();
        item.setLibroId(libroIdInexistente);
        item.setCantidad(1);

        CrearVentaRequest request = new CrearVentaRequest();
        request.setClienteId(clienteId);
        request.setItems(List.of(item));

        // CORRECCIÓN AQUÍ: Usamos builder().id() para que el cliente tenga ID y no sea null
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(Cliente.builder().id(clienteId).build()));
        when(ventaRepository.existsByClienteIdAndEstado(any(), any())).thenReturn(false);
        when(libroRepository.findById(libroIdInexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventaService.registrarVenta(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Libro no encontrado");
    }

    // --- TEST 4: FALLO POR DEUDA (EL QUE FALLABA) ---
    @Test
    void registrarVenta_DeberiaFallar_SiClienteTieneDeuda() {
        // GIVEN
        Long clienteId = 1L; // Asegúrate de usar 1L
        CrearVentaRequest request = new CrearVentaRequest();
        request.setClienteId(clienteId);

        // --- CORRECCIÓN AQUÍ ---
        // Antes probablemente tenías: new Cliente() o builder().build() sin ID.
        // Debemos ponerle el ID explícitamente: .id(clienteId)
        Cliente clienteSimulado = Cliente.builder()
                .id(clienteId) // <--- ¡ESTO ES LO QUE FALTA!
                .nombre("Test")
                .build();

        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(clienteSimulado));

        // Ahora Mockito estará feliz porque el ID (1L) coincidirá
        when(ventaRepository.existsByClienteIdAndEstado(eq(clienteId), any())).thenReturn(true);

        // WHEN / THEN
        assertThatThrownBy(() -> ventaService.registrarVenta(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("venta en proceso");

        verify(ventaRepository, never()).save(any());
    }

    // --- TEST 5: FALLO POR DUPLICADO ---
    @Test
    void registrarVenta_DeberiaFallar_SiYaComproLibro() {
        Long clienteId = 1L;
        Long libroId = 10L;

        CrearVentaRequest.ItemVenta item = new CrearVentaRequest.ItemVenta();
        item.setLibroId(libroId);
        item.setCantidad(1);
        CrearVentaRequest request = new CrearVentaRequest();
        request.setClienteId(clienteId);
        request.setItems(List.of(item));

        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(Cliente.builder().id(clienteId).build()));
        when(ventaRepository.existsByClienteIdAndEstado(any(), any())).thenReturn(false);
        when(libroRepository.findById(libroId)).thenReturn(Optional.of(Libro.builder().id(libroId).build()));
        when(detalleVentaRepository.haCompradoLibro(eq(clienteId), eq(libroId))).thenReturn(true);

        assertThatThrownBy(() -> ventaService.registrarVenta(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya compró el libro");
    }

    // --- TEST 6: FALLO POR STOCK ---
    @Test
    void registrarVenta_DeberiaFallar_SiStockInsuficiente() {
        Long libroId = 10L;
        CrearVentaRequest.ItemVenta item = new CrearVentaRequest.ItemVenta();
        item.setLibroId(libroId);
        item.setCantidad(5);
        CrearVentaRequest request = new CrearVentaRequest();
        request.setClienteId(1L);
        request.setItems(List.of(item));

        when(clienteRepository.findById(any())).thenReturn(Optional.of(Cliente.builder().id(1L).build()));
        when(ventaRepository.existsByClienteIdAndEstado(any(), any())).thenReturn(false);
        when(libroRepository.findById(libroId)).thenReturn(Optional.of(Libro.builder().id(libroId).stock(1).build()));

        assertThatThrownBy(() -> ventaService.registrarVenta(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stock insuficiente");
    }
}