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

    @Test
    void registrarVenta_DeberiaCrearVentaYCuotas_CuandoTodoEsCorrecto() {
        // GIVEN
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

        // MOCKS: Usamos any() para ser más flexibles y evitar errores de strict stubbing
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(ventaRepository.existsByClienteIdAndEstado(eq(clienteId), any())).thenReturn(false);
        when(libroRepository.findById(libroId)).thenReturn(Optional.of(libro));
        when(detalleVentaRepository.haCompradoLibro(eq(clienteId), eq(libroId))).thenReturn(false);

        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        Venta resultado = ventaService.registrarVenta(request);

        // THEN
        assertThat(resultado.getCliente()).isEqualTo(cliente);
        assertThat(resultado.getMontoTotal()).isEqualByComparingTo(new BigDecimal("100.00"));

        verify(libroRepository).save(libro);
        assertThat(libro.getStock()).isEqualTo(9);

        verify(cuotaRepository, times(3)).save(any(Cuota.class));
    }

    @Test
    void registrarVenta_DeberiaFallar_SiClienteTieneDeuda() {
        // GIVEN
        Long clienteId = 1L;
        CrearVentaRequest request = new CrearVentaRequest();
        request.setClienteId(clienteId);

        // Cliente existe
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(Cliente.builder().id(clienteId).build()));

        // Tiene deuda (Stubbing relajado con any())
        when(ventaRepository.existsByClienteIdAndEstado(eq(clienteId), any())).thenReturn(true);

        // WHEN / THEN
        assertThatThrownBy(() -> ventaService.registrarVenta(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("venta en proceso");

        verify(ventaRepository, never()).save(any());
    }

    @Test
    void registrarVenta_DeberiaFallar_SiYaComproLibro() {
        // GIVEN
        Long clienteId = 1L;
        Long libroId = 10L;

        CrearVentaRequest.ItemVenta item = new CrearVentaRequest.ItemVenta();
        item.setLibroId(libroId);
        item.setCantidad(1);

        CrearVentaRequest request = new CrearVentaRequest();
        request.setClienteId(clienteId);
        request.setItems(List.of(item));

        // Cliente OK
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(Cliente.builder().id(clienteId).build()));
        // Sin deuda
        when(ventaRepository.existsByClienteIdAndEstado(any(), any())).thenReturn(false);
        // Libro existe
        when(libroRepository.findById(libroId)).thenReturn(Optional.of(Libro.builder().id(libroId).build()));

        // YA COMPRÓ EL LIBRO (El stubbing clave)
        when(detalleVentaRepository.haCompradoLibro(eq(clienteId), eq(libroId))).thenReturn(true);

        // WHEN / THEN
        assertThatThrownBy(() -> ventaService.registrarVenta(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya compró el libro");
    }

    @Test
    void registrarVenta_DeberiaFallar_SiStockInsuficiente() {
        // GIVEN
        Long libroId = 10L;
        CrearVentaRequest.ItemVenta item = new CrearVentaRequest.ItemVenta();
        item.setLibroId(libroId);
        item.setCantidad(5);

        CrearVentaRequest request = new CrearVentaRequest();
        request.setClienteId(1L);
        request.setItems(List.of(item));

        when(clienteRepository.findById(any())).thenReturn(Optional.of(Cliente.builder().id(1L).build()));
        when(ventaRepository.existsByClienteIdAndEstado(any(), any())).thenReturn(false);

        // Libro con stock 1 (pide 5)
        when(libroRepository.findById(libroId)).thenReturn(Optional.of(Libro.builder().id(libroId).stock(1).build()));

        // WHEN / THEN
        assertThatThrownBy(() -> ventaService.registrarVenta(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stock insuficiente");
    }
}