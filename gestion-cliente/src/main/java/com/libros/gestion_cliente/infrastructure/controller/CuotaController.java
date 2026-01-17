package com.libros.gestion_cliente.infrastructure.controller;

import com.libros.gestion_cliente.application.service.CuotaService;
import com.libros.gestion_cliente.domain.model.Cuota;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.libros.gestion_cliente.infrastructure.report.ReciboPdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/cuotas")
@RequiredArgsConstructor
@Tag(name = "Cuotas", description = "Gestión de pagos y cobranzas")
public class CuotaController {

    private final CuotaService cuotaService;
    private final ReciboPdfService reciboPdfService;

    @PostMapping("/{id}/pagar")
    @Operation(summary = "Registrar pago de cuota",
            description = "Marca una cuota como PAGADA. Si es la última, finaliza la venta asociada.")
    public ResponseEntity<Cuota> registrarPago(@PathVariable Long id) {
        return ResponseEntity.ok(cuotaService.registrarPago(id));
    }

    @GetMapping("/{id}/recibo")
    @Operation(summary = "Descargar recibo PDF", description = "Genera el comprobante de pago para imprimir.")
    public ResponseEntity<byte[]> descargarRecibo(@PathVariable Long id) {
        byte[] pdf = reciboPdfService.generarReciboCuota(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"recibo_cuota_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}