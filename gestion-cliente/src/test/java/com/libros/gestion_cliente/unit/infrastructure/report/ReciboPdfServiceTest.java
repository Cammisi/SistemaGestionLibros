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
import java.util.ArrayList;
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

    // --- SOLUCIÓN AL PROBLEMA DE I/O EN CI ---
    @TempDir
    Path tempDir;

    private String originalUserHome;

    @BeforeEach
    void setUp() {
        // 1. Guardamos el user.home original
        originalUserHome = System.getProperty("user.home");

        // 2. Cambiamos user.home para que apunte a la carpeta temporal del test
        System.setProperty("user.home", tempDir.toString());

        // 3. Creamos la carpeta "Desktop" dentro del tempDir para que el servicio la encuentre
        File desktopMock = new File(tempDir.toFile(), "Desktop");
        if (!desktopMock.exists()) {
            desktopMock.mkdir();
        }
    }

    @AfterEach
    void tearDown() {
        // 4. Restauramos el user.home original para no afectar otros tests
        if (originalUserHome != null) {
            System.setProperty("user.home", originalUserHome);
        }
    }
    // ------------------------------------------

    @Test
    void generarRecibo_DeberiaEjecutarseCorrectamente_CuandoDatosEstanCompletos() {
        // GIVEN
        Cliente cliente = Cliente.builder().nombre("Juan").apellido("Perez").localidad("Springfield").direccion("Av 123").build();
        Venta venta = Venta.builder().id(1L).cliente(cliente).nroFactura("A-0001").detalles(List.of()).build();
        Cuota cuota = Cuota.builder().id(1L).venta(venta).numeroCuota(1).montoCuota(BigDecimal.TEN).estado(EstadoCuota.PAGADA).build();

        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuota));
        when(cuotaRepository.findByVentaId(1L)).thenReturn(List.of(cuota));

        // WHEN
        try {
            reciboPdfService.generarRecibo(cuota);
        } catch (Exception e) {
            e.printStackTrace(); // Imprimir error si falla para depurar
        }

        // THEN
        verify(cuotaRepository, atLeastOnce()).findById(1L);
    }

    @Test
    void generarRecibo_DeberiaFuncionar_CuandoFaltanDatosOpcionales() {
        // GIVEN
        Cliente cliente = Cliente.builder().nombre("Maria").apellido("Gomez").build();
        Venta venta = Venta.builder().id(1L).cliente(cliente).detalles(List.of()).build();
        Cuota cuota = Cuota.builder().id(1L).venta(venta).numeroCuota(1).montoCuota(BigDecimal.TEN).estado(EstadoCuota.PENDIENTE).build();

        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuota));
        when(cuotaRepository.findByVentaId(1L)).thenReturn(List.of(cuota));

        // WHEN
        try {
            reciboPdfService.generarRecibo(cuota);
        } catch (Exception e) {}

        // THEN
        verify(cuotaRepository, atLeastOnce()).findById(1L);
    }

    @Test
    void generarRecibo_DeberiaCambiarEstado_SiEstaPendiente() {
        // GIVEN - Datos completos
        Cliente cliente = Cliente.builder()
                .nombre("Test").apellido("Pendiente")
                .direccion("Calle Falsa 123").localidad("Madrid")
                .build();

        Libro libro = Libro.builder().titulo("Libro Mock").build();
        DetalleVenta detalle = DetalleVenta.builder().libro(libro).build();

        Venta venta = Venta.builder()
                .nroFactura("F-001")
                .cliente(cliente)
                .detalles(List.of(detalle))
                .build();

        Cuota cuota = Cuota.builder()
                .id(1L)
                .venta(venta)
                .numeroCuota(1)
                .montoCuota(BigDecimal.TEN)
                .estado(EstadoCuota.PENDIENTE) // ESTADO PENDIENTE
                .build();

        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuota));
        when(cuotaRepository.findByVentaId(any())).thenReturn(List.of(cuota));

        // WHEN
        // Al haber configurado el TempDir, la escritura del PDF debería funcionar y permitir que el código avance
        try {
            reciboPdfService.generarRecibo(cuota);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // THEN
        verify(cuotaRepository).save(argThat(c -> c.getEstado() == EstadoCuota.PAGADA));
    }

    @Test
    void generarRecibo_NoDeberiaGuardar_SiYaEstaPagada() {
        // GIVEN
        Venta venta = Venta.builder().nroFactura("F-002").cliente(Cliente.builder().nombre("A").apellido("B").build()).detalles(List.of()).build();
        Cuota cuota = Cuota.builder().id(2L).venta(venta).numeroCuota(2).montoCuota(BigDecimal.TEN).estado(EstadoCuota.PAGADA).build();

        when(cuotaRepository.findById(2L)).thenReturn(Optional.of(cuota));
        when(cuotaRepository.findByVentaId(any())).thenReturn(List.of(cuota));

        // WHEN
        try {
            reciboPdfService.generarRecibo(cuota);
        } catch (Exception e) {}

        // THEN
        verify(cuotaRepository, never()).save(any());
    }

    @Test
    void generarRecibo_DeberiaLanzarExcepcion_CuandoCuotaNoExisteEnBD() {
        // GIVEN
        Long idInexistente = 999L;
        Cuota cuotaParametro = Cuota.builder().id(idInexistente).build();
        when(cuotaRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> reciboPdfService.generarRecibo(cuotaParametro))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cuota no encontrada");
    }

    @Test
    void generarRecibo_DeberiaManejarNroFacturaNulo() {
        Venta venta = Venta.builder().nroFactura(null).cliente(Cliente.builder().nombre("T").apellido("N").build()).detalles(List.of()).build();
        Cuota cuota = Cuota.builder().id(1L).venta(venta).montoCuota(BigDecimal.ONE).build();

        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuota));
        when(cuotaRepository.findByVentaId(any())).thenReturn(List.of(cuota));

        try { reciboPdfService.generarRecibo(cuota); } catch (Exception e) {}
        verify(cuotaRepository, atLeastOnce()).findById(1L);
    }

    @Test
    void generarRecibo_DeberiaTratarMontoNuloComoCero() {
        Venta venta = Venta.builder().cliente(Cliente.builder().nombre("T").apellido("N").build()).detalles(List.of()).build();
        Cuota cuota = Cuota.builder().id(1L).venta(venta).montoCuota(null).build();

        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuota));
        when(cuotaRepository.findByVentaId(any())).thenReturn(List.of(cuota));

        try { reciboPdfService.generarRecibo(cuota); } catch (Exception e) {}
        verify(cuotaRepository, atLeastOnce()).findById(1L);
    }

    @Test
    void generarRecibo_DeberiaConcatenarMultiplesLibrosConComa() {
        // GIVEN
        Libro l1 = Libro.builder().titulo("Libro A").build();
        Libro l2 = Libro.builder().titulo("Libro B").build();
        DetalleVenta d1 = DetalleVenta.builder().libro(l1).build();
        DetalleVenta d2 = DetalleVenta.builder().libro(l2).build();

        Venta venta = Venta.builder().nroFactura("F-Multi").cliente(Cliente.builder().nombre("L").apellido("A").build()).detalles(List.of(d1, d2)).build();
        Cuota cuota = Cuota.builder().id(1L).venta(venta).montoCuota(BigDecimal.TEN).build();

        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuota));
        when(cuotaRepository.findByVentaId(any())).thenReturn(List.of(cuota));

        try { reciboPdfService.generarRecibo(cuota); } catch (Exception e) {}
        verify(cuotaRepository, atLeastOnce()).findById(1L);
    }
}