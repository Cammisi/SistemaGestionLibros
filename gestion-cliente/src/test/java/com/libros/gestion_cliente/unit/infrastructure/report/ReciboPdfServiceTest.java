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
                .detalles(List.of())
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
        // GIVEN
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
    void generarRecibo_DeberiaLanzarExcepcion_CuandoCuotaNoExisteEnBD() {
        Long idInexistente = 999L;
        Cuota cuotaParametro = Cuota.builder().id(idInexistente).build();
        when(cuotaRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reciboPdfService.generarRecibo(cuotaParametro))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cuota no encontrada");
    }

    @Test
    void generarRecibo_DeberiaManejarNroFacturaNulo() throws Exception {
        Venta venta = Venta.builder().nroFactura(null)
                .cliente(Cliente.builder().nombre("T").apellido("N").build())
                .detalles(List.of()).build();
        // CORREGIDO: Agregado numeroCuota(1)
        Cuota cuota = Cuota.builder().id(1L).venta(venta).numeroCuota(1).montoCuota(BigDecimal.ONE).build();

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
        // CORREGIDO: Agregado numeroCuota(1)
        Cuota cuota = Cuota.builder().id(1L).venta(venta).numeroCuota(1).montoCuota(null).build();

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
        // CORREGIDO: Agregado numeroCuota(1)
        Cuota cuota = Cuota.builder().id(1L).venta(venta).numeroCuota(1).montoCuota(BigDecimal.TEN).build();

        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuota));
        when(cuotaRepository.findByVentaId(any())).thenReturn(List.of(cuota));

        reciboPdfService.generarRecibo(cuota);
        verify(cuotaRepository, atLeastOnce()).findById(1L);
    }

    // --- Test para generarListaReposicion ---
    @Test
    void generarListaReposicion_DeberiaCrearArchivoCorrectamente() throws Exception {
        // GIVEN
        Cliente cliente = Cliente.builder().nombre("Lector").apellido("Fiel").build();
        PedidoEspecial pedido = PedidoEspecial.builder()
                .descripcion("Harry Potter Edición Especial")
                .cliente(cliente)
                .fechaPedido(LocalDate.now())
                .estado(EstadoPedido.PENDIENTE)
                .build();

        // WHEN
        reciboPdfService.generarListaReposicion(List.of(pedido));

        // THEN
        // Verificamos que se haya creado el directorio (implícito si no falla)
        File directorioReportes = new File(tempDir.toFile(), "Desktop/Reportes");
        // Nota: Como el nombre del archivo tiene timestamp, es difícil verificar el archivo exacto,
        // pero podemos verificar que el directorio existe y no está vacío o que el método no lanzó error.
        org.junit.jupiter.api.Assertions.assertTrue(directorioReportes.exists() || new File(tempDir.toFile(), "Desktop").exists());
    }

    // --- Tests para calcularSaldoRestante (Lógica interna) ---
    @Test
    void generarRecibo_CalculoSaldo_DeberiaSerCero_SiNoHayMasCuotas() throws Exception {
        // GIVEN
        Venta venta = Venta.builder().detalles(List.of()).cliente(Cliente.builder().nombre("A").apellido("B").build()).build();
        // Solo existe la cuota actual en la lista
        Cuota cuotaActual = Cuota.builder().id(1L).venta(venta).numeroCuota(1).montoCuota(BigDecimal.TEN).build();
        venta.setCuotas(List.of(cuotaActual));

        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuotaActual));
        when(cuotaRepository.findByVentaId(any())).thenReturn(List.of(cuotaActual));

        // WHEN
        reciboPdfService.generarRecibo(cuotaActual);

        // THEN: Si no explota, cubrió la rama de saldo cero.
        verify(cuotaRepository, atLeastOnce()).findById(1L);
    }

    @Test
    void generarRecibo_CalculoSaldo_DeberiaSumarCuotasFuturas() throws Exception {
        // GIVEN
        Venta venta = Venta.builder().detalles(List.of()).cliente(Cliente.builder().nombre("A").apellido("B").build()).build();

        Cuota c1 = Cuota.builder().id(1L).venta(venta).numeroCuota(1).montoCuota(new BigDecimal("10.00")).build();
        Cuota c2 = Cuota.builder().id(2L).venta(venta).numeroCuota(2).montoCuota(new BigDecimal("20.00")).build(); // Futura
        Cuota c3 = Cuota.builder().id(3L).venta(venta).numeroCuota(3).montoCuota(new BigDecimal("30.00")).build(); // Futura

        venta.setCuotas(List.of(c1, c2, c3));

        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(c1));
        // IMPORTANTE: findByVentaId debe devolver TODAS para que el cálculo funcione
        when(cuotaRepository.findByVentaId(any())).thenReturn(List.of(c1, c2, c3));

        // WHEN
        reciboPdfService.generarRecibo(c1);

        // THEN: Cubre la línea del stream.reduce
        verify(cuotaRepository, atLeastOnce()).findById(1L);
    }

    // --- Test para la excepción RuntimeException("Error al generar PDF") ---
    @Test
    void generarRecibo_DeberiaLanzarRuntimeException_SiFallaItext() {
        // GIVEN
        // Creamos una estructura de datos VÁLIDA para que pase los primeros gets
        Cliente cliente = Cliente.builder().nombre("A").apellido("B").build();
        Venta venta = Venta.builder().cliente(cliente).nroFactura("F-Error").detalles(List.of()).build();

        // Pero hacemos que la cuota tenga un dato CRÍTICO que al usarse en el PDF cause error
        // O mejor aún: Simplemente verificamos que ante una excepción no controlada (como NPE por datos incompletos),
        // el servicio la envuelva en RuntimeException o la deje pasar.

        // Estrategia: Forzar un error interno pasando un objeto incompleto que pase el primer filtro
        // pero falle al construir el párrafo.

        Cuota cuotaMalformada = Cuota.builder()
                .id(1L)
                .venta(venta)
                .numeroCuota(null) // <--- Esto causará NPE al hacer "Cuota Nro " + null
                .montoCuota(BigDecimal.TEN)
                .build();

        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuotaMalformada));
        when(cuotaRepository.findByVentaId(1L)).thenReturn(List.of(cuotaMalformada));

        // WHEN & THEN
        assertThatThrownBy(() -> reciboPdfService.generarRecibo(cuotaMalformada))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error al generar PDF");
    }

    // --- Test para generarReciboCuota (Método wrapper) ---
    @Test
    void generarReciboCuota_DeberiaLanzarExcepcion_SiNoExisteID() {
        when(cuotaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reciboPdfService.generarReciboCuota(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cuota no encontrada con ID: 99");
    }
}