package com.libros.gestion_cliente.infrastructure.controller;

import com.libros.gestion_cliente.application.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Generación de informes de gestión")
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/ventas/mensual")
    @Operation(summary = "Descargar reporte de ventas (CSV)", description = "Genera un archivo CSV con el detalle de ventas del mes y año indicados.")
    public ResponseEntity<byte[]> descargarReporteMensual(
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getMonthValue()}") int mes,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int anio) {

        String reporteCsv = reporteService.generarReporteVentasMensuales(mes, anio);
        byte[] output = reporteCsv.getBytes();

        String filename = "ventas_" + mes + "_" + anio + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(output);
    }
}