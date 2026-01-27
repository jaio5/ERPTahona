package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.EmpresaConfigRepository;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Optional;


/**
 * Servicio para generación de reportes PDF avanzados con iText 7
 * Facturas, albaranes, presupuestos, listados, etc.
 */
@Service
@RequiredArgsConstructor
public class ReportesPDFService {
    private static final Logger log = LoggerFactory.getLogger(ReportesPDFService.class);

    private static final String RUTA_REPORTES = "target/reportes/pdf/";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final EmpresaConfigRepository empresaConfigRepository;

    // Constructor que crea el directorio de reportes
    {
        File dir = new File(RUTA_REPORTES);
        if (!dir.exists()) {
            dir.mkdirs();
            log.info("📁 Directorio de reportes PDF creado: {}", RUTA_REPORTES);
        }
    }

    // ==========================================
    // FACTURAS
    // ==========================================

    /**
     * Generar PDF de factura completo
     */
    public File generarPDFFactura(Factura factura) {
        log.info("📄 Generando PDF de factura: {}", factura.getNumero());

        try {
            String html = generarHTMLFactura(factura);
            String nombreArchivo = String.format("Factura_%s_%d.pdf",
                factura.getNumero().replace("/", "-"),
                System.currentTimeMillis());

            File pdfFile = new File(RUTA_REPORTES + nombreArchivo);
            convertirHTMLaPDF(html, pdfFile);

            log.info("✅ PDF de factura generado: {}", pdfFile.getAbsolutePath());
            return pdfFile;

        } catch (Exception e) {
            log.error("❌ Error generando PDF de factura", e);
            throw new RuntimeException("Error al generar PDF de factura", e);
        }
    }

