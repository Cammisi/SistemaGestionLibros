package com.libros.gestion_cliente.infrastructure.report;

import com.libros.gestion_cliente.application.service.ReporteService;
import com.libros.gestion_cliente.domain.model.DetalleVenta;
import com.libros.gestion_cliente.domain.model.Venta;
import com.libros.gestion_cliente.domain.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvReporteService implements ReporteService {

    private final VentaRepository ventaRepository;

    @Override
    @Transactional(readOnly = true)
    public String generarReporteVentasMensuales(int mes, int anio) {
        YearMonth yearMonth = YearMonth.of(anio, mes);
        LocalDate inicio = yearMonth.atDay(1);
        LocalDate fin = yearMonth.atEndOfMonth();

        List<Venta> ventas = ventaRepository.findByFechaVentaBetween(inicio, fin);

        StringBuilder sb = new StringBuilder();
        // Cabecera del CSV
        sb.append("ID_Venta,Fecha,Cliente_DNI,Cliente_Nombre,Libro_Titulo,Cantidad,Precio_Unitario,Total_Linea\n");

        for (Venta venta : ventas) {
            for (DetalleVenta detalle : venta.getDetalles()) {
                sb.append(venta.getId()).append(",");
                sb.append(venta.getFechaVenta()).append(",");
                sb.append(venta.getCliente().getDni()).append(",");
                sb.append(venta.getCliente().getNombre()).append(" ").append(venta.getCliente().getApellido()).append(",");
                sb.append(detalle.getLibro().getTitulo().replace(",", "")).append(","); // Evitar romper CSV con comas en títulos
                sb.append(detalle.getCantidad()).append(",");
                sb.append(detalle.getPrecioAlMomento()).append(",");
                sb.append(detalle.getSubtotal()).append("\n");
            }
        }

        if (ventas.isEmpty()) {
            sb.append("No se encontraron ventas para el período seleccionado.");
        }

        return sb.toString();
    }
}