package com.libros.gestion_cliente.infrastructure.controller;

import com.libros.gestion_cliente.application.dto.CrearClienteRequest;
import com.libros.gestion_cliente.application.service.ClienteService;
import com.libros.gestion_cliente.domain.model.Cliente;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gestión de la cartera de clientes")
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    @Operation(summary = "Crear nuevo cliente", description = "Registra un cliente validando que el DNI no exista previamente.")
    public ResponseEntity<Cliente> crearCliente(@Valid @RequestBody CrearClienteRequest request) {
        Cliente nuevoCliente = clienteService.crearCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoCliente);
    }

    @GetMapping
    @Operation(summary = "Listar todos los clientes")
    public ResponseEntity<List<Cliente>> listarClientes() {
        return ResponseEntity.ok(clienteService.listarClientes());
    }

    @GetMapping("/{dni}")
    @Operation(summary = "Buscar cliente por DNI")
    public ResponseEntity<Cliente> buscarPorDni(@PathVariable String dni) {
        return ResponseEntity.ok(clienteService.buscarPorDni(dni));
    }
}