    /**
     * Generar HTML de factura
     */
    private String generarHTMLFactura(Factura factura) {
        EmpresaConfig empresa = obtenerDatosEmpresa();

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html><head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<title>Factura ").append(factura.getNumero()).append("</title>\n");
        html.append(obtenerEstilosCSS());
        html.append("</head><body>\n");

        // Encabezado empresa
        html.append("<div class='cabecera'>\n");
        html.append("<div class='empresa'>\n");
        html.append("<h1>").append(empresa.getNombreEmpresa()).append("</h1>\n");
        html.append("<p>").append(empresa.getDireccion()).append("</p>\n");
        html.append("<p>").append(empresa.getCodigoPostal()).append(" ").append(empresa.getCiudad()).append("</p>\n");
        html.append("<p>CIF: ").append(empresa.getCif()).append("</p>\n");
        if (empresa.getTelefono() != null) {
            html.append("<p>Tel: ").append(empresa.getTelefono()).append("</p>\n");
        }
        html.append("</div>\n");

        // Datos factura
        html.append("<div class='factura-info'>\n");
        html.append("<h2>FACTURA</h2>\n");
        html.append("<p><strong>Nº:</strong> ").append(factura.getNumero()).append("</p>\n");
        html.append("<p><strong>Fecha:</strong> ").append(factura.getFecha().format(DATE_FORMAT)).append("</p>\n");
        if (factura.getFechaOperacion() != null) {
            html.append("<p><strong>Fecha Operación:</strong> ").append(factura.getFechaOperacion().format(DATE_FORMAT)).append("</p>\n");
        }
        html.append("</div>\n");
        html.append("</div>\n");

        // Datos cliente
        html.append("<div class='cliente'>\n");
        html.append("<h3>Cliente</h3>\n");
        html.append("<p><strong>").append(factura.getCliente().getNombre()).append("</strong></p>\n");
        if (factura.getCliente().getCif() != null) {
            html.append("<p>CIF/NIF: ").append(factura.getCliente().getCif()).append("</p>\n");
        }
        if (factura.getCliente().getDireccion() != null) {
            html.append("<p>").append(factura.getCliente().getDireccion()).append("</p>\n");
        }
        if (factura.getCliente().getCodigoPostal() != null) {
            html.append("<p>").append(factura.getCliente().getCodigoPostal()).append(" ");
            if (factura.getCliente().getPoblacion() != null) {
                html.append(factura.getCliente().getPoblacion());
            }
            html.append("</p>\n");
        }
        html.append("</div>\n");

        // Líneas de factura
        html.append("<table class='lineas'>\n");
        html.append("<thead>\n");
        html.append("<tr>\n");
        html.append("<th>Descripción</th>\n");
        html.append("<th style='text-align:center'>Cantidad</th>\n");
        html.append("<th style='text-align:right'>Precio</th>\n");
        html.append("<th style='text-align:right'>Dto.</th>\n");
        html.append("<th style='text-align:right'>IVA</th>\n");
        html.append("<th style='text-align:right'>Total</th>\n");
        html.append("</tr>\n");
        html.append("</thead>\n");
        html.append("<tbody>\n");

        for (FacturaLinea linea : factura.getLineas()) {
            html.append("<tr>\n");
            html.append("<td>").append(linea.getDescripcion()).append("</td>\n");
            html.append("<td style='text-align:center'>").append(formatearNumero(linea.getCantidad())).append("</td>\n");
            html.append("<td style='text-align:right'>").append(formatearImporte(linea.getPrecioUnitario())).append(" €</td>\n");
            html.append("<td style='text-align:right'>").append(formatearNumero(linea.getDescuento())).append("%</td>\n");
            html.append("<td style='text-align:right'>").append(formatearNumero(linea.getIva())).append("%</td>\n");
            html.append("<td style='text-align:right'><strong>").append(formatearImporte(linea.getTotal())).append(" €</strong></td>\n");
            html.append("</tr>\n");
        }

        html.append("</tbody>\n");
        html.append("</table>\n");

        // Totales
        html.append("<div class='totales'>\n");
        html.append("<table class='tabla-totales'>\n");
        html.append("<tr><td>Base Imponible:</td><td>").append(formatearImporte(factura.getBaseImponible())).append(" €</td></tr>\n");
        html.append("<tr><td>IVA:</td><td>").append(formatearImporte(factura.getTotalIva())).append(" €</td></tr>\n");
        if (factura.getTotalRecargo() != null && factura.getTotalRecargo().compareTo(BigDecimal.ZERO) > 0) {
            html.append("<tr><td>Recargo Equiv.:</td><td>").append(formatearImporte(factura.getTotalRecargo())).append(" €</td></tr>\n");
        }
        if (factura.getRetencionIrpf() != null && factura.getRetencionIrpf().compareTo(BigDecimal.ZERO) > 0) {
            html.append("<tr><td>Retención IRPF (").append(formatearNumero(factura.getPorcentajeRetencion())).append("%):</td><td>-").append(formatearImporte(factura.getRetencionIrpf())).append(" €</td></tr>\n");
        }
        html.append("<tr class='total-final'><td><strong>TOTAL:</strong></td><td><strong>").append(formatearImporte(factura.getTotal())).append(" €</strong></td></tr>\n");
        html.append("</table>\n");
        html.append("</div>\n");

        // Observaciones
        if (factura.getObservaciones() != null && !factura.getObservaciones().isEmpty()) {
            html.append("<div class='observaciones'>\n");
            html.append("<h4>Observaciones:</h4>\n");
            html.append("<p>").append(factura.getObservaciones()).append("</p>\n");
            html.append("</div>\n");
        }

        // Pie de página
        html.append("<div class='footer'>\n");
        html.append("<p>").append(empresa.getNombreEmpresa()).append(" - CIF: ").append(empresa.getCif()).append("</p>\n");
        if (empresa.getRegistroMercantil() != null) {
            html.append("<p>").append(empresa.getRegistroMercantil()).append("</p>\n");
        }
        html.append("</div>\n");

        html.append("</body></html>");

        return html.toString();
    }

    // ==========================================
    // ALBARANES
    // ==========================================

