package com.libros.gestion_cliente.infrastructure.report;

import com.libros.gestion_cliente.domain.model.Cuota;
import com.libros.gestion_cliente.domain.repository.CuotaRepository;
import com.libros.gestion_cliente.domain.model.PedidoEspecial;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // --- MÉTODO QUE FALTABA (SOLUCIÓN AL ERROR) ---
    // Este método permite llamar al servicio usando solo el ID (para la API REST)
    @Transactional(readOnly = true)
    public void generarReciboCuota(Long idCuota) throws Exception {
        Cuota cuota = cuotaRepository.findById(idCuota)
                .orElseThrow(() -> new RuntimeException("Cuota no encontrada con ID: " + idCuota));

        // Reutilizamos la lógica existente
        generarRecibo(cuota);
    }

    // --- Generar archivo en disco (Escritorio) ---
    @Transactional(readOnly = true)
    public void generarRecibo(Cuota cuotaParametro) throws Exception {
        // Recuperar la cuota completa
        Cuota cuota = cuotaRepository.findById(cuotaParametro.getId())
                .orElseThrow(() -> new RuntimeException("Cuota no encontrada"));

        // Definir ruta
        String userHome = System.getProperty("user.home");
        String directorio = userHome + File.separator + "Desktop" + File.separator + "Recibos";
        Files.createDirectories(Paths.get(directorio));

        String nombreArchivo = "Recibo_" + (cuota.getVenta().getNroFactura() != null ? cuota.getVenta().getNroFactura() : "S-N")
                + "_Cuota" + cuota.getNumeroCuota() + ".pdf";
        String rutaCompleta = directorio + File.separator + nombreArchivo;

        // Generar
        try (FileOutputStream fos = new FileOutputStream(rutaCompleta)) {
            generarPdfEnStream(cuota, fos);
        }

        System.out.println("Recibo guardado en: " + rutaCompleta);
    }

    // --- Lógica interna de iText ---
    @Transactional(readOnly = true)
    public void generarPdfEnStream(Cuota cuotaParam, OutputStream outputStream) {
        Cuota cuota = cuotaRepository.findById(cuotaParam.getId())
                .orElseThrow(() -> new RuntimeException("Cuota no encontrada"));

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            if (cuota.getNumeroCuota() == 1) {
                generarContenidoContrato(document, cuota);
            } else {
                generarContenidoRecibo(document, cuota);
            }

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF", e);
        }
    }

    private void generarContenidoContrato(Document document, Cuota cuota) throws DocumentException {
        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font fontNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

        Paragraph titulo = new Paragraph("NOTA DE PEDIDO / CONTRATO", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);

        Paragraph subtitulo = new Paragraph("DOCUMENTO NO VÁLIDO COMO FACTURA", fontNormal);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        document.add(subtitulo);
        document.add(new Paragraph("----------------------------------------------------------------------------------", fontNormal));

        document.add(new Paragraph("Fecha: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), fontNormal));
        document.add(new Paragraph("Cliente: " + cuota.getVenta().getCliente().getApellido() + " " + cuota.getVenta().getCliente().getNombre(), fontNormal));
        document.add(new Paragraph("Dirección: " + cuota.getVenta().getCliente().getDireccion(), fontNormal));
        document.add(new Paragraph("Teléfono: " + cuota.getVenta().getCliente().getTelefono(), fontNormal));

        document.add(new Paragraph(" ", fontNormal));

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.addCell(new Phrase("OBRA / TÍTULO", fontNegrita));
        tabla.addCell(new Phrase("CANTIDAD", fontNegrita));

        cuota.getVenta().getDetalles().forEach(d -> {
            tabla.addCell(new Phrase(d.getLibro().getTitulo(), fontNormal));
            tabla.addCell(new Phrase(String.valueOf(d.getCantidad()), fontNormal));
        });
        document.add(tabla);

        document.add(new Paragraph(" ", fontNormal));
        document.add(new Paragraph("TOTAL OPERACIÓN: $ " + cuota.getVenta().getMontoTotal(), fontTitulo));
        document.add(new Paragraph("Pago Inicial (Esta Cuota): $ " + cuota.getMontoCuota(), fontNegrita));

        document.add(new Paragraph(" ", fontNormal));
        document.add(new Paragraph("El saldo restante lo abonaré en cuotas mensuales de $ " + cuota.getMontoCuota() + " del 1 al 10 de cada mes.", fontNormal));

        document.add(new Paragraph("\n\n\n\n", fontNormal));

        PdfPTable tablaFirmas = new PdfPTable(2);
        tablaFirmas.setWidthPercentage(100);
        tablaFirmas.getDefaultCell().setBorder(0);

        tablaFirmas.addCell(new Paragraph("__________________________\nFirma Cliente", fontNormal));
        tablaFirmas.addCell(new Paragraph("__________________________\nFirma Vendedor", fontNormal));

        document.add(tablaFirmas);
    }

    private void generarContenidoRecibo(Document document, Cuota cuota) throws DocumentException {
        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 12);

        document.add(new Paragraph("RECIBO DE COBRANZA", fontTitulo));
        document.add(new Paragraph("------------------------------------------------", fontNormal));
        document.add(new Paragraph("Fecha: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), fontNormal));
        document.add(new Paragraph("Recibimos de: " + cuota.getVenta().getCliente().getApellido() + " " + cuota.getVenta().getCliente().getNombre(), fontNormal));
        document.add(new Paragraph("La suma de Pesos: $ " + cuota.getMontoCuota(), fontTitulo));
        document.add(new Paragraph("En concepto de: Cuota Nro " + cuota.getNumeroCuota() + " del plan de pagos.", fontNormal));

        BigDecimal saldo = calcularSaldoRestante(cuota);
        document.add(new Paragraph("Saldo Restante Estimado: $ " + saldo, fontNormal));

        document.add(new Paragraph("\n\n__________________________\nFirma y Sello", fontNormal));
    }

    private BigDecimal calcularSaldoRestante(Cuota cuota) {
        List<Cuota> todasLasCuotas = cuota.getVenta().getCuotas();
        if (todasLasCuotas == null || todasLasCuotas.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return todasLasCuotas.stream()
                .filter(c -> c.getNumeroCuota() > cuota.getNumeroCuota())
                .map(c -> c.getMontoCuota() != null ? c.getMontoCuota() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
    }

    public void generarListaReposicion(List<PedidoEspecial> pedidos) throws Exception {
        String userHome = System.getProperty("user.home");
        String directorio = userHome + File.separator + "Desktop" + File.separator + "Reportes";
        Files.createDirectories(Paths.get(directorio));

        String rutaCompleta = directorio + File.separator + "Lista_Reposicion_" + LocalDate.now() + ".pdf";

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(rutaCompleta));
        document.open();

        Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        document.add(new Paragraph("LISTA DE REPOSICIÓN / PEDIDOS A EDITORIAL", tituloFont));
        document.add(new Paragraph("Fecha: " + LocalDate.now(), normalFont));
        document.add(new Paragraph(" ", normalFont));

        PdfPTable tabla = new PdfPTable(3); // Columnas: Libro, Cliente, Estado
        tabla.setWidthPercentage(100);
        tabla.addCell(new Phrase("OBRA / TÍTULO", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
        tabla.addCell(new Phrase("CLIENTE QUE LO PIDIÓ", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
        tabla.addCell(new Phrase("FECHA PEDIDO", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));

        for (PedidoEspecial p : pedidos) {
            tabla.addCell(new Phrase(p.getDescripcion(), normalFont));
            tabla.addCell(new Phrase(p.getCliente().getApellido() + " " + p.getCliente().getNombre(), normalFont));
            tabla.addCell(new Phrase(p.getFechaPedido().toString(), normalFont));
        }

        document.add(tabla);
        document.add(new Paragraph("\nTotal de libros a pedir: " + pedidos.size(), normalFont));

        document.close();
        System.out.println("Lista generada en: " + rutaCompleta);
    }
}