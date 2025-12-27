package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servicio de impresión para facturas y albaranes
 * Genera documentos en formato HTML optimizados para impresión en blanco y negro
 */
@Service
public class PrintService {
    private static final Logger log = LoggerFactory.getLogger(PrintService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public enum PrintDesign {
        CLASICO("Clásico", "Diseño tradicional con bordes"),
        MODERNO("Moderno", "Diseño minimalista sin bordes"),
        COMPACTO("Compacto", "Diseño compacto para ahorrar papel"),
        RECIBO_PANADERIA("Recibo Panadería", "Formato tipo ticket de panadería");

        private final String nombre;
        private final String descripcion;

        PrintDesign(String nombre, String descripcion) {
            this.nombre = nombre;
            this.descripcion = descripcion;
        }

        public String getNombre() { return nombre; }
        public String getDescripcion() { return descripcion; }
    }

    /**
     * Genera un archivo HTML para imprimir una factura
     */
    public File generarImpresionFactura(Factura factura, List<FacturaLinea> lineas, PrintDesign design) throws IOException {
        log.info("Generando impresión de factura {} con diseño {}", factura.getNumero(), design.name());

        StringBuilder html = new StringBuilder();
        html.append(getHtmlHeader("Factura " + factura.getNumero(), design));

        // Contenido según diseño
        switch (design) {
            case CLASICO:
                html.append(generarFacturaClasica(factura, lineas));
                break;
            case MODERNO:
                html.append(generarFacturaModerna(factura, lineas));
                break;
            case COMPACTO:
                html.append(generarFacturaCompacta(factura, lineas));
                break;
        }

        html.append(getHtmlFooter());

        // Guardar archivo temporal
        File tempFile = File.createTempFile("factura_" + factura.getNumero() + "_", ".html");
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
            writer.write(html.toString());
        }

        log.info("Archivo de impresión generado: {}", tempFile.getAbsolutePath());
        return tempFile;
    }

    /**
     * Genera un archivo HTML para imprimir un albarán
     */
    public File generarImpresionAlbaran(AlbaranVenta albaran, List<AlbaranVentaLinea> lineas, PrintDesign design) throws IOException {
        log.info("Generando impresión de albarán {} con diseño {}", albaran.getNumero(), design.name());

        StringBuilder html = new StringBuilder();
        html.append(getHtmlHeader("Albarán " + albaran.getNumero(), design));

        // Contenido según diseño
        switch (design) {
            case CLASICO:
                html.append(generarAlbaranClasico(albaran, lineas));
                break;
            case MODERNO:
                html.append(generarAlbaranModerno(albaran, lineas));
                break;
            case COMPACTO:
                html.append(generarAlbaranCompacto(albaran, lineas));
                break;
            case RECIBO_PANADERIA:
                html.append(generarAlbaranReciboPanaderia(albaran, lineas));
                break;
        }

        html.append(getHtmlFooter());

        // Guardar archivo temporal
        File tempFile = File.createTempFile("albaran_" + albaran.getNumero() + "_", ".html");
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
            writer.write(html.toString());
        }