    /**
     * Generar PDF de albarán
     */
    public File generarPDFAlbaran(AlbaranVenta albaran) {
        log.info("📄 Generando PDF de albarán: {}", albaran.getNumero());

        try {
            String html = generarHTMLAlbaran(albaran);
            String nombreArchivo = String.format("Albaran_%s_%d.pdf",
                albaran.getNumero().replace("/", "-"),
                System.currentTimeMillis());

            File pdfFile = new File(RUTA_REPORTES + nombreArchivo);
            convertirHTMLaPDF(html, pdfFile);

            log.info("✅ PDF de albarán generado: {}", pdfFile.getAbsolutePath());
            return pdfFile;

        } catch (Exception e) {
            log.error("❌ Error generando PDF de albarán", e);
            throw new RuntimeException("Error al generar PDF de albarán", e);
        }
    }

    /**
     * Generar HTML de albarán
     */
    private String generarHTMLAlbaran(AlbaranVenta albaran) {
        EmpresaConfig empresa = obtenerDatosEmpresa();

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html><head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<title>Albarán ").append(albaran.getNumero()).append("</title>\n");
        html.append(obtenerEstilosCSS());
        html.append("</head><body>\n");

        // Encabezado
        html.append("<div class='cabecera'>\n");
        html.append("<div class='empresa'>\n");
        html.append("<h1>").append(empresa.getNombreEmpresa()).append("</h1>\n");
        html.append("<p>").append(empresa.getDireccion()).append("</p>\n");
        html.append("<p>").append(empresa.getCodigoPostal()).append(" ").append(empresa.getCiudad()).append("</p>\n");
        html.append("</div>\n");

        html.append("<div class='factura-info'>\n");
        html.append("<h2>ALBARÁN DE ENTREGA</h2>\n");
        html.append("<p><strong>Nº:</strong> ").append(albaran.getNumero()).append("</p>\n");
        html.append("<p><strong>Fecha:</strong> ").append(albaran.getFecha().format(DATE_FORMAT)).append("</p>\n");
        html.append("</div>\n");
        html.append("</div>\n");

        // Cliente
        html.append("<div class='cliente'>\n");
        html.append("<h3>Cliente</h3>\n");
        html.append("<p><strong>").append(albaran.getCliente().getNombre()).append("</strong></p>\n");
        if (albaran.getCliente().getDireccion() != null) {
            html.append("<p>").append(albaran.getCliente().getDireccion()).append("</p>\n");
        }
        html.append("</div>\n");

        // Líneas
        html.append("<table class='lineas'>\n");
        html.append("<thead>\n");
        html.append("<tr>\n");
        html.append("<th>Descripción</th>\n");
        html.append("<th style='text-align:center'>Cantidad</th>\n");
        html.append("</tr>\n");
        html.append("</thead>\n");
        html.append("<tbody>\n");

        for (AlbaranVentaLinea linea : albaran.getLineas()) {
            html.append("<tr>\n");
            html.append("<td>").append(linea.getDescripcion() != null ? linea.getDescripcion() : "").append("</td>\n");
            html.append("<td style='text-align:center'>").append(formatearNumero(linea.getCantidad())).append("</td>\n");
            html.append("</tr>\n");
        }

        html.append("</tbody>\n");
        html.append("</table>\n");

        // Observaciones
        if (albaran.getObservaciones() != null && !albaran.getObservaciones().isEmpty()) {
            html.append("<div class='observaciones'>\n");
            html.append("<p>").append(albaran.getObservaciones()).append("</p>\n");
            html.append("</div>\n");
        }

        // Firma
        html.append("<div class='firma'>\n");
        html.append("<p>Firma y sello del receptor:</p>\n");
        html.append("<div style='border-top:1px solid #000; width:300px; margin-top:50px;'></div>\n");
        html.append("</div>\n");

        html.append("</body></html>");

        return html.toString();
    }

    // ==========================================
    // PRESUPUESTOS
    // ==========================================

