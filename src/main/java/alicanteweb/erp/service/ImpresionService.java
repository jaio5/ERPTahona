package alicanteweb.erp.service;

import alicanteweb.erp.exception.ErpException;
import alicanteweb.erp.entities.AlbaranVenta;
import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.HojaRuta;
import alicanteweb.erp.entities.HojaRutaEntrega;
import alicanteweb.erp.entities.Lote;
import alicanteweb.erp.entities.Receta;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class ImpresionService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String OUTPUT_DIR = "impresiones/";

    private final EmpresaConfigService empresaConfigService;
    private final FacturacionEventoService facturacionEventoService;
    private final HojaRutaService hojaRutaService;
    private final SpringTemplateEngine templateEngine;

    public ImpresionService(EmpresaConfigService empresaConfigService,
                            FacturacionEventoService facturacionEventoService,
                            HojaRutaService hojaRutaService,
                            SpringTemplateEngine templateEngine) {
        this.empresaConfigService = empresaConfigService;
        this.facturacionEventoService = facturacionEventoService;
        this.hojaRutaService = hojaRutaService;
        this.templateEngine = templateEngine;
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
            throw new ErpException("Error al generar PDF de factura: " + e.getMessage(), e);
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
            throw new ErpException("Error al generar PDF de albaran: " + e.getMessage(), e);
        }
    }

    public File generarFacturaPdf(Factura factura) {
        try {
            String html = renderTemplate("pdf/factura", Map.of(
                "factura", factura,
                "empresa", empresaConfigService.getConfiguracionActivaOrThrow(),
                "datosVerificacion", generarDatosVerificacion(factura),
                "now", LocalDateTime.now()
            ));
            File pdfFile = buildOutputFile("Factura", factura.getNumero());
            generarPdf(html, pdfFile);
            return pdfFile;
        } catch (Exception e) {
            throw new ErpException("Error generando PDF de factura " + factura.getNumero(), e);
        }
    }

    public File generarAlbaranPdf(AlbaranVenta albaran) {
        try {
            String html = renderTemplate("pdf/albaran", Map.of(
                "albaran", albaran,
                "empresa", empresaConfigService.getConfiguracionActivaOrThrow(),
                "now", LocalDateTime.now()
            ));
            File pdfFile = buildOutputFile("Albaran", albaran.getNumero());
            generarPdf(html, pdfFile);
            return pdfFile;
        } catch (Exception e) {
            throw new ErpException("Error generando PDF de albarán " + albaran.getNumero(), e);
        }
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

    public void imprimirHojaRuta(HojaRuta hojaRuta, boolean abrirPdf) {
        try {
            log.info("Generando hoja de ruta PDF para: {}", hojaRuta.getFecha());
            File pdfFile = generarHojaRutaPdf(hojaRuta);
            registrarEventoDocumento("IMPRESION_HOJA_RUTA", hojaRuta.getFecha().toString(), pdfFile);
            imprimirPdf(pdfFile, abrirPdf);
        } catch (Exception e) {
            log.error("Error imprimiendo hoja de ruta", e);
            throw new ErpException("Error al generar PDF de hoja de ruta: " + e.getMessage(), e);
        }
    }

    public void imprimirEtiqueta(Articulo articulo, Lote lote, Receta receta, boolean abrirPdf) {
        try {
            String nombre = articulo.getNombre() != null ? articulo.getNombre() : articulo.getCodigo();
            log.info("Generando etiqueta para: {}", nombre);
            File pdfFile = generarEtiquetaPdf(articulo, lote, receta);
            registrarEventoDocumento("IMPRESION_ETIQUETA", articulo.getCodigo(), pdfFile);
            imprimirPdf(pdfFile, abrirPdf);
        } catch (Exception e) {
            log.error("Error imprimiendo etiqueta", e);
            throw new ErpException("Error al generar PDF de etiqueta: " + e.getMessage(), e);
        }
    }

    private String renderTemplate(String templateName, Map<String, Object> vars) {
        Context ctx = new Context();
        ctx.setVariables(vars);
        return templateEngine.process(templateName, ctx);
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

    private File generarHojaRutaPdf(HojaRuta hojaRuta) {
        List<HojaRutaEntrega> entregas = hojaRutaService.getEntregas(hojaRuta.getId());
        EmpresaConfig empresa = empresaConfigService.getConfiguracionActivaOrThrow();
        Map<String, Object> vars = new HashMap<>();
        vars.put("hojaRuta", hojaRuta);
        vars.put("entregas", entregas);
        vars.put("empresa", empresa);
        vars.put("now", LocalDateTime.now());
        String html = renderTemplate("pdf/hoja-ruta", vars);
        File outputFile = buildOutputFile("hoja_ruta",
            hojaRuta.getFecha() != null ? hojaRuta.getFecha().toString() : "sin-fecha");
        try {
            generarPdf(html, outputFile);
        } catch (Exception e) {
            throw new ErpException("Error generando PDF de hoja de ruta", e);
        }
        return outputFile;
    }

    private File generarEtiquetaPdf(Articulo articulo, Lote lote, Receta receta) {
        EmpresaConfig empresa = null;
        try {
            empresa = empresaConfigService.getConfiguracionActiva().orElse(null);
        } catch (Exception e) {
            log.debug("No se pudo cargar datos de empresa para etiqueta");
        }
        Map<String, Object> vars = new HashMap<>();
        vars.put("articulo", articulo);
        vars.put("lote", lote);
        vars.put("receta", receta);
        vars.put("empresa", empresa);
        vars.put("now", LocalDateTime.now());
        String html = renderTemplate("pdf/etiqueta", vars);
        File outputFile = buildOutputFile("etiqueta", articulo.getCodigo());
        try {
            generarPdf(html, outputFile);
        } catch (Exception e) {
            throw new ErpException("Error generando PDF de etiqueta", e);
        }
        return outputFile;
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
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("rutaPdf", pdfFile.getAbsolutePath());
        metadata.put("directorio", new File(OUTPUT_DIR).getAbsolutePath());
        facturacionEventoService.registrarEvento(
            FacturacionEventoService.AMBITO_FACTURAS,
            tipoEvento,
            referencia,
            metadata
        );
    }
}
