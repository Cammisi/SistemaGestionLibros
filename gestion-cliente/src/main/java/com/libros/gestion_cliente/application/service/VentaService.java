package com.libros.gestion_cliente.application.service;

import com.libros.gestion_cliente.application.dto.CrearVentaRequest;
import com.libros.gestion_cliente.domain.model.*;
import com.libros.gestion_cliente.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor // Inyecta los repositorios automáticamente
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final LibroRepository libroRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final CuotaRepository cuotaRepository;

    @Transactional // <--- ¡Vital! O todo o nada.
    public Venta registrarVenta(CrearVentaRequest request) {

        // 1. Validar Cliente
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // 2. Regla de Negocio: No vender si tiene deuda activa
        // Asumimos que EN_PROCESO significa que aún está pagando cuotas
        if (ventaRepository.existsByClienteIdAndEstado(cliente.getId(), EstadoVenta.EN_PROCESO)) {
            throw new IllegalStateException("El cliente tiene una venta en proceso. Debe finalizarla antes de comprar de nuevo.");
        }

        // 3. Crear la Cabecera de la Venta
        Venta venta = Venta.builder()
                .cliente(cliente)
                .fechaVenta(LocalDate.now())
                .nroFactura(generarNumeroFactura()) // Podrías usar un UUID o una secuencia
                .cantidadCuotas(request.getCantidadCuotas())
                .estado(EstadoVenta.EN_PROCESO) // Nace debiendo
                .build();

        // 4. Procesar Libros (Detalles)
        for (CrearVentaRequest.ItemVenta item : request.getItems()) {
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
            libroRepository.save(libro); // Actualizamos inventario

            // Agregar a la venta (esto calcula el subtotal y total automáticamente gracias a tu entidad)
            DetalleVenta detalle = DetalleVenta.builder()
                    .libro(libro)
                    .cantidad(item.getCantidad())
                    .precioAlMomento(libro.getPrecioBase())
                    .build();

            venta.addDetalle(detalle);
        }

        // 5. Guardar Venta (Cascade guardará los detalles)
        Venta ventaGuardada = ventaRepository.save(venta);

        // 6. Generar Cuotas (Matemática Financiera)
        generarCuotas(ventaGuardada);

        return ventaGuardada;
    }

    private void generarCuotas(Venta venta) {
        BigDecimal total = venta.getMontoTotal();
        int cantidadCuotas = venta.getCantidadCuotas();

        // División exacta (ej: 100 / 3 = 33.33)
        BigDecimal montoCuotaBase = total.divide(BigDecimal.valueOf(cantidadCuotas), 2, RoundingMode.FLOOR);

        // El resto se suma a la primera cuota para que cierre perfecto (ej: 0.01 sobrante)
        BigDecimal resto = total.subtract(montoCuotaBase.multiply(BigDecimal.valueOf(cantidadCuotas)));

        for (int i = 1; i <= cantidadCuotas; i++) {
            BigDecimal montoFinal = montoCuotaBase;
            if (i == 1) {
                montoFinal = montoFinal.add(resto); // Ajuste en la cuota 1
            }

            Cuota cuota = Cuota.builder()
                    .venta(venta)
                    .numeroCuota(i)
                    .montoCuota(montoFinal)
                    .fechaVencimiento(LocalDate.now().plusMonths(i - 1)) // Cuota 1 vence hoy (pago inicial) o el mes que viene
                    .estado(EstadoCuota.PENDIENTE)
                    .build();

            cuotaRepository.save(cuota);
        }
    }

    private String generarNumeroFactura() {
        // Simple generador para el ejemplo. En prod usarías una secuencia de DB o lógica fiscal.
        return "FAC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}