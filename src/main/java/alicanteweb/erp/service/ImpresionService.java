package alicanteweb.erp.service;

import alicanteweb.erp.exception.ErpException;
import alicanteweb.erp.entities.AlbaranVenta;
import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaLinea;
import alicanteweb.erp.entities.HojaRuta;
import alicanteweb.erp.entities.HojaRutaEntrega;
import alicanteweb.erp.entities.Lote;
import alicanteweb.erp.entities.Receta;
import alicanteweb.erp.util.DesgloseFiscal;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class ImpresionService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String OUTPUT_DIR = "impresiones/";
    private static final String LOGO_RESOURCE = "/img/logo-empresa.png";

    /** Logo/escudo incrustado (data URI base64), cargado una sola vez desde el classpath. */
    private volatile String logoDataUri;

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
            DesgloseFiscal.Resultado desglose = DesgloseFiscal.calcular(lineasDeFactura(factura),
                factura.getDescuentoGlobalTipo(), factura.getDescuentoGlobalValor());
            Map<String, Object> vars = new HashMap<>();
            vars.put("factura", factura);
            vars.put("empresa", empresaConfigService.getConfiguracionActivaOrThrow());
            vars.put("datosVerificacion", generarDatosVerificacion(factura));
            vars.put("desgloseIva", filasIva(desglose));
            vars.put("descuentoGlobal", desglose.descuentoGlobal());
            vars.put("logo", getLogoDataUri());
            vars.put("now", LocalDateTime.now());
            String html = renderTemplate("pdf/factura", vars);
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
                "resumen", resumenAlbaran(albaran),
                "logo", getLogoDataUri(),
                "now", LocalDateTime.now()
            ));
            // (el resumen ya incluye base, IVA, descuento global y total)
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

    /**
     * Logo/escudo de la empresa como data URI base64, leído del classpath una sola vez.
     * Devuelve cadena vacía si no está disponible (la plantilla lo omite).
     */
    private String getLogoDataUri() {
        if (logoDataUri == null) {
            synchronized (this) {
                if (logoDataUri == null) {
                    String cargado = "";
                    try (InputStream is = getClass().getResourceAsStream(LOGO_RESOURCE)) {
                        if (is != null) {
                            cargado = "data:image/png;base64,"
                                + Base64.getEncoder().encodeToString(is.readAllBytes());
                        } else {
                            log.warn("Logo de empresa no encontrado en el classpath: {}", LOGO_RESOURCE);
                        }
                    } catch (Exception e) {
                        log.warn("No se pudo cargar el logo de empresa: {}", e.getMessage());
                    }
                    logoDataUri = cargado;
                }
            }
        }
        return logoDataUri;
    }

    /** Convierte las líneas de una factura al modelo de entrada de {@link DesgloseFiscal}. */
    private List<DesgloseFiscal.Linea> lineasDeFactura(Factura factura) {
        List<DesgloseFiscal.Linea> lineas = new ArrayList<>();
        if (factura.getFacturaLineas() != null) {
            for (FacturaLinea l : factura.getFacturaLineas()) {
                lineas.add(new DesgloseFiscal.Linea(l.getCantidad(), l.getPrecioUnitario(), l.getIva(),
                    l.getDescuentoTipo(), l.getDescuento()));
            }
        }
        return lineas;
    }

    /** Mapea el desglose por tipo de IVA al modelo que consume la plantilla (tipo/base/cuota). */
    private List<Map<String, Object>> filasIva(DesgloseFiscal.Resultado desglose) {
        List<Map<String, Object>> filas = new ArrayList<>();
        for (DesgloseFiscal.TipoIva t : desglose.porTipo()) {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("tipo", t.tipo());
            fila.put("base", t.base());
            fila.put("cuota", t.cuota());
            filas.add(fila);
        }
        return filas;
    }

    /**
     * Resumen de importes del albarán para el pie: base imponible, IVA y total a pagar.
     * La base de cada línea es cantidad x precio; el IVA se calcula con el tipo de la línea.
     */
    private Map<String, BigDecimal> resumenAlbaran(AlbaranVenta albaran) {
        List<DesgloseFiscal.Linea> lineas = new ArrayList<>();
        if (albaran.getAlbaranVentaLineas() != null) {
            for (var l : albaran.getAlbaranVentaLineas()) {
                lineas.add(new DesgloseFiscal.Linea(l.getCantidad(), l.getPrecio(), l.getIva(),
                    l.getDescuentoTipo(), l.getDescuento()));
            }
        }
        DesgloseFiscal.Resultado r = DesgloseFiscal.calcular(lineas,
            albaran.getDescuentoGlobalTipo(), albaran.getDescuentoGlobalValor());
        Map<String, BigDecimal> resumen = new LinkedHashMap<>();
        resumen.put("base", r.base());
        resumen.put("iva", r.iva());
        resumen.put("descuentoGlobal", r.descuentoGlobal());
        resumen.put("total", r.total());
        return resumen;
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
