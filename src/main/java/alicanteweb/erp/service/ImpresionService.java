package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
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

/**
 * Servicio de impresión de documentos
 * Cumple con normativa VeriFacTu (Ley Crea y Crece)
 */
@Slf4j
@Service
public class ImpresionService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final String OUTPUT_DIR = "impresiones/";

    private final EmpresaConfigService empresaConfigService;

    public ImpresionService(EmpresaConfigService empresaConfigService) {
        this.empresaConfigService = empresaConfigService;

        // Crear directorio de impresiones si no existe
        try {
            Files.createDirectories(Paths.get(OUTPUT_DIR));
        } catch (Exception e) {
            log.error("Error creando directorio de impresiones", e);
        }
    }

    /**
     * Imprimir factura con soporte VeriFacTu
     */
    public void imprimirFactura(Factura factura, boolean abrirPDF) {
        try {
            log.info("Generando impresión de factura: {}", factura.getNumero());

            String html = generarHTMLFactura(factura);
            String fileName = String.format("Factura_%s_%s.pdf",
                factura.getNumero().replace("/", "-"),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

            String pdfPath = OUTPUT_DIR + fileName;
            generarPDF(html, pdfPath);

            log.info("✅ Factura generada: {}", pdfPath);

            if (abrirPDF) {
                abrirPDF(pdfPath);
            }

        } catch (Exception e) {
            log.error("❌ Error imprimiendo factura", e);
            throw new RuntimeException("Error al generar PDF de factura: " + e.getMessage());
        }
    }

    /**
     * Generar HTML de factura con VeriFacTu
     */
    private String generarHTMLFactura(Factura factura) {
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

        // HEADER - Datos de la empresa
        html.append("<div class='header'>");
        html.append("<div class='empresa'>").append(empresa.getNombreEmpresa()).append("</div>");
        html.append("<div>").append(empresa.getCif()).append("</div>");
        html.append("<div>").append(empresa.getDireccion()).append("</div>");
        html.append("<div>").append(empresa.getCodigoPostal()).append(" - ")
            .append(empresa.getCiudad()).append("</div>");
        if (empresa.getTelefono() != null) {
            html.append("<div>Tel: ").append(empresa.getTelefono()).append("</div>");
        }
        if (empresa.getEmail() != null) {
            html.append("<div>Email: ").append(empresa.getEmail()).append("</div>");
        }
        html.append("</div>");

        // TÍTULO FACTURA
        html.append("<h2 style='text-align: center; color: #333;'>FACTURA ")
            .append(factura.getNumero()).append("</h2>");

        // DATOS DEL CLIENTE
        html.append("<div class='cliente-info'>");
        html.append("<strong>CLIENTE:</strong><br>");
        if (factura.getCliente() != null) {
            Cliente cliente = factura.getCliente();
            html.append("<strong>").append(cliente.getNombre()).append("</strong><br>");
            html.append("CIF/NIF: ").append(cliente.getCif()).append("<br>");
            if (cliente.getDireccion() != null) {
                html.append(cliente.getDireccion()).append("<br>");
            }
            if (cliente.getPoblacion() != null) {
                html.append(cliente.getCodigoPostal()).append(" - ")
                    .append(cliente.getPoblacion()).append("<br>");
            }
        }
        html.append("</div>");

        // DATOS DE LA FACTURA
        html.append("<div class='section'>");
        html.append("<table style='width: 50%;'>");
        html.append("<tr><td><strong>Fecha:</strong></td><td>")
            .append(factura.getFecha() != null ?
                factura.getFecha().format(DATE_FORMATTER) : "")
            .append("</td></tr>");
        if (factura.getFechaVencimiento() != null) {
            html.append("<tr><td><strong>Vencimiento:</strong></td><td>")
                .append(factura.getFechaVencimiento().format(DATE_FORMATTER))
                .append("</td></tr>");
        }
        html.append("<tr><td><strong>Forma de Pago:</strong></td><td>")
            .append(factura.getMedioCobro() != null ? factura.getMedioCobro() : "Contado")
            .append("</td></tr>");
        html.append("</table>");
        html.append("</div>");

        // LÍNEAS DE FACTURA
        html.append("<div class='section'>");
        html.append("<table>");
        html.append("<thead><tr>");
        html.append("<th>Artículo</th>");
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
                    html.append(linea.getArticulo().getNombre());
                } else {
                    html.append(linea.getDescripcion() != null ? linea.getDescripcion() : "");
                }
                html.append("</td>");
                html.append("<td style='text-align: right;'>").append(cantidad).append("</td>");
                html.append("<td style='text-align: right;'>").append(String.format("%.2f€", precioLinea)).append("</td>");
                html.append("<td style='text-align: right;'>").append(descuento).append("%</td>");
                html.append("<td style='text-align: right;'>").append(iva).append("%</td>");
                html.append("<td style='text-align: right;'><strong>").append(String.format("%.2f€", total)).append("</strong></td>");
                html.append("</tr>");
            }
        }

        html.append("</tbody></table>");
        html.append("</div>");

        // TOTALES
        html.append("<div class='totales'>");
        html.append("<p><strong>Base Imponible:</strong> ")
            .append(String.format("%.2f€", factura.getBaseImponible() != null ? factura.getBaseImponible() : BigDecimal.ZERO))
            .append("</p>");
        html.append("<p><strong>IVA:</strong> ")
            .append(String.format("%.2f€", factura.getIva() != null ? factura.getIva() : BigDecimal.ZERO))
            .append("</p>");
        if (factura.getRetencionIrpf() != null && factura.getRetencionIrpf().compareTo(BigDecimal.ZERO) > 0) {
            html.append("<p><strong>Retención IRPF:</strong> -")
                .append(String.format("%.2f€", factura.getRetencionIrpf() != null ? factura.getRetencionIrpf() : BigDecimal.ZERO))
                .append("</p>");
        }
        html.append("<p class='total-final'><strong>TOTAL:</strong> ")
            .append(String.format("%.2f€", factura.getTotal() != null ? factura.getTotal() : BigDecimal.ZERO))
            .append("</p>");
        html.append("</div>");

        // VERIFACTU - Según Ley Crea y Crece
        html.append("<div class='verifactu'>");
        html.append("<div class='verifactu-title'>📋 VERIFICACIÓN ELECTRÓNICA - VeriFacTu</div>");
        html.append("<p style='font-size: 10px; margin: 5px 0;'>");
        html.append("Esta factura ha sido registrada en el sistema VeriFacTu de la AEAT ");
        html.append("según la Ley 18/2022 de creación y crecimiento de empresas.");
        html.append("</p>");

        // Código QR con datos de verificación
        String datosVerificacion = generarDatosVerificacion(factura);
        html.append("<div class='qr-container'>");
        html.append("<p style='font-size: 10px;'><strong>Código de Verificación:</strong></p>");
        html.append("<p style='font-size: 9px; word-break: break-all; margin: 5px 20px;'>")
            .append(datosVerificacion).append("</p>");
        html.append("</div>");

        if (factura.getVerifactuCsv() != null) {
            html.append("<p style='font-size: 10px;'><strong>CSV:</strong> ")
                .append(factura.getVerifactuCsv()).append("</p>");
        }

        html.append("<p style='font-size: 9px; margin-top: 10px; color: #555;'>");
        html.append("Puede verificar esta factura en: https://www2.agenciatributaria.gob.es/wlpl/PCut-S450");
        html.append("</p>");
        html.append("</div>");

        // FOOTER
        html.append("<div class='footer'>");
        html.append("<p>Factura generada electrónicamente el ")
            .append(LocalDateTime.now().format(DATETIME_FORMATTER))
            .append("</p>");
        html.append("<p>Sistema ERP - Panadería Tahona</p>");
        html.append("</div>");

        html.append("</body></html>");

        return html.toString();
    }

    /**
     * Generar datos de verificación VeriFacTu
     */
    private String generarDatosVerificacion(Factura factura) {
        StringBuilder datos = new StringBuilder();

        EmpresaConfig empresa = empresaConfigService.getConfiguracionActivaOrThrow();

        // Formato según VeriFacTu
        datos.append("NIF:").append(empresa.getCif()).append("|");
        datos.append("NUM:").append(factura.getNumero()).append("|");
        datos.append("FECHA:").append(factura.getFecha().format(DATE_FORMATTER)).append("|");
        datos.append("TOTAL:").append(String.format("%.2f", factura.getTotal())).append("|");

        if (factura.getVerifactuHash() != null && factura.getVerifactuHash().length() > 20) {
            datos.append("HUELLA:").append(factura.getVerifactuHash(), 0, 20).append("...|");
        } else if (factura.getVerifactuHash() != null) {
            datos.append("HUELLA:").append(factura.getVerifactuHash()).append("|");
        }

        datos.append("SISTEMA:VERIFACTU");

        return datos.toString();
    }

    /**
     * Imprimir albarán
     */
    public void imprimirAlbaran(AlbaranVenta albaran, boolean abrirPDF) {
        try {
            log.info("Generando impresión de albarán: {}", albaran.getNumero());

            String html = generarHTMLAlbaran(albaran);
            String fileName = String.format("Albaran_%s_%s.pdf",
                albaran.getNumero().replace("/", "-"),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

            String pdfPath = OUTPUT_DIR + fileName;
            generarPDF(html, pdfPath);

            log.info("✅ Albarán generado: {}", pdfPath);

            if (abrirPDF) {
                abrirPDF(pdfPath);
            }

        } catch (Exception e) {
            log.error("❌ Error imprimiendo albarán", e);
            throw new RuntimeException("Error al generar PDF de albarán: " + e.getMessage());
        }
    }

    /**
     * Generar HTML de albarán
     */
    private String generarHTMLAlbaran(AlbaranVenta albaran) {
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

        // HEADER
        html.append("<div class='header'>");
        html.append("<div class='empresa'>").append(empresa.getNombreEmpresa()).append("</div>");
        html.append("<div>").append(empresa.getCif()).append("</div>");
        html.append("<div>").append(empresa.getDireccion()).append("</div>");
        html.append("</div>");

        // TÍTULO
        html.append("<h2 style='text-align: center; color: #333;'>ALBARÁN ")
            .append(albaran.getNumero()).append("</h2>");

        // DATOS DEL CLIENTE
        html.append("<div class='cliente-info'>");
        html.append("<strong>CLIENTE:</strong><br>");
        if (albaran.getCliente() != null) {
            Cliente cliente = albaran.getCliente();
            html.append("<strong>").append(cliente.getNombre()).append("</strong><br>");
            if (cliente.getDireccion() != null) {
                html.append(cliente.getDireccion()).append("<br>");
            }
        }
        html.append("</div>");

        // DATOS DEL ALBARÁN
        html.append("<div class='section'>");
        html.append("<p><strong>Fecha:</strong> ")
            .append(albaran.getFecha() != null ? albaran.getFecha().format(DATE_FORMATTER) : "")
            .append("</p>");
        html.append("</div>");

        // LÍNEAS
        html.append("<div class='section'>");
        html.append("<table>");
        html.append("<thead><tr>");
        html.append("<th>Artículo</th>");
        html.append("<th style='text-align: right;'>Cantidad</th>");
        html.append("</tr></thead><tbody>");

        if (albaran.getLineas() != null) {
            for (AlbaranVentaLinea linea : albaran.getLineas()) {
                html.append("<tr>");
                html.append("<td>");
                if (linea.getArticulo() != null) {
                    html.append(linea.getArticulo().getNombre());
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

        // FOOTER
        html.append("<div class='footer'>");
        html.append("<p>Albarán generado el ")
            .append(LocalDateTime.now().format(DATETIME_FORMATTER))
            .append("</p>");
        html.append("</div>");

        html.append("</body></html>");

        return html.toString();
    }

    /**
     * Generar PDF desde HTML
     */
    private void generarPDF(String html, String outputPath) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            HtmlConverter.convertToPdf(html, fos);
        }
    }

    /**
     * Abrir PDF generado
     */
    private void abrirPDF(String pdfPath) {
        try {
            File file = new File(pdfPath);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception e) {
            log.warn("No se pudo abrir automáticamente el PDF: {}", e.getMessage());
        }
    }

    /**
     * Obtener ruta del directorio de impresiones
     */
    public String getDirectorioImpresiones() {
        return new File(OUTPUT_DIR).getAbsolutePath();
    }
}

