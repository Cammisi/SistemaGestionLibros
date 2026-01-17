package com.libros.gestion_cliente.infrastructure.controller;

import com.libros.gestion_cliente.domain.model.Cliente;
import com.libros.gestion_cliente.domain.model.Venta;
import com.libros.gestion_cliente.domain.repository.ClienteRepository;
import com.libros.gestion_cliente.domain.repository.VentaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/estrategia")
@RequiredArgsConstructor
@Tag(name = "Estrategia de Ventas", description = "Herramientas para organizar visitas y ventas")
public class EstrategiaVentaController {

    private final ClienteRepository clienteRepository;
    private final VentaRepository ventaRepository;

    @GetMapping("/localidad")
    @Operation(summary = "Filtrar por localidad", description = "Lista clientes de una zona para armar la hoja de ruta.")
    public ResponseEntity<List<Cliente>> listarPorLocalidad(@RequestParam String zona) {
        return ResponseEntity.ok(clienteRepository.findByLocalidadContainingIgnoreCase(zona));
    }

    @GetMapping("/interesados")
    @Operation(summary = "Buscar clientes por interés", description = "Ej: Buscar quiénes quieren libros de 'Cocina'.")
    public ResponseEntity<List<Cliente>> buscarInteresados(@RequestParam String tema) {
        return ResponseEntity.ok(clienteRepository.findByInteresesPersonalesContainingIgnoreCase(tema));
    }

    @GetMapping("/historial/{clienteId}")
    @Operation(summary = "Ver historial de compras", description = "Muestra qué compró el cliente para no repetir ofertas.")
    public ResponseEntity<List<Map<String, Object>>> verHistorial(@PathVariable Long clienteId) {
        List<Venta> ventas = ventaRepository.findByClienteId(clienteId);

        List<Map<String, Object>> historial = ventas.stream()
                .flatMap(v -> v.getDetalles().stream().map(d -> Map.<String, Object>of( // <--- AQUÍ ESTÁ LA MAGIA
                        "fecha", v.getFechaVenta(),
                        "libro", d.getLibro().getTitulo(),
                        "estado", v.getEstado()
                )))
                .collect(Collectors.toList());

        return ResponseEntity.ok(historial);
    }

    @GetMapping("/libres-deuda")
    @Operation(summary = "Clientes sin deuda", description = "Lista clientes que han finalizado todos sus pagos, ordenados por localidad.")
    public ResponseEntity<List<Cliente>> listarLibresDeDeuda() {
        return ResponseEntity.ok(clienteRepository.findClientesLibresDeDeuda());
    }
}