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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReciboPdfServiceTest {

    @Mock
    private CuotaRepository cuotaRepository;

    @InjectMocks
    private ReciboPdfService reciboPdfService;

    @Test
    void generarReciboCuota_DeberiaGenerarBytes_CuandoDatosEstanCompletos() {
        // GIVEN
        Cliente cliente = Cliente.builder().nombre("Juan").apellido("Perez").direccion("Calle Falsa 123").build();
        Libro libro = Libro.builder().titulo("Martin Fierro").build();
        DetalleVenta detalle = DetalleVenta.builder().libro(libro).build();

        Venta venta = Venta.builder()
                .id(1L)
                .cliente(cliente)
                .nroFactura("A-0001")
                .cantidadCuotas(3)
                .detalles(List.of(detalle))
                .build();

        Cuota cuota = Cuota.builder()
                .id(10L)
                .venta(venta)
                .numeroCuota(1)
                .montoCuota(new BigDecimal("1000.00"))
                .fechaPagoReal(LocalDate.now()) // Fecha existe
                .estado(EstadoCuota.PAGADA)
                .build();

        when(cuotaRepository.findById(10L)).thenReturn(Optional.of(cuota));
        // Simulamos que hay otra cuota pendiente para probar el cálculo de saldo
        Cuota cuota2 = Cuota.builder().estado(EstadoCuota.PENDIENTE).montoCuota(new BigDecimal("1000.00")).build();
        when(cuotaRepository.findByVentaId(1L)).thenReturn(List.of(cuota, cuota2));

        // WHEN
        byte[] pdf = reciboPdfService.generarReciboCuota(10L);

        // THEN
        assertThat(pdf).isNotEmpty();
        assertThat(pdf.length).isGreaterThan(100); // Debe tener contenido
    }

    @Test
    void generarReciboCuota_DeberiaFuncionar_CuandoFaltanDatosOpcionales() {
        // GIVEN: Caso extremo con nulos para probar la robustez (NullPointer checks)
        Cliente cliente = Cliente.builder().nombre("Juan").apellido("Perez").build(); // Sin dirección
        Venta venta = Venta.builder().id(1L).cliente(cliente).detalles(Collections.emptyList()).build(); // Sin factura, sin libros

        Cuota cuota = Cuota.builder()
                .id(10L)
                .venta(venta)
                .montoCuota(null) // Monto null
                .fechaPagoReal(null) // Fecha pago null
                .estado(EstadoCuota.PENDIENTE)
                .build();

        when(cuotaRepository.findById(10L)).thenReturn(Optional.of(cuota));
        when(cuotaRepository.findByVentaId(1L)).thenReturn(List.of(cuota));

        // WHEN
        byte[] pdf = reciboPdfService.generarReciboCuota(10L);

        // THEN
        assertThat(pdf).isNotEmpty(); // No debe explotar
    }

    @Test
    void generarReciboCuota_DeberiaLanzarExcepcion_SiCuotaNoExiste() {
        when(cuotaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> reciboPdfService.generarReciboCuota(99L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void generarReciboCuota_DeberiaCapturarExcepcion_Y_LanzarRuntimeException() {
        // GIVEN: Preparamos el escenario para el sabotaje

        // 1. Mockeamos Venta y Cuota (en lugar de usar .builder())
        // Esto nos permite obligar a que sus métodos lancen excepciones
        Venta ventaMock = org.mockito.Mockito.mock(Venta.class);
        Cuota cuotaMock = org.mockito.Mockito.mock(Cuota.class);

        // 2. Configuramos lo básico para que pase las líneas ANTES del try
        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuotaMock));
        when(cuotaMock.getVenta()).thenReturn(ventaMock);
        when(ventaMock.getId()).thenReturn(100L); // Necesario para findByVentaId
        when(cuotaRepository.findByVentaId(100L)).thenReturn(List.of(cuotaMock));

        // 3. EL SABOTAJE:
        // Cuando el código entre al try y llame a venta.getCliente(), ¡PUM! Excepción.
        // Esto ocurre justo en la línea: "Recibí de: " + venta.getCliente().getNombre()...
        when(ventaMock.getCliente()).thenThrow(new RuntimeException("Error inesperado al leer datos"));

        // WHEN & THEN
        // Verificamos que el servicio atrape esa excepción interna y lance la nuestra
        assertThatThrownBy(() -> reciboPdfService.generarReciboCuota(1L))
                .isInstanceOf(RuntimeException.class) // La excepción envoltorio
                .hasMessage("Error al generar PDF")   // Tu mensaje personalizado
                .hasCauseInstanceOf(RuntimeException.class); // La causa original
    }
}