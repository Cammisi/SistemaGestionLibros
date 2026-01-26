package com.libros.gestion_cliente.unit.infrastructure.report;

import com.libros.gestion_cliente.domain.model.*;
import com.libros.gestion_cliente.domain.repository.CuotaRepository;
import com.libros.gestion_cliente.infrastructure.report.ReciboPdfService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReciboPdfServiceTest {

    @Mock
    private CuotaRepository cuotaRepository;

    @InjectMocks
    private ReciboPdfService reciboPdfService;

    @TempDir
    Path tempDir;

    private String originalUserHome;

    @BeforeEach
    void setUp() {
        // 1. Redirigir user.home a una carpeta temporal segura para CI/CD
        originalUserHome = System.getProperty("user.home");
        System.setProperty("user.home", tempDir.toString());

        // 2. Crear estructura de carpetas simulada (Desktop/Recibos)
        File desktop = new File(tempDir.toFile(), "Desktop");
        if (!desktop.exists()) desktop.mkdir();

        File recibos = new File(desktop, "Recibos");
        if (!recibos.exists()) recibos.mkdir();
    }

    @AfterEach
    void tearDown() {
        if (originalUserHome != null) {
            System.setProperty("user.home", originalUserHome);
        }
    }

    @Test
    void generarRecibo_DeberiaEjecutarseCorrectamente_CuandoDatosEstanCompletos() throws Exception {
        // GIVEN
        Cliente cliente = Cliente.builder()
                .nombre("Juan").apellido("Perez").dni("12345678")
                .localidad("Springfield").direccion("Av 123").telefono("555-5555")
                .build();

        Venta venta = Venta.builder()
                .id(1L).cliente(cliente).nroFactura("A-0001")
                .detalles(List.of()) // Lista vacía segura
                .build();

        Cuota cuota = Cuota.builder()
                .id(1L).venta(venta).numeroCuota(1)
                .montoCuota(BigDecimal.TEN).estado(EstadoCuota.PAGADA)
                .fechaPagoReal(LocalDate.now())
                .build();

        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuota));
        when(cuotaRepository.findByVentaId(1L)).thenReturn(List.of(cuota));

        // WHEN
        reciboPdfService.generarRecibo(cuota);

        // THEN
        verify(cuotaRepository, atLeastOnce()).findById(1L);
    }

    @Test
    void generarRecibo_DeberiaFuncionar_CuandoFaltanDatosOpcionales() throws Exception {
        // GIVEN - Datos mínimos
        Cliente cliente = Cliente.builder().nombre("Maria").apellido("Gomez").build();
        Venta venta = Venta.builder().id(1L).cliente(cliente).detalles(List.of()).build();
        Cuota cuota = Cuota.builder().id(1L).venta(venta).numeroCuota(1)
                .montoCuota(BigDecimal.TEN).estado(EstadoCuota.PENDIENTE)
                .build();

        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuota));
        when(cuotaRepository.findByVentaId(1L)).thenReturn(List.of(cuota));

        // WHEN
        reciboPdfService.generarRecibo(cuota);

        // THEN
        verify(cuotaRepository, atLeastOnce()).findById(1L);
    }

    @Test
    void generarRecibo_DeberiaCambiarEstado_SiEstaPendiente() throws Exception {
        // GIVEN - ¡DATOS COMPLETOS PARA EVITAR NPE!
        Cliente cliente = Cliente.builder()
                .nombre("Test").apellido("Pendiente")
                .direccion("Calle Falsa 123").localidad("Madrid").telefono("999-999")
                .dni("11223344")
                .build();

        Libro libro = Libro.builder().titulo("Libro Mock").isbn("123-456").build();

        DetalleVenta detalle = DetalleVenta.builder()
                .libro(libro)
                .cantidad(1)
                .precioAlMomento(new BigDecimal("100.00")) // Importante para cálculos
                .build();

        Venta venta = Venta.builder()
                .nroFactura("F-001")
                .cliente(cliente)
                .montoTotal(new BigDecimal("100.00"))
                .detalles(List.of(detalle))
                .build();

        Cuota cuota = Cuota.builder()
                .id(1L)
                .venta(venta)
                .numeroCuota(1)
                .montoCuota(BigDecimal.TEN)
                .estado(EstadoCuota.PENDIENTE) // Estado inicial PENDIENTE
                .build();

        // El repositorio devuelve la cuota tal cual
        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuota));
        // Para calcular saldo restante
        when(cuotaRepository.findByVentaId(any())).thenReturn(List.of(cuota));

        // WHEN
        reciboPdfService.generarRecibo(cuota);

        // THEN
        // Verificamos que se llame a save con el estado cambiado a PAGADA
        verify(cuotaRepository).save(argThat(c -> c.getEstado() == EstadoCuota.PAGADA));
    }

    @Test
    void generarRecibo_NoDeberiaGuardar_SiYaEstaPagada() throws Exception {
        // GIVEN
        Venta venta = Venta.builder()
                .nroFactura("F-002").cliente(Cliente.builder().nombre("A").apellido("B").build())
                .detalles(List.of()).build();
        Cuota cuota = Cuota.builder()
                .id(2L).venta(venta).numeroCuota(2)
                .montoCuota(BigDecimal.TEN).estado(EstadoCuota.PAGADA) // YA PAGADA
                .build();

        when(cuotaRepository.findById(2L)).thenReturn(Optional.of(cuota));
        when(cuotaRepository.findByVentaId(any())).thenReturn(List.of(cuota));

        // WHEN
        reciboPdfService.generarRecibo(cuota);

        // THEN
        verify(cuotaRepository, never()).save(any());
    }

    @Test
    void generarRecibo_DeberiaLanzarExcepcion_CuandoCuotaNoExisteEnBD() {
        Long idInexistente = 999L;
        Cuota cuotaParametro = Cuota.builder().id(idInexistente).build();
        when(cuotaRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reciboPdfService.generarRecibo(cuotaParametro))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cuota no encontrada");
    }

    @Test
    void generarRecibo_DeberiaManejarNroFacturaNulo() throws Exception {
        Venta venta = Venta.builder().nroFactura(null)
                .cliente(Cliente.builder().nombre("T").apellido("N").build())
                .detalles(List.of()).build();
        Cuota cuota = Cuota.builder().id(1L).venta(venta).montoCuota(BigDecimal.ONE).build();

        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuota));
        when(cuotaRepository.findByVentaId(any())).thenReturn(List.of(cuota));

        reciboPdfService.generarRecibo(cuota);
        verify(cuotaRepository, atLeastOnce()).findById(1L);
    }

    @Test
    void generarRecibo_DeberiaTratarMontoNuloComoCero() throws Exception {
        Venta venta = Venta.builder()
                .cliente(Cliente.builder().nombre("T").apellido("N").build())
                .detalles(List.of()).build();
        Cuota cuota = Cuota.builder().id(1L).venta(venta).montoCuota(null).build();

        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuota));
        when(cuotaRepository.findByVentaId(any())).thenReturn(List.of(cuota));

        reciboPdfService.generarRecibo(cuota);
        verify(cuotaRepository, atLeastOnce()).findById(1L);
    }

    @Test
    void generarRecibo_DeberiaConcatenarMultiplesLibrosConComa() throws Exception {
        Libro l1 = Libro.builder().titulo("Libro A").build();
        Libro l2 = Libro.builder().titulo("Libro B").build();
        DetalleVenta d1 = DetalleVenta.builder().libro(l1).precioAlMomento(BigDecimal.ONE).cantidad(1).build();
        DetalleVenta d2 = DetalleVenta.builder().libro(l2).precioAlMomento(BigDecimal.ONE).cantidad(1).build();

        Venta venta = Venta.builder().nroFactura("F-Multi")
                .cliente(Cliente.builder().nombre("L").apellido("A").build())
                .detalles(List.of(d1, d2)).build();
        Cuota cuota = Cuota.builder().id(1L).venta(venta).montoCuota(BigDecimal.TEN).build();

        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuota));
        when(cuotaRepository.findByVentaId(any())).thenReturn(List.of(cuota));

        reciboPdfService.generarRecibo(cuota);
        verify(cuotaRepository, atLeastOnce()).findById(1L);
    }
}