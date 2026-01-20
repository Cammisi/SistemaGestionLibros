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
    void generarReporteVentasMensuales_DeberiaGenerarCsvCorrecto_CuandoHayVentas() {
        // GIVEN
        Venta venta = Venta.builder()
                .id(1L)
                .fechaVenta(LocalDate.now())
                .cliente(Cliente.builder().dni("123").nombre("Juan").apellido("Perez").build())
                .detalles(List.of(
                        DetalleVenta.builder()
                                .libro(Libro.builder().titulo("Libro A").build())
                                .cantidad(2)
                                .precioAlMomento(new BigDecimal("50.00"))
                                // .subtotal(...) ELIMINADO: Se calcula solo
                                .build()
                ))
                .build();

        when(ventaRepository.findByFechaVentaBetween(any(), any())).thenReturn(List.of(venta));

        // WHEN
        String csv = csvReporteService.generarReporteVentasMensuales(1, 2025);

        // THEN
        assertThat(csv).contains("ID_Venta,Fecha"); // Cabecera
        assertThat(csv).contains("Juan Perez");
        assertThat(csv).contains("Libro A");
        // 2 * 50.00 = 100.00. Verificamos que el cálculo aparezca en el CSV
        assertThat(csv).contains("100.00");
    }

    @Test
    void generarReporteVentasMensuales_DeberiaRetornarMensaje_CuandoNoHayVentas() {
        // GIVEN
        when(ventaRepository.findByFechaVentaBetween(any(), any())).thenReturn(Collections.emptyList());

        // WHEN
        String csv = csvReporteService.generarReporteVentasMensuales(1, 2025);

        // THEN
        assertThat(csv).contains("No se encontraron ventas para el período seleccionado");
    }
}