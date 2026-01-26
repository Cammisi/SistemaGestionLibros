package com.libros.gestion_cliente.application.service;

import com.libros.gestion_cliente.domain.model.Cuota;
import com.libros.gestion_cliente.domain.model.EstadoCuota;
import com.libros.gestion_cliente.domain.model.EstadoVenta;
import com.libros.gestion_cliente.domain.model.Venta;
import com.libros.gestion_cliente.domain.repository.CuotaRepository;
import com.libros.gestion_cliente.domain.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CuotaService {

    private final CuotaRepository cuotaRepository;
    private final VentaRepository ventaRepository; // Necesitamos guardar la venta si cambia de estado

    @Transactional
    public Cuota registrarPago(Long cuotaId) {
        // 1. Buscar Cuota
        Cuota cuota = cuotaRepository.findById(cuotaId)
                .orElseThrow(() -> new RuntimeException("Cuota no encontrada: " + cuotaId));

        // 2. Validar que no esté pagada
        if (cuota.getEstado() == EstadoCuota.PAGADA) {
            throw new IllegalStateException("La cuota ya está pagada");
        }

        // 3. Registrar Pago
        cuota.setEstado(EstadoCuota.PAGADA);
        cuota.setFechaPagoReal(LocalDate.now());
        Cuota cuotaGuardada = cuotaRepository.save(cuota);

        // 4. Verificar si la Venta se completó
        verificarFinalizacionVenta(cuota.getVenta());

        return cuotaGuardada;
    }

    private void verificarFinalizacionVenta(Venta venta) {
        // Contamos cuántas cuotas siguen PENDIENTES para esta venta
        long pendientes = cuotaRepository.countByVentaIdAndEstado(venta.getId(), EstadoCuota.PENDIENTE);

        if (pendientes == 0) {
            // ¡No debe nada! Cerramos la venta.
            venta.setEstado(EstadoVenta.FINALIZADA);
            ventaRepository.save(venta);
        }
    }

    public Cuota buscarPorId(Long id) {
        return cuotaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuota no encontrada con ID: " + id));
    }
}