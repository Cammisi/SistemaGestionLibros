package com.libros.gestion_cliente.infrastructure.controller;

import com.libros.gestion_cliente.application.dto.CrearVentaRequest;
import com.libros.gestion_cliente.application.service.VentaService;
import com.libros.gestion_cliente.domain.model.Venta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@Tag(name = "Ventas", description = "Operaciones relacionadas con el registro de ventas")
public class VentaController {

    private final VentaService ventaService;

    @Operation(summary = "Registrar una nueva venta",
            description = "Crea una venta verificando stock, deudas del cliente y duplicados.")
    @ApiResponse(responseCode = "201", description = "Venta creada exitosamente",
            content = @Content(schema = @Schema(implementation = Venta.class)))
    @ApiResponse(responseCode = "400", description = "Error de validación o regla de negocio (Stock, Deuda, etc)")
    @ApiResponse(responseCode = "404", description = "Cliente o Libro no encontrado")
    @PostMapping
    public ResponseEntity<Venta> registrarVenta(@Valid @RequestBody CrearVentaRequest request) {
        Venta nuevaVenta = ventaService.registrarVenta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaVenta);
    }
}