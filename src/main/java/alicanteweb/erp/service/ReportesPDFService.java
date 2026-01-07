package alicanteweb.erp.service;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.AlbaranVenta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servicio para generación de reportes PDF avanzados
 * Facturas, albaranes, reportes fiscales, etc.
 */
@Service
public class ReportesPDFService {
    private static final Logger log = LoggerFactory.getLogger(ReportesPDFService.class);

    private static final String RUTA_REPORTES = "target/reportes/pdf/";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ReportesPDFService() {
        File dir = new File(RUTA_REPORTES);
        if (!dir.exists()) {
            dir.mkdirs();
            log.info("Directorio de reportes PDF creado: {}", RUTA_REPORTES);
        }
    }

    /**
     * Generar PDF de factura
     */
    public String generarPDFFactura(Factura factura) {
        log.info("Generando PDF de factura: {}", factura.getId());

        try {
            // Generar HTML primero
            String htmlFactura = generarHTMLFactura(factura);

            // Guardar como PDF (simulado - en producción usar iText o Apache PDFBox)
            String nombreArchivo = String.format("Factura_%s_%d.pdf",
                factura.getNumero().replace("/", "_"),
                System.currentTimeMillis());

            String ruta = RUTA_REPORTES + nombreArchivo;

            // En una aplicación real, aquí usaríamos una librería de PDF
            // Por ahora guardamos el HTML como referencia
            java.nio.file.Files.write(
                java.nio.file.Paths.get(ruta.replace(".pdf", ".html")),
                htmlFactura.getBytes("UTF-8")
            );

            log.info("PDF de factura generado: {}", ruta);
            return ruta;

        } catch (Exception e) {
            log.error("Error generando PDF de factura", e);
            throw new RuntimeException("Error al generar PDF", e);
        }
    }

    /**
     * Generar PDF de albarán
     */
    public String generarPDFAlbaran(AlbaranVenta albaran) {
        log.info("Generando PDF de albarán: {}", albaran.getId());

        try {
            String htmlAlbaran = generarHTMLAlbaran(albaran);

            String nombreArchivo = String.format("Albaran_%s_%d.pdf",
                albaran.getNumero().replace("/", "_"),
                System.currentTimeMillis());

            String ruta = RUTA_REPORTES + nombreArchivo;

            java.nio.file.Files.write(
                java.nio.file.Paths.get(ruta.replace(".pdf", ".html")),
                htmlAlbaran.getBytes("UTF-8")
            );

            log.info("PDF de albarán generado: {}", ruta);
            return ruta;

        } catch (Exception e) {
            log.error("Error generando PDF de albarán", e);
            throw new RuntimeException("Error al generar PDF", e);
        }
    }

    /**
     * Generar reporte resumen de facturas
     */
    public String generarReporteSummary(java.util.List<Factura> facturas) {
        try {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n");
            html.append("<title>Reporte de Facturas</title>\n");
            html.append("<style>");
            html.append("body { font-family: Arial; margin: 20px; }");
            html.append("table { border-collapse: collapse; width: 100%; }");
            html.append("th, td { border: 1px solid #ccc; padding: 10px; text-align: left; }");
            html.append("th { background-color: #f0f0f0; font-weight: bold; }");
            html.append(".total-row { font-weight: bold; background-color: #f9f9f9; }");
            html.append("</style>\n</head>\n<body>\n");

            html.append("<h1>Reporte de Facturas</h1>\n");
            html.append("<p>Generado: ").append(LocalDateTime.now().format(DATE_FORMAT)).append("</p>\n");

            html.append("<table>\n<thead>\n<tr>\n");
            html.append("<th>Número</th><th>Fecha</th><th>Cliente</th><th>Total</th><th>Estado</th>\n");
            html.append("</tr>\n</thead>\n<tbody>\n");

            java.math.BigDecimal totalGeneral = java.math.BigDecimal.ZERO;
            for (Factura factura : facturas) {
                html.append("<tr>\n");
                html.append("<td>").append(factura.getNumero()).append("</td>\n");
                html.append("<td>").append(factura.getFecha()).append("</td>\n");
                html.append("<td>").append(factura.getCliente() != null ? factura.getCliente().getNombre() : "").append("</td>\n");
                html.append("<td>").append(factura.getTotal()).append("€</td>\n");
                html.append("<td>").append(factura.getEstado()).append("</td>\n");
                html.append("</tr>\n");
                totalGeneral = totalGeneral.add(factura.getTotal());
            }

            html.append("<tr class=\"total-row\">\n");
            html.append("<td colspan=\"3\">TOTAL</td>\n");
            html.append("<td>").append(totalGeneral).append("€</td>\n");
            html.append("<td></td>\n");
            html.append("</tr>\n");

            html.append("</tbody>\n</table>\n");
            html.append("</body>\n</html>\n");

            String nombreArchivo = "ReporteFacturas_" + System.currentTimeMillis() + ".html";
            String ruta = RUTA_REPORTES + nombreArchivo;

            java.nio.file.Files.write(
                java.nio.file.Paths.get(ruta),
                html.toString().getBytes("UTF-8")
            );

            log.info("Reporte generado: {}", ruta);
            return ruta;

        } catch (Exception e) {
            log.error("Error generando reporte", e);
            throw new RuntimeException("Error al generar reporte", e);
        }
    }

