package com.libros.gestion_cliente.infrastructure.report;

import com.libros.gestion_cliente.domain.model.Cuota;
import com.libros.gestion_cliente.domain.model.Venta;
import com.libros.gestion_cliente.domain.repository.CuotaRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReciboPdfService {

    private final CuotaRepository cuotaRepository;

    public byte[] generarReciboCuota(Long cuotaId) {
        Cuota cuota = cuotaRepository.findById(cuotaId)
                .orElseThrow(() -> new RuntimeException("Cuota no encontrada"));
        Venta venta = cuota.getVenta();

        // Calcular saldo restante (cuotas pendientes)
        // Se agrega validación para evitar NullPointerException si getMontoCuota es null
        List<Cuota> todasLasCuotas = cuotaRepository.findByVentaId(venta.getId());
        BigDecimal saldoRestante = todasLasCuotas.stream()
                .filter(c -> !c.getEstado().name().equals("PAGADA"))
                .map(c -> c.getMontoCuota() != null ? c.getMontoCuota() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A5.rotate()); // Formato apaisado
            PdfWriter.getInstance(document, out);
            document.open();

            // --- ENCABEZADO ---
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph titulo = new Paragraph("José Hernández", fontTitulo);
            titulo.setAlignment(Element.ALIGN_LEFT);
            document.add(titulo);

            Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Paragraph subtitulo = new Paragraph("E D I T O R I A L   -   Santa Fe", fontSub);
            subtitulo.setSpacingAfter(10);
            document.add(subtitulo);

            // Línea separadora
            document.add(new Paragraph("____________________________________________________________"));

            // --- DATOS DEL RECIBO ---
            PdfPTable tabla = new PdfPTable(2);
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(10);

            // Columna Izquierda (Datos Cliente)
            PdfPCell celdaCliente = new PdfPCell();
            celdaCliente.setBorder(Rectangle.NO_BORDER);
            celdaCliente.addElement(new Paragraph("Recibí de: " + venta.getCliente().getNombre() + " " + venta.getCliente().getApellido()));
            celdaCliente.addElement(new Paragraph("Domicilio: " + (venta.getCliente().getDireccion() != null ? venta.getCliente().getDireccion() : "-")));
            celdaCliente.addElement(new Paragraph("La suma de Pesos: $" + (cuota.getMontoCuota() != null ? cuota.getMontoCuota() : "0.00")));
            celdaCliente.addElement(new Paragraph("En concepto de: Cuota Nº " + cuota.getNumeroCuota() + " de " + venta.getCantidadCuotas()));

            // Mostrar qué libro compró
            String libroTitulo = venta.getDetalles().stream()
                    .map(d -> d.getLibro().getTitulo())
                    .findFirst().orElse("Varios");
            celdaCliente.addElement(new Paragraph("Obra: " + libroTitulo));

            tabla.addCell(celdaCliente);

            // Columna Derecha (Datos Administrativos)
            PdfPCell celdaAdmin = new PdfPCell();
            celdaAdmin.setBorder(Rectangle.NO_BORDER);

            // Validación Nro Factura
            celdaAdmin.addElement(new Paragraph("Nº Factura: " + (venta.getNroFactura() != null ? venta.getNroFactura() : "-")));

            // --- CORRECCIÓN CLAVE: Validación de Fecha ---
            // Si fechaPagoReal es null (no se pagó aún), usamos la fecha de hoy para la impresión.
            String fechaTexto = cuota.getFechaPagoReal() != null
                    ? cuota.getFechaPagoReal().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    : LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            celdaAdmin.addElement(new Paragraph("Fecha: " + fechaTexto));
            celdaAdmin.addElement(new Paragraph(" "));

            Font fontSaldo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            celdaAdmin.addElement(new Paragraph("SALDO: $" + saldoRestante, fontSaldo));

            tabla.addCell(celdaAdmin);

            document.add(tabla);

            // --- PIE DE PÁGINA (Firma) ---
            document.add(new Paragraph("\n\n\n"));
            Paragraph firma = new Paragraph("__________________________\nFirma Vendedor", fontSub);
            firma.setAlignment(Element.ALIGN_RIGHT);
            document.add(firma);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF", e);
        }
    }
}