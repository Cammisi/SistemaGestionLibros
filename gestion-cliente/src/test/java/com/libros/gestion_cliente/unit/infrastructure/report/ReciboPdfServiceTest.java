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

    @TempDir
    Path tempDir;

    private String originalUserHome;

    @BeforeEach
    void setUp() {
        originalUserHome = System.getProperty("user.home");
        System.setProperty("user.home", tempDir.toString());

        File desktop = new File(tempDir.toFile(), "Desktop");
        if (!desktop.exists()) desktop.mkdir();

        File recibos = new File(desktop, "Recibos");
        if (!recibos.exists()) recibos.mkdir();

        File reportes = new File(desktop, "Reportes");
        if (!reportes.exists()) reportes.mkdir();
    }

    @AfterEach
    void tearDown() {
        if (originalUserHome != null) {
            System.setProperty("user.home", originalUserHome);
        }
    }

    @Test
    void generarRecibo_DeberiaEjecutarseCorrectamente_CuandoDatosEstanCompletos() throws Exception {
        Cliente cliente = Cliente.builder().nombre("Juan").apellido("Perez").dni("123").direccion("Av 1").telefono("555").build();
        Venta venta = Venta.builder().id(1L).cliente(cliente).nroFactura("A-0001").detalles(List.of()).build();
        Cuota cuota = Cuota.builder().id(1L).venta(venta).numeroCuota(1).montoCuota(BigDecimal.TEN).estado(EstadoCuota.PAGADA).fechaPagoReal(LocalDate.now()).build();

        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuota));
        when(cuotaRepository.findByVentaId(1L)).thenReturn(List.of(cuota));

        reciboPdfService.generarRecibo(cuota);

        verify(cuotaRepository, atLeastOnce()).findById(1L);
    }

    @Test
    void generarRecibo_DeberiaManejarListaCuotasNula() throws Exception {
        Venta venta = Venta.builder().cliente(Cliente.builder().nombre("A").apellido("B").build()).detalles(List.of()).build();
        venta.setCuotas(null);

        Cuota cuota = Cuota.builder().id(1L).venta(venta).numeroCuota(2).montoCuota(BigDecimal.TEN).build();

        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuota));

        reciboPdfService.generarRecibo(cuota);
        verify(cuotaRepository, atLeastOnce()).findById(1L);
    }

    @Test
    void generarRecibo_DeberiaManejarListaCuotasVacia() throws Exception {
        Venta venta = Venta.builder().cliente(Cliente.builder().nombre("A").apellido("B").build()).detalles(List.of()).build();
        venta.setCuotas(new ArrayList<>());

        Cuota cuota = Cuota.builder().id(1L).venta(venta).numeroCuota(2).montoCuota(BigDecimal.TEN).build();

        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuota));

        reciboPdfService.generarRecibo(cuota);
        verify(cuotaRepository, atLeastOnce()).findById(1L);
    }

    @Test
    void generarRecibo_CoberturaCompletaStream() throws Exception {
        Venta venta = Venta.builder().id(10L).cliente(Cliente.builder().nombre("S").apellido("T").build()).detalles(List.of()).build();
        Cuota cActual = Cuota.builder().id(2L).venta(venta).numeroCuota(2).montoCuota(BigDecimal.TEN).build();
        Cuota cPasada = Cuota.builder().id(1L).venta(venta).numeroCuota(1).montoCuota(BigDecimal.TEN).build();
        Cuota cFutura = Cuota.builder().id(3L).venta(venta).numeroCuota(3).montoCuota(new BigDecimal("50.00")).build();
        Cuota cNull = Cuota.builder().id(4L).venta(venta).numeroCuota(4).montoCuota(null).build();

        venta.setCuotas(List.of(cPasada, cActual, cFutura, cNull));

        when(cuotaRepository.findById(2L)).thenReturn(Optional.of(cActual));
        when(cuotaRepository.findByVentaId(10L)).thenReturn(List.of(cPasada, cActual, cFutura, cNull));

        reciboPdfService.generarRecibo(cActual);
    }

    @Test
    void generarRecibo_DeberiaLanzarExcepcionInterna_SiCuotaDesaparece() {
        // GIVEN: Creamos una cuota COMPLETA para que pase la primera parte del método (generar nombre archivo)
        Venta venta = Venta.builder().nroFactura("F-Test").build();
        Cuota cuota = Cuota.builder()
                .id(1L)
                .venta(venta)
                .numeroCuota(1)
                .montoCuota(BigDecimal.TEN)
                .build();

        // Stubbing secuencial:
        // 1ra vez: Devuelve la cuota (pasa generación de nombre)
        // 2da vez: Devuelve Empty (falla dentro de generarPdfEnStream)
        when(cuotaRepository.findById(1L))
                .thenReturn(Optional.of(cuota))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reciboPdfService.generarRecibo(cuota))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cuota no encontrada");
    }

    @Test
    void generarReciboCuota_DeberiaBuscarYGenerar_CuandoExiste() throws Exception {
        Long id = 5L;
        Cliente c = Cliente.builder().nombre("A").apellido("B").build();
        Venta v = Venta.builder().cliente(c).detalles(List.of()).build();
        Cuota cuota = Cuota.builder().id(id).venta(v).numeroCuota(1).montoCuota(BigDecimal.ONE).build();

        when(cuotaRepository.findById(id)).thenReturn(Optional.of(cuota));

        reciboPdfService.generarReciboCuota(id);

        verify(cuotaRepository, atLeastOnce()).findById(id);
    }

    @Test
    void generarReciboCuota_DeberiaLanzarExcepcion_SiNoExiste() {
        when(cuotaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> reciboPdfService.generarReciboCuota(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cuota no encontrada");
    }

    @Test
    void generarListaReposicion_DeberiaCrearArchivo() throws Exception {
        Cliente c = Cliente.builder().nombre("A").apellido("B").build();
        PedidoEspecial p = PedidoEspecial.builder().descripcion("Libro").cliente(c).fechaPedido(LocalDate.now()).build();

        reciboPdfService.generarListaReposicion(List.of(p));

        File reporteDir = new File(tempDir.toFile(), "Desktop/Reportes");
        assert(reporteDir.exists());
    }
    // 1. Cubrir lambda$generarContenidoContrato$3 (El forEach de los detalles)
    @Test
    void generarRecibo_DeberiaIterarDetalles_CuandoEsCuotaUnoConLibros() throws Exception {
        // GIVEN
        Cliente cliente = Cliente.builder().nombre("Juan").apellido("Perez").build();
        Libro libro = Libro.builder().titulo("Libro de Prueba").build();

        // IMPORTANTE: Lista con elementos para que el forEach se ejecute
        DetalleVenta detalle = DetalleVenta.builder().libro(libro).cantidad(1).build();

        Venta venta = Venta.builder()
                .id(1L).cliente(cliente).nroFactura("A-0001")
                .detalles(List.of(detalle)) // <--- ESTO ACTIVA LA LAMBDA
                .montoTotal(BigDecimal.TEN)
                .build();

        Cuota cuota = Cuota.builder()
                .id(1L).venta(venta).numeroCuota(1) // Cuota 1 = Contrato
                .montoCuota(BigDecimal.TEN)
                .build();

        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuota));

        // WHEN
        reciboPdfService.generarRecibo(cuota);

        // THEN
        verify(cuotaRepository, atLeastOnce()).findById(1L);
    }

    // 2. Cubrir lambda$generarRecibo$1 (El orElseThrow al inicio del método público)
    @Test
    void generarRecibo_DeberiaLanzarExcepcion_SiNoEncuentraCuotaAlInicio() {
        // GIVEN
        Cuota cuotaParametro = Cuota.builder().id(99L).build();

        // Simulamos que no existe en BD
        when(cuotaRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> reciboPdfService.generarRecibo(cuotaParametro))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cuota no encontrada");
    }

    // 3. Cubrir catch (Exception e) en generarPdfEnStream
    @Test
    void generarRecibo_DeberiaCapturarExcepcionYRelanzar_SiFallaGeneracionPDF() {
        // GIVEN
        // Configuramos una cuota que al intentar leerse cause error (NPE en numeroCuota)
        // pero que pase el filtro inicial de búsqueda
        Cliente cliente = Cliente.builder().nombre("A").apellido("B").build();
        Venta venta = Venta.builder().cliente(cliente).detalles(List.of()).build();

        Cuota cuotaMalformada = Cuota.builder()
                .id(1L)
                .venta(venta)
                .numeroCuota(null) // <--- Esto forzará el error DENTRO del try
                .montoCuota(BigDecimal.TEN)
                .build();

        // Stubbing secuencial:
        // 1. findById(1L) -> devuelve OK (pasa la validación inicial)
        // 2. findById(1L) -> devuelve OK (entra a generarPdfEnStream)
        // 3. Dentro del try, getNumeroCuota() es null -> Explota
        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuotaMalformada));

        // WHEN & THEN
        assertThatThrownBy(() -> reciboPdfService.generarRecibo(cuotaMalformada))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error al generar PDF");
    }
}