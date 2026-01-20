package com.libros.gestion_cliente.infrastructure.report;

import com.libros.gestion_cliente.application.service.ReporteService;
import com.libros.gestion_cliente.domain.model.Cliente;
import com.libros.gestion_cliente.domain.model.DetalleVenta;
import com.libros.gestion_cliente.domain.model.Venta;
import com.libros.gestion_cliente.domain.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        // ... (Tu código existente para reporte mensual se queda igual) ...
        YearMonth yearMonth = YearMonth.of(anio, mes);
        LocalDate inicio = yearMonth.atDay(1);
        LocalDate fin = yearMonth.atEndOfMonth();
        List<Venta> ventas = ventaRepository.findByFechaVentaBetween(inicio, fin);
        StringBuilder sb = new StringBuilder();
        sb.append("ID_Venta,Fecha,Cliente_DNI,Cliente_Nombre,Libro_Titulo,Cantidad,Precio_Unitario,Total_Linea\n");
        for (Venta venta : ventas) {
            for (DetalleVenta detalle : venta.getDetalles()) {
                sb.append(venta.getId()).append(",");
                sb.append(venta.getFechaVenta()).append(",");
                sb.append(venta.getCliente().getDni()).append(",");
                sb.append(venta.getCliente().getNombre()).append(" ").append(venta.getCliente().getApellido()).append(",");
                sb.append(detalle.getLibro().getTitulo().replace(",", "")).append(",");
                sb.append(detalle.getCantidad()).append(",");
                sb.append(detalle.getPrecioAlMomento()).append(",");
                sb.append(detalle.getSubtotal()).append("\n");
            }
        }
        return sb.toString();
    }

    // --- NUEVO MÉTODO PARA SOLUCIONAR EL ERROR ---
    public void exportarClientes(List<Cliente> clientes) throws Exception {
        String userHome = System.getProperty("user.home");
        String directorio = userHome + File.separator + "Desktop" + File.separator + "Reportes";
        Files.createDirectories(Paths.get(directorio));

        String ruta = directorio + File.separator + "Hoja_de_Ruta_" + System.currentTimeMillis() + ".csv";

        try (PrintWriter writer = new PrintWriter(new File(ruta))) {
            // Cabecera
            writer.println("Localidad,Apellido,Nombre,Telefono,Direccion,Intereses");

            for (Cliente c : clientes) {
                String intereses = c.getInteresesPersonales() != null ? c.getInteresesPersonales().replace(",", " ") : "";

                writer.printf("%s,%s,%s,%s,%s,%s%n",
                        c.getLocalidad(),
                        c.getApellido(),
                        c.getNombre(),
                        c.getTelefono(),
                        c.getDireccion(),
                        intereses
                );
            }
        }
    }
}