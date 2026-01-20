package com.libros.gestion_cliente.unit.infrastructure.report;

import com.libros.gestion_cliente.domain.model.*;
import com.libros.gestion_cliente.domain.repository.CuotaRepository;
import com.libros.gestion_cliente.infrastructure.report.ReciboPdfService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReciboPdfServiceTest {

    @Mock
    private CuotaRepository cuotaRepository;

    @InjectMocks
    private ReciboPdfService reciboPdfService;

    @Test
    void generarReciboCuota_DeberiaGenerarBytes_CuandoDatosEstanCompletos() {
        // GIVEN
        Cliente cliente = Cliente.builder()
                .nombre("Juan")
                .apellido("Perez")
                .direccion("Av. Siempre Viva 123")
                .localidad("Springfield")
                .build();

        Libro libro = Libro.builder().titulo("Libro Test").build();
        DetalleVenta detalle = DetalleVenta.builder().libro(libro).build();

        Venta venta = Venta.builder()
                .id(1L)
                .cliente(cliente)
                .detalles(List.of(detalle))
                .nroFactura("A-0001")
                .cantidadCuotas(2) // Total cuotas
                .build();

        // Cuota actual (la que se imprime)
        Cuota cuota = Cuota.builder()
                .id(1L)
                .venta(venta)
                .numeroCuota(1) // IMPORTANTE: Asignar número
                .montoCuota(new BigDecimal("100.00"))
                .fechaPagoReal(LocalDate.now())
                .estado(EstadoCuota.PAGADA)
                .build();

        // Otra cuota (para el cálculo de saldo)
        Cuota cuota2 = Cuota.builder()
                .id(2L)
                .venta(venta)
                .numeroCuota(2) // IMPORTANTE: Asignar número
                .montoCuota(new BigDecimal("100.00"))
                .estado(EstadoCuota.PENDIENTE)
                .build();

        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuota));
        // Mockear la búsqueda de todas las cuotas para el cálculo del saldo
        when(cuotaRepository.findByVentaId(1L)).thenReturn(List.of(cuota, cuota2));

        // WHEN
        byte[] pdfBytes = reciboPdfService.generarReciboCuota(1L);

        // THEN
        assertThat(pdfBytes).isNotEmpty();
        assertThat(new String(pdfBytes)).startsWith("%PDF");
    }

    @Test
    void generarReciboCuota_DeberiaFuncionar_CuandoFaltanDatosOpcionales() {
        // GIVEN
        Cliente cliente = Cliente.builder()
                .nombre("Maria")
                .apellido("Gomez")
                // Sin dirección ni localidad
                .build();

        Venta venta = Venta.builder()
                .id(1L)
                .cliente(cliente)
                .detalles(List.of(DetalleVenta.builder().libro(Libro.builder().titulo("X").build()).build()))
                // Sin nro factura
                .cantidadCuotas(1)
                .build();

        Cuota cuota = Cuota.builder()
                .id(1L)
                .venta(venta)
                .numeroCuota(1) // IMPORTANTE
                .montoCuota(new BigDecimal("50.00"))
                // Sin fecha pago (null)
                .estado(EstadoCuota.PENDIENTE)
                .build();

        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuota));
        when(cuotaRepository.findByVentaId(1L)).thenReturn(List.of(cuota));

        // WHEN
        byte[] pdfBytes = reciboPdfService.generarReciboCuota(1L);

        // THEN
        assertThat(pdfBytes).isNotEmpty();
    }

    @Test
    void generarReciboCuota_DeberiaLanzarExcepcion_CuandoCuotaNoExiste() {
        when(cuotaRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reciboPdfService.generarReciboCuota(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cuota no encontrada");
    }

    @Test
    void generarReciboCuota_DeberiaCapturarExcepcion_Y_LanzarRuntimeException() {
        // Simulamos un error dentro de la generación (ej: falla al leer cliente)
        Cuota cuotaMock = Cuota.builder().id(1L).build(); // Cuota vacía hará fallar getVenta()
        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuotaMock));

        assertThatThrownBy(() -> reciboPdfService.generarReciboCuota(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error generando PDF para API"); // Mensaje actualizado
    }

    // --- Test para cubrir el cambio de estado a PAGADA ---
    @Test
    void generarRecibo_DeberiaCambiarEstado_SiEstaPendiente() {
        // Given
        Venta venta = Venta.builder().nroFactura("F-001").cliente(Cliente.builder().nombre("A").apellido("B").build()).build();
        Cuota cuota = Cuota.builder()
                .id(1L)
                .venta(venta)
                .numeroCuota(1)
                .montoCuota(BigDecimal.TEN)
                .estado(EstadoCuota.PENDIENTE) // <--- CASO 1: PENDIENTE
                .build();

        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuota));

        // When
        // Capturamos excepción de IO o Runtime si falla la creación del archivo real,
        // pero lo importante es que corra la lógica previa
        try {
            reciboPdfService.generarRecibo(cuota); // Llamamos al método público que guarda archivo
        } catch (Exception e) {
            // Ignoramos errores de FileSystem en entorno de test (GitHub Actions a veces no tiene Desktop)
        }

        // Then
        // Verificamos que se intentó guardar la cuota con el nuevo estado
        verify(cuotaRepository).save(argThat(c -> c.getEstado() == EstadoCuota.PAGADA));
    }

    // --- Test para cubrir el NO cambio de estado ---
    @Test
    void generarRecibo_NoDeberiaGuardar_SiYaEstaPagada() {
        // Given
        Venta venta = Venta.builder().nroFactura("F-002").cliente(Cliente.builder().nombre("A").apellido("B").build()).build();
        Cuota cuota = Cuota.builder()
                .id(2L)
                .venta(venta)
                .numeroCuota(2)
                .montoCuota(BigDecimal.TEN)
                .estado(EstadoCuota.PAGADA) // <--- CASO 2: YA PAGADA
                .build();

        when(cuotaRepository.findById(2L)).thenReturn(Optional.of(cuota));

        // When
        try {
            reciboPdfService.generarRecibo(cuota);
        } catch (Exception e) {}

        // Then
        // Verificamos que NUNCA se llame al save
        verify(cuotaRepository, never()).save(any());
    }
}