package com.libros.gestion_cliente.application.service;

import com.libros.gestion_cliente.application.dto.CrearDetalleVentaRequest;
import com.libros.gestion_cliente.application.dto.CrearVentaRequest;
import com.libros.gestion_cliente.domain.model.*;
import com.libros.gestion_cliente.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final LibroRepository libroRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final CuotaRepository cuotaRepository;

    @Transactional
    public Venta registrarVenta(CrearVentaRequest request) {

        // 1. Validar Cliente
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // 2. Regla de Negocio: No vender si tiene deuda activa
        if (ventaRepository.existsByClienteIdAndEstado(cliente.getId(), EstadoVenta.EN_PROCESO)) {
            throw new IllegalStateException("El cliente tiene una venta en proceso. Debe finalizarla antes de comprar de nuevo.");
        }

        // 3. Crear la Cabecera de la Venta
        Venta venta = Venta.builder()
                .cliente(cliente)
                .fechaVenta(LocalDate.now())
                .nroFactura(generarNumeroFactura())
                .cantidadCuotas(request.getCantidadCuotas())
                .estado(EstadoVenta.EN_PROCESO)
                .build();

        List<DetalleVenta> detalles = new ArrayList<>();
        BigDecimal totalVenta = BigDecimal.ZERO;

        // 4. Procesar Libros (Detalles)
        for (CrearDetalleVentaRequest item : request.getDetalles()) {
            Libro libro = libroRepository.findById(item.getLibroId())
                    .orElseThrow(() -> new RuntimeException("Libro no encontrado: " + item.getLibroId()));

            // Regla: No vender duplicados
            if (detalleVentaRepository.haCompradoLibro(cliente.getId(), libro.getId())) {
                throw new IllegalStateException("El cliente ya compró el libro: " + libro.getTitulo());
            }

            // Regla: Stock
            if (libro.getStock() < item.getCantidad()) {
                throw new IllegalStateException("Stock insuficiente para: " + libro.getTitulo());
            }

            // Descontar Stock
            libro.setStock(libro.getStock() - item.getCantidad());
            libroRepository.save(libro);

            // Crear Detalle
            DetalleVenta detalle = DetalleVenta.builder()
                    .venta(venta)
                    .libro(libro)
                    .cantidad(item.getCantidad())
                    .precioAlMomento(libro.getPrecioBase())
                    .build();

            detalles.add(detalle);

            BigDecimal subtotal = libro.getPrecioBase().multiply(new BigDecimal(item.getCantidad()));
            totalVenta = totalVenta.add(subtotal);
        }

        venta.setDetalles(detalles);
        venta.setMontoTotal(totalVenta);

        // 5. Guardar Venta
        Venta ventaGuardada = ventaRepository.save(venta);

        // 6. Generar Cuotas (Usando método local)
        if (request.getCantidadCuotas() > 0) {
            this.generarCuotas(ventaGuardada);
        }

        return ventaGuardada;
    }

    // --- MÉTODOS PRIVADOS AUXILIARES ---

    private void generarCuotas(Venta venta) {
        BigDecimal total = venta.getMontoTotal();
        int cantidadCuotas = venta.getCantidadCuotas();

        // División exacta (ej: 100 / 3 = 33.33)
        BigDecimal montoCuotaBase = total.divide(BigDecimal.valueOf(cantidadCuotas), 2, RoundingMode.FLOOR);

        // El resto se suma a la primera cuota (ej: 0.01 sobrante)
        BigDecimal resto = total.subtract(montoCuotaBase.multiply(BigDecimal.valueOf(cantidadCuotas)));

        for (int i = 1; i <= cantidadCuotas; i++) {
            BigDecimal montoFinal = montoCuotaBase;
            if (i == 1) {
                montoFinal = montoFinal.add(resto);
            }

            Cuota cuota = Cuota.builder()
                    .venta(venta)
                    .numeroCuota(i)
                    .montoCuota(montoFinal)
                    .fechaVencimiento(LocalDate.now().plusMonths(i - 1))
                    .estado(EstadoCuota.PENDIENTE)
                    .build();

            cuotaRepository.save(cuota);
        }
    }

    private String generarNumeroFactura() {
        return "FAC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Transactional(readOnly = true)
    public List<Venta> listarVentasRecientes() {
        // Usamos el método nuevo findAllWithCliente en lugar de findAll
        return ventaRepository.findAllWithCliente(
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id")
        );
    }
}