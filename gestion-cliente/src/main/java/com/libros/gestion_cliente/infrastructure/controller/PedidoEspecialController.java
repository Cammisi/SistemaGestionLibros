package com.libros.gestion_cliente.infrastructure.controller;

import com.libros.gestion_cliente.application.dto.CrearPedidoRequest;
import com.libros.gestion_cliente.application.service.PedidoEspecialService;
import com.libros.gestion_cliente.domain.model.EstadoPedido;
import com.libros.gestion_cliente.domain.model.PedidoEspecial;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos Especiales", description = "Gestión de encargos de libros sin stock")
public class PedidoEspecialController {

    private final PedidoEspecialService pedidoService;

    @PostMapping
    @Operation(summary = "Crear un pedido especial")
    public ResponseEntity<PedidoEspecial> crearPedido(@Valid @RequestBody CrearPedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crearPedido(request));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado del pedido", description = "Ej: PENDIENTE -> DISPONIBLE")
    public ResponseEntity<PedidoEspecial> actualizarEstado(@PathVariable Long id, @RequestParam EstadoPedido estado) {
        return ResponseEntity.ok(pedidoService.cambiarEstado(id, estado));
    }

    @GetMapping("/pendientes")
    @Operation(summary = "Listar pedidos pendientes")
    public ResponseEntity<List<PedidoEspecial>> listarPendientes() {
        return ResponseEntity.ok(pedidoService.listarPendientes());
    }
}