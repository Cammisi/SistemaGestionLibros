package com.libros.gestion_cliente.infrastructure.controller;

import com.libros.gestion_cliente.application.dto.CrearFamiliarRequest;
import com.libros.gestion_cliente.application.service.FamiliarService;
import com.libros.gestion_cliente.domain.model.Familiar;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/familiares")
@RequiredArgsConstructor
@Tag(name = "Familiares", description = "Gestión de vínculos familiares de los clientes")
public class FamiliarController {

    private final FamiliarService familiarService;

    @PostMapping
    @Operation(summary = "Agregar familiar", description = "Asocia un nuevo familiar a un cliente existente.")
    public ResponseEntity<Familiar> agregarFamiliar(@Valid @RequestBody CrearFamiliarRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(familiarService.agregarFamiliar(request));
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar familiares de un cliente")
    public ResponseEntity<List<Familiar>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(familiarService.listarPorCliente(clienteId));
    }
}