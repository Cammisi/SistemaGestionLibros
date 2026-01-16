package com.libros.gestion_cliente.infrastructure.controller;

import com.libros.gestion_cliente.application.service.CuotaService;
import com.libros.gestion_cliente.domain.model.Cuota;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cuotas")
@RequiredArgsConstructor
@Tag(name = "Cuotas", description = "Gestión de pagos y cobranzas")
public class CuotaController {

    private final CuotaService cuotaService;

    @PostMapping("/{id}/pagar")
    @Operation(summary = "Registrar pago de cuota",
            description = "Marca una cuota como PAGADA. Si es la última, finaliza la venta asociada.")
    public ResponseEntity<Cuota> registrarPago(@PathVariable Long id) {
        return ResponseEntity.ok(cuotaService.registrarPago(id));
    }
}