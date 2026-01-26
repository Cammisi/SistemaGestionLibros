package com.libros.gestion_cliente.infrastructure.controller;

import com.libros.gestion_cliente.application.service.CuotaService;
import com.libros.gestion_cliente.infrastructure.report.ReciboPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cuotas")
@RequiredArgsConstructor
public class CuotaController {

    private final CuotaService cuotaService;
    private final ReciboPdfService reciboPdfService;

    // POST: Registrar pago
    @PostMapping("/{id}/pagar")
    public ResponseEntity<Void> pagarCuota(@PathVariable Long id) {
        cuotaService.registrarPago(id);
        return ResponseEntity.ok().build();
    }

    // GET: Descargar recibo (Genera en Escritorio por ahora)
    @GetMapping("/{id}/recibo")
    public ResponseEntity<String> generarRecibo(@PathVariable Long id) {
        try {
            // Llamamos al método que acabamos de crear en el servicio
            reciboPdfService.generarReciboCuota(id);
            return ResponseEntity.ok("Recibo generado correctamente en el Escritorio.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al generar recibo: " + e.getMessage());
        }
    }
}