    /**
     * Generar PDF de presupuesto
     */
    public File generarPDFPresupuesto(Presupuesto presupuesto) {
        log.info("📄 Generando PDF de presupuesto: {}", presupuesto.getNumero());

        try {
            String html = generarHTMLPresupuesto(presupuesto);
            String nombreArchivo = String.format("Presupuesto_%s_%d.pdf",
                presupuesto.getNumero().replace("/", "-"),
                System.currentTimeMillis());

            File pdfFile = new File(RUTA_REPORTES + nombreArchivo);
            convertirHTMLaPDF(html, pdfFile);

            log.info("✅ PDF de presupuesto generado: {}", pdfFile.getAbsolutePath());
            return pdfFile;

        } catch (Exception e) {
            log.error("❌ Error generando PDF de presupuesto", e);
            throw new RuntimeException("Error al generar PDF de presupuesto", e);
        }
    }

    /**
     * Generar HTML de presupuesto
     */
    private String generarHTMLPresupuesto(Presupuesto presupuesto) {
        EmpresaConfig empresa = obtenerDatosEmpresa();

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html><head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<title>Presupuesto ").append(presupuesto.getNumero()).append("</title>\n");
        html.append(obtenerEstilosCSS());
        html.append("</head><body>\n");

        // Encabezado
        html.append("<div class='cabecera'>\n");
        html.append("<div class='empresa'>\n");
        html.append("<h1>").append(empresa.getNombreEmpresa()).append("</h1>\n");
        html.append("<p>").append(empresa.getDireccion()).append("</p>\n");
        html.append("</div>\n");

        html.append("<div class='factura-info'>\n");
        html.append("<h2>PRESUPUESTO</h2>\n");
        html.append("<p><strong>Nº:</strong> ").append(presupuesto.getNumero()).append("</p>\n");
        html.append("<p><strong>Fecha:</strong> ").append(presupuesto.getFecha().format(DATE_FORMAT)).append("</p>\n");
        if (presupuesto.getFechaValidez() != null) {
            html.append("<p><strong>Válido hasta:</strong> ").append(presupuesto.getFechaValidez().format(DATE_FORMAT)).append("</p>\n");
        }
        html.append("</div>\n");
        html.append("</div>\n");

        // Cliente
        html.append("<div class='cliente'>\n");
        html.append("<h3>Cliente</h3>\n");
        html.append("<p><strong>").append(presupuesto.getCliente().getNombre()).append("</strong></p>\n");
        html.append("</div>\n");

        // Líneas
        html.append("<table class='lineas'>\n");
        html.append("<thead>\n");
        html.append("<tr>\n");
        html.append("<th>Descripción</th>\n");
        html.append("<th style='text-align:center'>Cantidad</th>\n");
        html.append("<th style='text-align:right'>Precio</th>\n");
        html.append("<th style='text-align:right'>Total</th>\n");
        html.append("</tr>\n");
        html.append("</thead>\n");
        html.append("<tbody>\n");

        for (PresupuestoLinea linea : presupuesto.getLineas()) {
            html.append("<tr>\n");
            html.append("<td>").append(linea.getDescripcion()).append("</td>\n");
            html.append("<td style='text-align:center'>").append(formatearNumero(linea.getCantidad())).append("</td>\n");
            html.append("<td style='text-align:right'>").append(formatearImporte(linea.getPrecioUnitario())).append(" €</td>\n");
            html.append("<td style='text-align:right'><strong>").append(formatearImporte(linea.getTotal())).append(" €</strong></td>\n");
            html.append("</tr>\n");
        }

        html.append("</tbody>\n");
        html.append("</table>\n");

        // Total
        html.append("<div class='totales'>\n");
        html.append("<table class='tabla-totales'>\n");
        html.append("<tr class='total-final'><td><strong>TOTAL:</strong></td><td><strong>").append(formatearImporte(presupuesto.getTotal())).append(" €</strong></td></tr>\n");
        html.append("</table>\n");
        html.append("</div>\n");

        html.append("</body></html>");

        return html.toString();
    }

    // ==========================================
    // UTILIDADES
    // ==========================================