    /**
     * Generar HTML de factura
     */
    private String generarHTMLFactura(Factura factura) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n");
        html.append("<title>FACTURA ").append(factura.getNumero()).append("</title>\n");
        html.append("<style>");
        html.append("body { font-family: Arial; padding: 20px; }");
        html.append(".encabezado { text-align: center; margin-bottom: 30px; }");
        html.append(".titulo { font-size: 24px; font-weight: bold; margin-bottom: 10px; }");
        html.append("table { border-collapse: collapse; width: 100%; margin-bottom: 20px; }");
        html.append("th, td { border: 1px solid #ccc; padding: 8px; text-align: left; }");
        html.append("th { background-color: #f0f0f0; }");
        html.append(".total { font-weight: bold; text-align: right; font-size: 16px; }");
        html.append("</style>\n</head>\n<body>\n");

        html.append("<div class=\"encabezado\">\n");
        html.append("<div class=\"titulo\">FACTURA</div>\n");
        html.append("<p>Número: ").append(factura.getNumero()).append("</p>\n");
        html.append("</div>\n");

        html.append("<table>\n<tr><td><strong>Fecha:</strong> ").append(factura.getFecha()).append("</td></tr>\n");
        html.append("<tr><td><strong>Cliente:</strong> ");
        if (factura.getCliente() != null) {
            html.append(factura.getCliente().getNombre()).append(" (").append(factura.getCliente().getCif()).append(")");
        }
        html.append("</td></tr>\n</table>\n");

        html.append("<table>\n<thead><tr><th>Concepto</th><th>Importe</th></tr></thead>\n<tbody>\n");
        html.append("<tr><td>Importe</td><td>").append(factura.getTotal()).append("€</td></tr>\n");
        html.append("</tbody>\n</table>\n");

        html.append("<div class=\"total\">TOTAL: ").append(factura.getTotal()).append("€</div>\n");
        html.append("<p style=\"font-size: 12px; margin-top: 30px;\">Documento generado: ")
            .append(LocalDateTime.now().format(DATE_FORMAT)).append("</p>\n");
        html.append("</body>\n</html>\n");

        return html.toString();
    }

    /**
     * Generar HTML de albarán
     */
    private String generarHTMLAlbaran(AlbaranVenta albaran) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n");
        html.append("<title>ALBARÁN ").append(albaran.getNumero()).append("</title>\n");
        html.append("<style>");
        html.append("body { font-family: Arial; padding: 20px; }");
        html.append(".encabezado { text-align: center; margin-bottom: 30px; }");
        html.append(".titulo { font-size: 24px; font-weight: bold; margin-bottom: 10px; }");
        html.append("table { border-collapse: collapse; width: 100%; margin-bottom: 20px; }");
        html.append("th, td { border: 1px solid #ccc; padding: 8px; text-align: left; }");
        html.append("</style>\n</head>\n<body>\n");

        html.append("<div class=\"encabezado\">\n");
        html.append("<div class=\"titulo\">ALBARÁN DE ENTREGA</div>\n");
        html.append("<p>Número: ").append(albaran.getNumero()).append("</p>\n");
        html.append("</div>\n");

        html.append("<table>\n<tr><td><strong>Fecha:</strong> ").append(albaran.getFecha()).append("</td></tr>\n");
        html.append("<tr><td><strong>Cliente:</strong> ");
        if (albaran.getCliente() != null) {
            html.append(albaran.getCliente().getNombre());
        }
        html.append("</td></tr>\n</table>\n");

        if (albaran.getObservaciones() != null) {
            html.append("<p><strong>Observaciones:</strong> ").append(albaran.getObservaciones()).append("</p>\n");
        }

        html.append("<p style=\"font-size: 12px; margin-top: 30px;\">Documento generado: ")
            .append(LocalDateTime.now().format(DATE_FORMAT)).append("</p>\n");
        html.append("</body>\n</html>\n");

        return html.toString();
    }
}

