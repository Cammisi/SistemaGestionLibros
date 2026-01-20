package com.libros.gestion_cliente.infrastructure.report;

import com.libros.gestion_cliente.domain.model.Cuota;
import com.libros.gestion_cliente.domain.model.DetalleVenta;
import com.libros.gestion_cliente.domain.model.EstadoCuota;
import com.libros.gestion_cliente.domain.model.Venta;
import com.libros.gestion_cliente.domain.repository.CuotaRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <--- IMPORTANTE

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReciboPdfService {

    private final CuotaRepository cuotaRepository;

    // ========================================================================
    // MÉTODO 1: Para la App de Escritorio (JavaFX)
    // Guarda el archivo directamente en el escritorio del usuario
    // ========================================================================
    @Transactional // <--- EVITA EL ERROR LAZY INITIALIZATION
    public void generarRecibo(Cuota cuotaParametro) throws Exception {

        // 1. Recargamos la cuota desde la BD para tener la sesión abierta y traer Cliente/Venta
        Cuota cuota = cuotaRepository.findById(cuotaParametro.getId())
                .orElseThrow(() -> new RuntimeException("Cuota no encontrada"));

        // 2. Opcional: Al generar recibo, marcamos la cuota como PAGADA si no lo estaba
        if (cuota.getEstado() == EstadoCuota.PENDIENTE) {
            cuota.setEstado(EstadoCuota.PAGADA);
            cuota.setFechaPagoReal(LocalDate.now());
            cuotaRepository.save(cuota);
        }

        String userHome = System.getProperty("user.home");
        String directorio = userHome + File.separator + "Desktop" + File.separator + "Recibos";
        Files.createDirectories(Paths.get(directorio));

        String nombreArchivo = "Recibo_" + (cuota.getVenta().getNroFactura() != null ? cuota.getVenta().getNroFactura() : "S-N")
                + "_Cuota" + cuota.getNumeroCuota() + ".pdf";
        String rutaCompleta = directorio + File.separator + nombreArchivo;

        try (FileOutputStream fos = new FileOutputStream(rutaCompleta)) {
            generarPdfEnStream(cuota, fos);
        }
        System.out.println("Recibo guardado en: " + rutaCompleta);
    }

    // ========================================================================
    // MÉTODO 2: Para el Controlador REST (API) - Si lo usas
    // ========================================================================
    @Transactional
    public byte[] generarReciboCuota(Long cuotaId) {
        Cuota cuota = cuotaRepository.findById(cuotaId)
                .orElseThrow(() -> new RuntimeException("Cuota no encontrada con ID: " + cuotaId));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            generarPdfEnStream(cuota, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF para API", e);
        }
    }

    // ========================================================================
    // LÓGICA DE DIBUJO DEL PDF
    // ========================================================================
    private void generarPdfEnStream(Cuota cuota, OutputStream outputStream) throws DocumentException {
        Venta venta = cuota.getVenta();

        // --- LÓGICA MATEMÁTICA CORREGIDA (SALDO RESTANTE) ---
        // Buscamos todas las cuotas de esta venta
        List<Cuota> todasLasCuotas = cuotaRepository.findByVentaId(venta.getId());

        // El saldo es la suma de las cuotas POSTERIORES a la actual.
        // Ejemplo: Si estoy en la cuota 3, el saldo es la suma de las cuotas 4, 5...
        BigDecimal saldoRestante = todasLasCuotas.stream()
                .filter(c -> c.getNumeroCuota() > cuota.getNumeroCuota()) // Solo las futuras
                .map(c -> c.getMontoCuota() != null ? c.getMontoCuota() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Creación del Documento
        Document document = new Document(PageSize.A5.rotate());
        PdfWriter.getInstance(document, outputStream);
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

        document.add(new Paragraph("____________________________________________________________"));

        // --- TABLA DE DATOS ---
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(10);

        // COLUMNA IZQUIERDA
        PdfPCell celdaCliente = new PdfPCell();
        celdaCliente.setBorder(Rectangle.NO_BORDER);
        celdaCliente.addElement(new Paragraph("Recibí de: " + venta.getCliente().getNombre() + " " + venta.getCliente().getApellido()));

        String direccion = venta.getCliente().getDireccion() != null ? venta.getCliente().getDireccion() : "-";
        celdaCliente.addElement(new Paragraph("Domicilio: " + direccion + " (" + venta.getCliente().getLocalidad() + ")"));

        celdaCliente.addElement(new Paragraph("La suma de Pesos: $" + (cuota.getMontoCuota() != null ? cuota.getMontoCuota() : "0.00")));
        celdaCliente.addElement(new Paragraph("En concepto de: Cuota Nº " + cuota.getNumeroCuota() + " de " + venta.getCantidadCuotas()));

        // Libros
        StringBuilder librosStr = new StringBuilder();
        for (DetalleVenta dv : venta.getDetalles()) {
            if (librosStr.length() > 0) librosStr.append(", ");
            librosStr.append(dv.getLibro().getTitulo());
        }
        celdaCliente.addElement(new Paragraph("Obra: " + librosStr.toString()));
        tabla.addCell(celdaCliente);

        // COLUMNA DERECHA
        PdfPCell celdaAdmin = new PdfPCell();
        celdaAdmin.setBorder(Rectangle.NO_BORDER);
        celdaAdmin.addElement(new Paragraph("Nº Factura: " + (venta.getNroFactura() != null ? venta.getNroFactura() : "-")));

        String fechaTexto = cuota.getFechaPagoReal() != null
                ? cuota.getFechaPagoReal().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        celdaAdmin.addElement(new Paragraph("Fecha: " + fechaTexto));
        celdaAdmin.addElement(new Paragraph(" "));

        Font fontSaldo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        // Si el saldo es 0, mostramos "CANCELADO"
        if (saldoRestante.compareTo(BigDecimal.ZERO) == 0) {
            celdaAdmin.addElement(new Paragraph("SALDO DEUDA: $0.00 (CANCELADO)", fontSaldo));
        } else {
            celdaAdmin.addElement(new Paragraph("SALDO DEUDA: $" + saldoRestante, fontSaldo));
        }

        tabla.addCell(celdaAdmin);
        document.add(tabla);

        // --- PIE ---
        document.add(new Paragraph("\n\n\n"));
        Paragraph firma = new Paragraph("__________________________\nFirma Vendedor", fontSub);
        firma.setAlignment(Element.ALIGN_RIGHT);
        document.add(firma);

        document.close();
    }
}