package alicanteweb.erp.service;

import alicanteweb.erp.entities.AlbaranVenta;
import alicanteweb.erp.entities.AlbaranVentaLinea;
import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaLinea;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Service
public class ImpresionService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final String OUTPUT_DIR = "impresiones/";

    private final EmpresaConfigService empresaConfigService;
    private final FacturacionEventoService facturacionEventoService;

    public ImpresionService(EmpresaConfigService empresaConfigService,
                            FacturacionEventoService facturacionEventoService) {
        this.empresaConfigService = empresaConfigService;
        this.facturacionEventoService = facturacionEventoService;
        try {
            Files.createDirectories(Paths.get(OUTPUT_DIR));
        } catch (Exception e) {
            log.error("Error creando directorio de impresiones", e);
        }
    }

    public void imprimirFactura(Factura factura, boolean abrirPdf) {
        try {
            log.info("Generando impresion de factura: {}", factura.getNumero());
            File pdfFile = generarFacturaPdf(factura);
            registrarEventoDocumento("IMPRESION_FACTURA", factura.getNumero(), pdfFile);
            log.info("Factura generada: {}", pdfFile.getAbsolutePath());
            imprimirPdf(pdfFile, abrirPdf);
        } catch (Exception e) {
            log.error("Error imprimiendo factura", e);
            throw new RuntimeException("Error al generar PDF de factura: " + e.getMessage(), e);
        }
    }

    public void imprimirAlbaran(AlbaranVenta albaran, boolean abrirPdf) {
        try {
            log.info("Generando impresion de albaran: {}", albaran.getNumero());
            File pdfFile = generarAlbaranPdf(albaran);
            registrarEventoDocumento("IMPRESION_ALBARAN", albaran.getNumero(), pdfFile);
            log.info("Albaran generado: {}", pdfFile.getAbsolutePath());
            imprimirPdf(pdfFile, abrirPdf);
        } catch (Exception e) {
            log.error("Error imprimiendo albaran", e);
            throw new RuntimeException("Error al generar PDF de albaran: " + e.getMessage(), e);
        }
    }

    public File generarFacturaPdf(Factura factura) throws Exception {
        String html = generarHtmlFactura(factura);
        File pdfFile = buildOutputFile("Factura", factura.getNumero());
        generarPdf(html, pdfFile);
        return pdfFile;
    }

    public File generarAlbaranPdf(AlbaranVenta albaran) throws Exception {
        String html = generarHtmlAlbaran(albaran);
        File pdfFile = buildOutputFile("Albaran", albaran.getNumero());
        generarPdf(html, pdfFile);
        return pdfFile;
    }

    public void imprimirPdf(File pdfFile, boolean abrirPdf) {
        if (pdfFile == null || !pdfFile.exists()) {
            throw new IllegalArgumentException("El PDF a imprimir no existe");
        }
        if (abrirPdf) {
            abrirPdf(pdfFile.getAbsolutePath());
        }
    }

    public String getDirectorioImpresiones() {
        return new File(OUTPUT_DIR).getAbsolutePath();
    }

    private String generarHtmlFactura(Factura factura) {
        StringBuilder html = new StringBuilder();
        EmpresaConfig empresa = empresaConfigService.getConfiguracionActivaOrThrow();

        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; font-size: 12px; margin: 20px; }");
        html.append(".header { text-align: center; margin-bottom: 20px; border-bottom: 2px solid #000; padding-bottom: 10px; }");
        html.append(".empresa { font-size: 14px; font-weight: bold; }");
        html.append(".section { margin-top: 15px; }");
        html.append(".cliente-info { background: #f0f0f0; padding: 10px; margin: 10px 0; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-top: 10px; }");
        html.append("th { background: #333; color: white; padding: 8px; text-align: left; }");
        html.append("td { padding: 6px; border-bottom: 1px solid #ddd; }");
        html.append(".totales { text-align: right; margin-top: 20px; font-size: 13px; }");
        html.append(".total-final { font-size: 16px; font-weight: bold; }");
        html.append(".verifactu { margin-top: 30px; padding: 15px; background: #e8f5e9; border: 2px solid #4caf50; }");
        html.append(".verifactu-title { font-weight: bold; color: #2e7d32; margin-bottom: 10px; }");
        html.append(".qr-container { text-align: center; margin: 15px 0; }");
        html.append(".footer { margin-top: 30px; text-align: center; font-size: 10px; color: #666; }");
        html.append("</style>");
        html.append("</head><body>");

        html.append("<div class='header'>");
        html.append("<div class='empresa'>").append(escapeHtml(empresa.getNombreEmpresa())).append("</div>");
        html.append("<div>").append(escapeHtml(empresa.getCif())).append("</div>");
        html.append("<div>").append(escapeHtml(empresa.getDireccion())).append("</div>");
        html.append("<div>").append(escapeHtml(empresa.getCodigoPostal())).append(" - ")
            .append(escapeHtml(empresa.getCiudad())).append("</div>");
        if (empresa.getTelefono() != null) {
            html.append("<div>Tel: ").append(escapeHtml(empresa.getTelefono())).append("</div>");
        }
        if (empresa.getEmail() != null) {
            html.append("<div>Email: ").append(escapeHtml(empresa.getEmail())).append("</div>");
        }
        html.append("</div>");

        html.append("<h2 style='text-align: center; color: #333;'>FACTURA ")
            .append(escapeHtml(factura.getNumero())).append("</h2>");

        html.append("<div class='cliente-info'>");
        html.append("<strong>CLIENTE:</strong><br>");
        if (factura.getCliente() != null) {
            Cliente cliente = factura.getCliente();
            html.append("<strong>").append(escapeHtml(cliente.getNombre())).append("</strong><br>");
            html.append("CIF/NIF: ").append(escapeHtml(cliente.getCif())).append("<br>");
            if (cliente.getDireccion() != null) {
                html.append(escapeHtml(cliente.getDireccion())).append("<br>");
            }
            if (cliente.getPoblacion() != null) {
                html.append(escapeHtml(cliente.getCodigoPostal())).append(" - ")
                    .append(escapeHtml(cliente.getPoblacion())).append("<br>");
            }
        }
        html.append("</div>");

        html.append("<div class='section'>");
        html.append("<table style='width: 50%;'>");
        html.append("<tr><td><strong>Fecha:</strong></td><td>")
            .append(factura.getFecha() != null ? factura.getFecha().format(DATE_FORMATTER) : "")
            .append("</td></tr>");
        if (factura.getFechaVencimiento() != null) {
            html.append("<tr><td><strong>Vencimiento:</strong></td><td>")
                .append(factura.getFechaVencimiento().format(DATE_FORMATTER))
                .append("</td></tr>");
        }
        html.append("<tr><td><strong>Forma de Pago:</strong></td><td>")
            .append(escapeHtml(factura.getMedioCobro() != null ? factura.getMedioCobro() : "Contado"))
            .append("</td></tr>");
        html.append("</table>");
        html.append("</div>");

        html.append("<div class='section'>");
        html.append("<table>");
        html.append("<thead><tr>");
        html.append("<th>Articulo</th>");
        html.append("<th style='text-align: right;'>Cant.</th>");
        html.append("<th style='text-align: right;'>Precio</th>");
        html.append("<th style='text-align: right;'>Dto.%</th>");
        html.append("<th style='text-align: right;'>IVA%</th>");
        html.append("<th style='text-align: right;'>Total</th>");
        html.append("</tr></thead><tbody>");

        if (factura.getLineas() != null) {
            for (FacturaLinea linea : factura.getLineas()) {
                BigDecimal precioLinea = linea.getPrecio() != null ? linea.getPrecio() : BigDecimal.ZERO;
                BigDecimal cantidad = linea.getCantidad() != null ? linea.getCantidad() : BigDecimal.ZERO;
                BigDecimal descuento = linea.getDescuento() != null ? linea.getDescuento() : BigDecimal.ZERO;
                BigDecimal iva = linea.getIva() != null ? linea.getIva() : BigDecimal.ZERO;

                BigDecimal subtotal = precioLinea.multiply(cantidad);
                BigDecimal descuentoImporte = subtotal.multiply(descuento).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                BigDecimal baseImponible = subtotal.subtract(descuentoImporte);
                BigDecimal ivaImporte = baseImponible.multiply(iva).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                BigDecimal total = baseImponible.add(ivaImporte);

                html.append("<tr>");
                html.append("<td>");
                if (linea.getArticulo() != null) {
                    html.append(escapeHtml(linea.getArticulo().getNombre()));
                } else {
                    html.append(escapeHtml(linea.getDescripcion()));
                }
                html.append("</td>");
                html.append("<td style='text-align: right;'>").append(cantidad).append("</td>");
                html.append("<td style='text-align: right;'>").append(formatCurrency(precioLinea)).append("</td>");
                html.append("<td style='text-align: right;'>").append(descuento).append("%</td>");
                html.append("<td style='text-align: right;'>").append(iva).append("%</td>");
                html.append("<td style='text-align: right;'><strong>").append(formatCurrency(total)).append("</strong></td>");
                html.append("</tr>");
            }
        }

        html.append("</tbody></table>");
        html.append("</div>");

        html.append("<div class='totales'>");
        html.append("<p><strong>Base Imponible:</strong> ").append(formatCurrency(factura.getBaseImponible())).append("</p>");
        html.append("<p><strong>IVA:</strong> ").append(formatCurrency(factura.getIva())).append("</p>");
        if (factura.getRetencionIrpf() != null && factura.getRetencionIrpf().compareTo(BigDecimal.ZERO) > 0) {
            html.append("<p><strong>Retencion IRPF:</strong> -").append(formatCurrency(factura.getRetencionIrpf())).append("</p>");
        }
        html.append("<p class='total-final'><strong>TOTAL:</strong> ").append(formatCurrency(factura.getTotal())).append("</p>");
        html.append("</div>");

        if (Boolean.TRUE.equals(factura.getVerifactuEnviada()) || factura.getVerifactuHash() != null) {
            html.append("<div class='verifactu'>");
            html.append("<div class='verifactu-title'>VERI*FACTU</div>");

            if (Boolean.TRUE.equals(factura.getVerifactuEnviada())) {
                html.append("<p style='font-size: 10px; margin: 5px 0;'>Factura verificable en la sede electronica de la AEAT.</p>");
            } else {
                html.append("<p style='font-size: 10px; margin: 5px 0;'>Factura con huella y datos de trazabilidad generados por el sistema.</p>");
            }

            if (factura.getVerifactuQr() != null && !factura.getVerifactuQr().isBlank()) {
                html.append("<div class='qr-container'>");
                html.append("<img alt='QR VeriFactu' style='width:180px; height:180px;' src='data:image/png;base64,")
                    .append(factura.getVerifactuQr()).append("'/>");
                html.append("</div>");
            }

            html.append("<p style='font-size: 9px; word-break: break-all; margin: 5px 20px;'>")
                .append(escapeHtml(generarDatosVerificacion(factura))).append("</p>");

            if (factura.getVerifactuCsv() != null) {
                html.append("<p style='font-size: 10px;'><strong>CSV:</strong> ")
                    .append(escapeHtml(factura.getVerifactuCsv())).append("</p>");
            }

            html.append("</div>");
        }

        html.append("<div class='footer'>");
        html.append("<p>Factura generada electronicamente el ")
            .append(LocalDateTime.now().format(DATETIME_FORMATTER))
            .append("</p>");
        html.append("<p>Sistema ERP - Panaderia Tahona</p>");
        html.append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    private String generarDatosVerificacion(Factura factura) {
        StringBuilder datos = new StringBuilder();
        EmpresaConfig empresa = empresaConfigService.getConfiguracionActivaOrThrow();

        datos.append("NIF:").append(empresa.getCif()).append("|");
        datos.append("NUM:").append(factura.getNumero()).append("|");
        datos.append("FECHA:").append(factura.getFecha().format(DATE_FORMATTER)).append("|");
        datos.append("TOTAL:").append(String.format(Locale.ROOT, "%.2f", factura.getTotal())).append("|");

        if (factura.getVerifactuHash() != null && factura.getVerifactuHash().length() > 20) {
            datos.append("HUELLA:").append(factura.getVerifactuHash(), 0, 20).append("...|");
        } else if (factura.getVerifactuHash() != null) {
            datos.append("HUELLA:").append(factura.getVerifactuHash()).append("|");
        }

        datos.append("SISTEMA:VERIFACTU");
        return datos.toString();
    }

    private String generarHtmlAlbaran(AlbaranVenta albaran) {
        StringBuilder html = new StringBuilder();
        EmpresaConfig empresa = empresaConfigService.getConfiguracionActivaOrThrow();

        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; font-size: 12px; margin: 20px; }");
        html.append(".header { text-align: center; margin-bottom: 20px; border-bottom: 2px solid #000; padding-bottom: 10px; }");
        html.append(".empresa { font-size: 14px; font-weight: bold; }");
        html.append(".section { margin-top: 15px; }");
        html.append(".cliente-info { background: #f0f0f0; padding: 10px; margin: 10px 0; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-top: 10px; }");
        html.append("th { background: #333; color: white; padding: 8px; text-align: left; }");
        html.append("td { padding: 6px; border-bottom: 1px solid #ddd; }");
        html.append(".footer { margin-top: 30px; text-align: center; font-size: 10px; color: #666; }");
        html.append("</style>");
        html.append("</head><body>");

        html.append("<div class='header'>");
        html.append("<div class='empresa'>").append(escapeHtml(empresa.getNombreEmpresa())).append("</div>");
        html.append("<div>").append(escapeHtml(empresa.getCif())).append("</div>");
        html.append("<div>").append(escapeHtml(empresa.getDireccion())).append("</div>");
        html.append("</div>");

        html.append("<h2 style='text-align: center; color: #333;'>ALBARAN ")
            .append(escapeHtml(albaran.getNumero())).append("</h2>");

        html.append("<div class='cliente-info'>");
        html.append("<strong>CLIENTE:</strong><br>");
        if (albaran.getCliente() != null) {
            Cliente cliente = albaran.getCliente();
            html.append("<strong>").append(escapeHtml(cliente.getNombre())).append("</strong><br>");
            if (cliente.getDireccion() != null) {
                html.append(escapeHtml(cliente.getDireccion())).append("<br>");
            }
        }
        html.append("</div>");

        html.append("<div class='section'>");
        html.append("<p><strong>Fecha:</strong> ")
            .append(albaran.getFecha() != null ? albaran.getFecha().format(DATE_FORMATTER) : "")
            .append("</p>");
        html.append("</div>");

        html.append("<div class='section'>");
        html.append("<table>");
        html.append("<thead><tr>");
        html.append("<th>Articulo</th>");
        html.append("<th style='text-align: right;'>Cantidad</th>");
        html.append("</tr></thead><tbody>");

        if (albaran.getLineas() != null) {
            for (AlbaranVentaLinea linea : albaran.getLineas()) {
                html.append("<tr>");
                html.append("<td>");
                if (linea.getArticulo() != null) {
                    html.append(escapeHtml(linea.getArticulo().getNombre()));
                } else if (linea.getDescripcion() != null) {
                    html.append(escapeHtml(linea.getDescripcion()));
                }
                html.append("</td>");
                html.append("<td style='text-align: right;'>")
                    .append(linea.getCantidad() != null ? linea.getCantidad() : "")
                    .append("</td>");
                html.append("</tr>");
            }
        }

        html.append("</tbody></table>");
        html.append("</div>");

        html.append("<div class='footer'>");
        html.append("<p>Albaran generado el ")
            .append(LocalDateTime.now().format(DATETIME_FORMATTER))
            .append("</p>");
        html.append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    private void generarPdf(String html, File outputFile) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            HtmlConverter.convertToPdf(html, fos);
        }
    }

    private File buildOutputFile(String prefix, String numeroDocumento) {
        String safeNumber = numeroDocumento != null ? numeroDocumento.replace("/", "-") : "sin-numero";
        String fileName = String.format(
            "%s_%s_%s.pdf",
            prefix,
            safeNumber,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        );
        return new File(OUTPUT_DIR + fileName);
    }

    private void abrirPdf(String pdfPath) {
        try {
            File file = new File(pdfPath);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception e) {
            log.warn("No se pudo abrir automaticamente el PDF: {}", e.getMessage());
        }
    }

    private void registrarEventoDocumento(String tipoEvento, String referencia, File pdfFile) {
        java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put("rutaPdf", pdfFile.getAbsolutePath());
        metadata.put("directorio", new File(OUTPUT_DIR).getAbsolutePath());
        facturacionEventoService.registrarEvento(
            FacturacionEventoService.AMBITO_FACTURAS,
            tipoEvento,
            referencia,
            metadata
        );
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    private String formatCurrency(BigDecimal amount) {
        BigDecimal value = amount != null ? amount : BigDecimal.ZERO;
        return String.format(Locale.ROOT, "%.2f EUR", value);
    }
}