        log.info("Archivo de impresión generado: {}", tempFile.getAbsolutePath());
        return tempFile;
    }

    private String getHtmlHeader(String titulo, PrintDesign design) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>%s</title>
                <style>
                    @media print {
                        body { margin: 0; }
                        .no-print { display: none; }
                        @page { margin: 1cm; }
                    }
                    
                    body {
                        font-family: Arial, 'Courier New', 'DejaVu Sans', sans-serif;
                        font-size: 11pt;
                        color: #000;
                        background: #fff;
                        margin: 20px;
                    }
                    
                    .container {
                        max-width: 800px;
                        margin: 0 auto;
                    }
                    
                    .header {
                        text-align: center;
                        margin-bottom: 20px;
                        %s
                    }
                    
                    .company-name {
                        font-size: 18pt;
                        font-weight: bold;
                        margin-bottom: 5px;
                    }
                    
                    .doc-title {
                        font-size: 16pt;
                        font-weight: bold;
                        margin: 10px 0;
                    }
                    
                    .info-section {
                        margin: 15px 0;
                        %s
                    }
                    
                    .info-row {
                        display: flex;
                        justify-content: space-between;
                        margin: 5px 0;
                    }
                    
                    .label {
                        font-weight: bold;
                        min-width: 100px;
                    }
                    
                    table {
                        width: 100%%;
                        border-collapse: collapse;
                        margin: 20px 0;
                    }
                    
                    th, td {
                        padding: 8px;
                        text-align: left;
                        %s
                    }
                    
                    th {
                        background-color: #f0f0f0;
                        font-weight: bold;
                    }
                    
                    .text-right {
                        text-align: right;
                    }
                    
                    .totals {
                        margin-top: 20px;
                        float: right;
                        min-width: 300px;
                    }
                    
                    .total-row {
                        display: flex;
                        justify-content: space-between;
                        padding: 5px;
                        %s
                    }
                    
                    .total-final {
                        font-size: 14pt;
                        font-weight: bold;
                        border-top: 2px solid #000;
                        margin-top: 5px;
                        padding-top: 10px;
                    }
                    
                    .footer {
                        margin-top: 40px;
                        text-align: center;
                        font-size: 9pt;
                        color: #666;
                        clear: both;
                    }
                    
                    .print-button {
                        margin: 20px auto;
                        padding: 10px 30px;
                        font-size: 14pt;
                        background: #4CAF50;
                        color: white;
                        border: none;
                        cursor: pointer;
                        display: block;
                    }
                    
                    .print-button:hover {
                        background: #45a049;
                    }
                </style>
            </head>
            <body>
                <button class="print-button no-print" onclick="window.print()">&#x1F5A8; Imprimir</button>
                <div class="container">
            """.formatted(
                titulo,
                design == PrintDesign.CLASICO ? "border: 2px solid #000; padding: 15px;" : "",
                design == PrintDesign.CLASICO ? "border: 1px solid #000; padding: 10px;" : "",
                design == PrintDesign.CLASICO ? "border: 1px solid #000;" : "border-bottom: 1px solid #ddd;",
                design == PrintDesign.CLASICO ? "border-top: 1px solid #000;" : ""
            );
    }

    private String getHtmlFooter() {
        return """
                </div>
            </body>
            </html>
            """;
    }

    private String generarFacturaClasica(Factura factura, List<FacturaLinea> lineas) {
        StringBuilder html = new StringBuilder();

        html.append("<div class='header'>");
        html.append("<div class='company-name'>PANADERÃA TAHONA</div>");
        html.append("<div>NIF: B12345678 | Tel: 123 456 789</div>");
        html.append("<div>Calle Principal, 123 - 03001 Alicante</div>");
        html.append("<div class='doc-title'>FACTURA</div>");
        html.append("</div>");

        html.append("<div class='info-section'>");
        html.append("<div class='info-row'><span class='label'>Número:</span><span>").append(factura.getNumero()).append("</span></div>");
        html.append("<div class='info-row'><span class='label'>Fecha:</span><span>")
            .append(factura.getFecha() != null ? factura.getFecha().format(DATE_FORMATTER) : "").append("</span></div>");
        if (factura.getCliente() != null) {
            html.append("<div class='info-row'><span class='label'>Cliente:</span><span>").append(factura.getCliente().getNombre()).append("</span></div>");
            if (factura.getCliente().getCif() != null) {
                html.append("<div class='info-row'><span class='label'>CIF:</span><span>").append(factura.getCliente().getCif()).append("</span></div>");
            }
        }
        html.append("</div>");

        html.append(generarTablaLineasFactura(lineas));
        html.append(generarTotalesFactura(factura, lineas));

        html.append("<div class='footer'>");
        html.append("Gracias por su confianza | www.panaderiatahona.com");
        html.append("</div>");

        return html.toString();
    }

    private String generarFacturaModerna(Factura factura, List<FacturaLinea> lineas) {
        // Similar pero sin bordes, más espacios en blanco
        return generarFacturaClasica(factura, lineas).replace("border:", "");
    }

    private String generarFacturaCompacta(Factura factura, List<FacturaLinea> lineas) {
        // Versión más compacta con menos espacios
        return generarFacturaClasica(factura, lineas)
            .replace("margin: 20px", "margin: 10px")
            .replace("padding: 15px", "padding: 5px");
    }

    private String generarTablaLineasFactura(List<FacturaLinea> lineas) {
        StringBuilder html = new StringBuilder();
        html.append("<table>");
        html.append("<thead><tr>");
        html.append("<th>Descripción</th>");
        html.append("<th class='text-right'>Cant.</th>");
        html.append("<th class='text-right'>Precio</th>");
        html.append("<th class='text-right'>IVA %</th>");
        html.append("<th class='text-right'>Total</th>");
        html.append("</tr></thead>");
        html.append("<tbody>");

        for (FacturaLinea linea : lineas) {
            html.append("<tr>");
            html.append("<td>").append(linea.getArticulo() != null ? linea.getArticulo().getDescripcion() : "").append("</td>");
            html.append("<td class='text-right'>").append(formatDecimal(linea.getCantidad())).append("</td>");
            html.append("<td class='text-right'>").append(formatMoney(linea.getPrecio())).append("</td>");
            html.append("<td class='text-right'>").append(formatDecimal(linea.getIva())).append("</td>");

            BigDecimal total = calcularTotalLinea(linea.getCantidad(), linea.getPrecio(), linea.getIva());
            html.append("<td class='text-right'>").append(formatMoney(total)).append("</td>");
            html.append("</tr>");
        }

        html.append("</tbody>");
        html.append("</table>");
        return html.toString();
    }

    private String generarTotalesFactura(Factura factura, List<FacturaLinea> lineas) {
        BigDecimal base = BigDecimal.ZERO;
        BigDecimal totalIva = BigDecimal.ZERO;

        for (FacturaLinea linea : lineas) {
            BigDecimal subtotal = linea.getCantidad().multiply(linea.getPrecio());
            base = base.add(subtotal);
            BigDecimal iva = subtotal.multiply(linea.getIva()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            totalIva = totalIva.add(iva);
        }

        StringBuilder html = new StringBuilder();
        html.append("<div class='totals'>");
        html.append("<div class='total-row'><span>Base Imponible:</span><span>").append(formatMoney(base)).append("</span></div>");
        html.append("<div class='total-row'><span>IVA:</span><span>").append(formatMoney(totalIva)).append("</span></div>");
        html.append("<div class='total-row total-final'><span>TOTAL:</span><span>").append(formatMoney(factura.getTotal())).append("</span></div>");
        html.append("</div>");
        return html.toString();
    }

    // Métodos similares para albaranes
    private String generarAlbaranClasico(AlbaranVenta albaran, List<AlbaranVentaLinea> lineas) {
        return generarFacturaClasica(
            convertirAlbaranAFactura(albaran),
            convertirLineasAlbaranAFactura(lineas)
        ).replace("FACTURA", "ALBARÃN DE ENTREGA");
    }

    private String generarAlbaranModerno(AlbaranVenta albaran, List<AlbaranVentaLinea> lineas) {
        return generarAlbaranClasico(albaran, lineas).replace("border:", "");
    }

    private String generarAlbaranCompacto(AlbaranVenta albaran, List<AlbaranVentaLinea> lineas) {
        return generarAlbaranClasico(albaran, lineas)
            .replace("margin: 20px", "margin: 10px")
            .replace("padding: 15px", "padding: 5px");
    }

    // Métodos auxiliares
    private Factura convertirAlbaranAFactura(AlbaranVenta albaran) {
        Factura f = new Factura();
        f.setNumero(albaran.getNumero());
        f.setFecha(albaran.getFecha());
        f.setCliente(albaran.getCliente());
        f.setTotal(albaran.getTotal());
        return f;
    }

    private List<FacturaLinea> convertirLineasAlbaranAFactura(List<AlbaranVentaLinea> lineasAlbaran) {
        return lineasAlbaran.stream().map(la -> {
            FacturaLinea fl = new FacturaLinea();
            fl.setArticulo(la.getArticulo());
            fl.setCantidad(la.getCantidad());
            fl.setPrecio(la.getPrecio());
            fl.setIva(la.getIva());
            return fl;
        }).toList();
    }

    private BigDecimal calcularTotalLinea(BigDecimal cantidad, BigDecimal precio, BigDecimal iva) {
        if (cantidad == null || precio == null) return BigDecimal.ZERO;
        BigDecimal subtotal = cantidad.multiply(precio);
        if (iva != null) {
            BigDecimal importeIva = subtotal.multiply(iva).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(importeIva);
        }
        return subtotal.setScale(2, RoundingMode.HALF_UP);
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) return "0.00 &euro;";
        return String.format("%.2f &euro;", value);
    }

    private String formatDecimal(BigDecimal value) {
        if (value == null) return "0";
        return value.stripTrailingZeros().toPlainString();
    }

    /**
     * Genera un albarán en formato tipo recibo de panadería
     * Formato exacto según imagen proporcionada del cliente
     */
    private String generarAlbaranReciboPanaderia(AlbaranVenta albaran, List<AlbaranVentaLinea> lineas) {
        StringBuilder html = new StringBuilder();

        // Estilo base: Courier New, tamño reducido, sin colores
        html.append("<div style='font-family: \"Courier New\", monospace; font-size: 9pt; color: #000; max-width: 800px; margin: 0 auto; padding: 20px;'>");

        // ENCABEZADO: Empresa (izquierda) y Datos fiscales (derecha)
        html.append("<table style='width:100%; border:none; border-collapse:collapse; margin-bottom:5px;'><tr>");

        // IZQUIERDA: Datos de empresa
        html.append("<td style='width:50%; vertical-align:top; border:none; padding:0;'>");
        html.append("<div style='font-size:10pt; margin-bottom:2px;'><strong>GRUPO BABO, S.Coop.V.L.</strong></div>");
        html.append("<div style='font-size:9pt;'>Armada Espñola, P.2 NÂº213</div>");
        html.append("<div style='font-size:9pt;'>03195 El Altet - ELCHE</div>");
        html.append("</td>");

        // CENTRO: Datos fiscales
        html.append("<td style='width:25%; vertical-align:top; text-align:center; border:none; padding:0;'>");
        html.append("<div style='font-size:9pt;'>CIF F54059985</div>");
        html.append("<div style='font-size:9pt;'>R.G.S. EM-20.05033/A</div>");
        html.append("<div style='font-size:9pt;'>Tel. 965 68 73 58</div>");
        html.append("</td>");

        // DERECHA: Logo / Nombre comercial
        html.append("<td style='width:25%; vertical-align:top; text-align:right; border:none; padding:0;'>");
        html.append("<div style='font-size:13pt; font-weight:bold; letter-spacing:1px;'>LA TAHONA</div>");
        html.append("<div style='font-size:11pt; font-weight:bold; letter-spacing:1px;'>EL ALTET</div>");
        html.append("</td>");

        html.append("</tr></table>");

        // Línea divisoria
        html.append("<hr style='border:none; border-top:1px solid #000; margin:8px 0;'/>");

        // SECCIÃ“N INTERMEDIA: Orden/Cliente (izquierda) y Recuadro Fecha/Albarán (derecha)
        html.append("<table style='width:100%; border:none; border-collapse:collapse; margin-bottom:10px;'><tr>");

        // IZQUIERDA: Número de orden, cliente y local
        html.append("<td style='width:65%; vertical-align:top; border:none; padding:0;'>");

        // Número de orden interno (008)
        html.append("<div style='margin-left:60px; margin-bottom:8px; font-size:9pt;'>");
        html.append(String.format("%03d", albaran.getId() != null ? albaran.getId() % 1000 : 0));
        html.append("</div>");

        // Nombre del cliente
        if (albaran.getCliente() != null) {
            html.append("<div style='font-size:10pt; font-weight:bold; margin-bottom:8px;'>");
            html.append(albaran.getCliente().getNombre());
            html.append("</div>");
        }

        // Almacén/Local
        if (albaran.getAlmacen() != null) {
            html.append("<div style='font-size:9pt; margin-bottom:5px;'>");
            html.append(albaran.getAlmacen().getNombre());
            html.append("</div>");
        }

        html.append("</td>");

        // DERECHA: Recuadro con fecha y número de albarán
        html.append("<td style='width:35%; vertical-align:top; border:none; padding:0; text-align:right;'>");
        html.append("<div style='border:2px solid #000; display:inline-block; padding:8px 15px; text-align:center;'>");
        html.append("<div style='font-size:8pt; margin-bottom:3px;'><em>FECHA/LOTE</em></div>");
        html.append("<div style='font-size:10pt; font-weight:bold; margin-bottom:8px;'>");
        html.append(albaran.getFecha() != null ? albaran.getFecha().format(DATE_FORMATTER) : "");
        html.append("</div>");
        html.append("<div style='font-size:8pt; margin-bottom:3px;'><em>NÂº ALBARAN</em></div>");
        html.append("<div style='font-size:11pt; font-weight:bold;'>");
        html.append(albaran.getNumero() != null ? albaran.getNumero() : "");
        html.append("</div>");
        html.append("</div>");
        html.append("</td>");

        html.append("</tr></table>");

        // Línea divisoria antes de productos
        html.append("<hr style='border:none; border-top:1px solid #000; margin:10px 0;'/>");

        // TABLA DE PRODUCTOS sin bordes visibles
        html.append("<table style='width:100%; border:none; border-collapse:collapse; margin-top:5px;'>");

        // Cabecera de tabla
        html.append("<thead><tr>");
        html.append("<th style='text-align:center; width:12%; padding:3px 5px; border:none; font-size:8pt; font-weight:normal;'><em>CANTIDAD</em></th>");
        html.append("<th style='text-align:left; width:48%; padding:3px 5px; border:none; font-size:8pt; font-weight:normal;'><em>DESCRIPCION / ARTICULO</em></th>");
        html.append("<th style='text-align:right; width:20%; padding:3px 5px; border:none; font-size:8pt; font-weight:normal;'><em>PRECIO</em></th>");
        html.append("<th style='text-align:right; width:20%; padding:3px 5px; border:none; font-size:8pt; font-weight:normal;'><em>IMPORTE</em></th>");
        html.append("</tr></thead>");

        html.append("<tbody>");

        // Líneas de productos
        for (AlbaranVentaLinea linea : lineas) {
            BigDecimal cantidad = linea.getCantidad() != null ? linea.getCantidad() : BigDecimal.ZERO;
            BigDecimal precio = linea.getPrecio() != null ? linea.getPrecio() : BigDecimal.ZERO;
            BigDecimal importe = cantidad.multiply(precio).setScale(2, RoundingMode.HALF_UP);

            html.append("<tr>");

            // Cantidad (centrada)
            html.append("<td style='text-align:center; padding:4px 5px; border:none; font-size:9pt;'>");
            html.append(formatDecimal(cantidad));
            html.append("</td>");

            // Descripción (izquierda)
            html.append("<td style='text-align:left; padding:4px 5px; border:none; font-size:9pt;'>");
            html.append(linea.getArticulo() != null ? linea.getArticulo().getDescripcion().toUpperCase() : "");
            html.append("</td>");

            // Precio (derecha, sin símbolo â‚¬)
            html.append("<td style='text-align:right; padding:4px 5px; border:none; font-size:9pt;'>");
            html.append(formatNumberOnly(precio));
            html.append("</td>");

            // Importe (derecha, sin símbolo â‚¬)
            html.append("<td style='text-align:right; padding:4px 5px; border:none; font-size:9pt;'>");
            html.append(formatNumberOnly(importe));
            html.append("</td>");

            html.append("</tr>");
        }

        html.append("</tbody>");
        html.append("</table>");

        // Observaciones (si existen)
        if (albaran.getObservaciones() != null && !albaran.getObservaciones().isBlank()) {
            html.append("<div style='margin-top:20px; font-size:8pt;'>");
            html.append("<strong>Observaciones:</strong> ");
            html.append(albaran.getObservaciones());
            html.append("</div>");
        }

        html.append("</div>");

        return html.toString();
    }

    /**
     * Formatea un número sin símbolo de moneda (para el formato de albarán)
     */
    private String formatNumberOnly(BigDecimal value) {
        if (value == null) return "0.00";
        return String.format("%.2f", value);
    }
}


