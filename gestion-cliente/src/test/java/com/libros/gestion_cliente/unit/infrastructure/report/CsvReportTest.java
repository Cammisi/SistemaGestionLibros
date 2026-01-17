package com.libros.gestion_cliente.unit.infrastructure.report;

import com.libros.gestion_cliente.domain.model.Cliente;
import com.libros.gestion_cliente.domain.model.DetalleVenta;
import com.libros.gestion_cliente.domain.model.Libro;
import com.libros.gestion_cliente.domain.model.Venta;
import com.libros.gestion_cliente.domain.repository.VentaRepository;
import com.libros.gestion_cliente.infrastructure.report.CsvReporteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsvReporteServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @InjectMocks
    private CsvReporteService csvReporteService;

    @Test
    void generarReporteVentasMensuales_DeberiaGenerarCsvConDatos_CuandoHayVentas() {
        // GIVEN: Preparamos datos simulados
        Cliente cliente = Cliente.builder()
                .dni("12345678")
                .nombre("Juan")
                .apellido("Perez")
                .build();

        // Ponemos una coma en el título para probar el .replace(",", "")
        Libro libro = Libro.builder()
                .titulo("Java, The Complete Reference")
                .build();

        DetalleVenta detalle = DetalleVenta.builder()
                .libro(libro)
                .cantidad(1)
                .precioAlMomento(new BigDecimal("50.00"))
                .build();
        // Simulamos un subtotal (si tu entidad no lo calcula solo, lo seteamos aquí,
        // o si es calculado, nos aseguramos que precio y cantidad estén bien)
        // Para este test, si el método getSubtotal() es calculado en la entidad, funcionará.
        // Si no, mockeamos o confiamos en que devuelve algo (null o valor).

        Venta venta = Venta.builder()
                .id(1L)
                .fechaVenta(LocalDate.now())
                .cliente(cliente)
                .detalles(List.of(detalle))
                .build();

        when(ventaRepository.findByFechaVentaBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(venta));

        // WHEN
        String csv = csvReporteService.generarReporteVentasMensuales(1, 2026);

        // THEN
        // Verificamos cabecera
        assertThat(csv).contains("ID_Venta,Fecha,Cliente_DNI");
        // Verificamos datos del cliente
        assertThat(csv).contains("12345678").contains("Juan Perez");
        // Verificamos que se eliminó la coma del título del libro
        assertThat(csv).contains("Java The Complete Reference");
        assertThat(csv).doesNotContain("Java, The Complete Reference");
    }

    @Test
    void generarReporteVentasMensuales_DeberiaRetornarMensaje_CuandoNoHayVentas() {
        // GIVEN: Repositorio retorna lista vacía
        when(ventaRepository.findByFechaVentaBetween(any(), any()))
                .thenReturn(Collections.emptyList());

        // WHEN
        String csv = csvReporteService.generarReporteVentasMensuales(1, 2026);

        // THEN
        // Verificamos cabecera
        assertThat(csv).contains("ID_Venta,Fecha");
        // Verificamos mensaje de vacío (Esto cubre el if(ventas.isEmpty()))
        assertThat(csv).contains("No se encontraron ventas para el período seleccionado.");
    }
}