    /**
     * Convertir HTML a PDF usando iText 7
     */
    private void convertirHTMLaPDF(String html, File pdfFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
            ConverterProperties converterProperties = new ConverterProperties();
            HtmlConverter.convertToPdf(html, fos, converterProperties);
        }
    }

    /**
     * Obtener datos de la empresa
     */
    private EmpresaConfig obtenerDatosEmpresa() {
        // Se usa findFirstByActivoTrue() (repositorio) que devuelve Optional<EmpresaConfig>
        Optional<EmpresaConfig> opt = empresaConfigRepository.findFirstByActivoTrue();
        return opt.orElseGet(() -> {
            EmpresaConfig empresa = new EmpresaConfig();
            empresa.setNombreEmpresa("GRUPO BABO");
            empresa.setNombreComercial("Panadería Tahona");
            empresa.setCif("B12345678");
            empresa.setDireccion("Calle Principal, 123");
            empresa.setCodigoPostal("03001");
            empresa.setCiudad("Alicante");
            empresa.setProvincia("Alicante");
            empresa.setPais("España");
            return empresa;
        });
    }

    /**
     * Estilos CSS para los PDFs
     */
    private String obtenerEstilosCSS() {
        return """
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body { 
                    font-family: Arial, sans-serif; 
                    font-size: 10pt; 
                    padding: 20px;
                    color: #333;
                }
                .cabecera {
                    display: flex;
                    justify-content: space-between;
                    margin-bottom: 30px;
                    border-bottom: 2px solid #2c3e50;
                    padding-bottom: 15px;
                }
                .empresa h1 { 
                    font-size: 18pt; 
                    color: #2c3e50; 
                    margin-bottom: 10px;
                }
                .empresa p { margin: 3px 0; }
                .factura-info { 
                    text-align: right;
                }
                .factura-info h2 {
                    font-size: 20pt;
                    color: #e74c3c;
                    margin-bottom: 10px;
                }
                .factura-info p { margin: 5px 0; }
                .cliente {
                    background-color: #ecf0f1;
                    padding: 15px;
                    margin-bottom: 20px;
                    border-left: 4px solid #3498db;
                }
                .cliente h3 {
                    color: #2c3e50;
                    margin-bottom: 10px;
                }
                .cliente p { margin: 3px 0; }
                table.lineas {
                    width: 100%;
                    border-collapse: collapse;
                    margin-bottom: 20px;
                }
                table.lineas th {
                    background-color: #34495e;
                    color: white;
                    padding: 10px;
                    text-align: left;
                }
                table.lineas td {
                    padding: 8px;
                    border-bottom: 1px solid #ddd;
                }
                table.lineas tr:hover {
                    background-color: #f5f5f5;
                }
                .totales {
                    float: right;
                    width: 350px;
                    margin-top: 20px;
                }
                .tabla-totales {
                    width: 100%;
                    border-collapse: collapse;
                }
                .tabla-totales td {
                    padding: 8px;
                    border-bottom: 1px solid #ddd;
                }
                .tabla-totales td:last-child {
                    text-align: right;
                }
                .total-final {
                    background-color: #2c3e50;
                    color: white;
                    font-size: 12pt;
                }
                .observaciones {
                    clear: both;
                    margin-top: 30px;
                    padding: 15px;
                    background-color: #fff3cd;
                    border-left: 4px solid #ffc107;
                }
                .observaciones h4 {
                    margin-bottom: 10px;
                    color: #856404;
                }
                .firma {
                    margin-top: 50px;
                    text-align: center;
                }
                .footer {
                    margin-top: 50px;
                    padding-top: 20px;
                    border-top: 1px solid #ddd;
                    text-align: center;
                    font-size: 8pt;
                    color: #7f8c8d;
                }
            </style>
            """;
    }

    /**
     * Formatear importe con 2 decimales
     */
    private String formatearImporte(BigDecimal importe) {
        if (importe == null) return "0,00";
        return String.format("%,.2f", importe).replace(",", "X").replace(".", ",").replace("X", ".");
    }

    /**
     * Formatear número con decimales variables
     */
    private String formatearNumero(BigDecimal numero) {
        if (numero == null) return "0";
        // Si es entero, mostrar sin decimales
        if (numero.stripTrailingZeros().scale() <= 0) {
            return String.format("%,.0f", numero).replace(",", ".");
        }
        return String.format("%,.2f", numero).replace(",", "X").replace(".", ",").replace("X", ".");
    }
}

