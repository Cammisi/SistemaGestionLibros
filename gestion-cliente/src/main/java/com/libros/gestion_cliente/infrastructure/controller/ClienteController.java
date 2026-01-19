package com.libros.gestion_cliente.infrastructure.controller;

import com.libros.gestion_cliente.application.dto.CrearClienteRequest;
import com.libros.gestion_cliente.application.service.ClienteService;
import com.libros.gestion_cliente.domain.model.Cliente;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gestión de la cartera de clientes")
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    @Operation(summary = "Registrar nuevo cliente")
    public ResponseEntity<Cliente> crearCliente(@Valid @RequestBody CrearClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crearCliente(request));
    }

    // --- CORREGIDO: Ahora acepta Pageable para coincidir con el Servicio ---
    @GetMapping
    @Operation(summary = "Listar clientes paginados", description = "Ejemplo: /api/clientes?page=0&size=10")
    public ResponseEntity<Page<Cliente>> listarClientes(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(clienteService.listarClientes(pageable));
    }

    @GetMapping("/{dni}")
    @Operation(summary = "Buscar cliente por DNI")
    public ResponseEntity<Cliente> buscarPorDni(@PathVariable String dni) {
        return ResponseEntity.ok(clienteService.buscarPorDni(dni));
    }
}