package alicanteweb.erp.service;

import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.util.HashUtils;
import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class DeclaracionResponsableService {

    private static final String OUTPUT_DIR = "impresiones/declaraciones";
    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final EmpresaConfigService empresaConfigService;
    private final FacturacionEventoService facturacionEventoService;

    public DeclaracionResponsableService(EmpresaConfigService empresaConfigService,
                                         FacturacionEventoService facturacionEventoService) {
        this.empresaConfigService = empresaConfigService;
        this.facturacionEventoService = facturacionEventoService;
    }

    @Transactional
    public File generarDeclaracionResponsable() {
        try {
            EmpresaConfig config = empresaConfigService.getConfiguracionActivaOrThrow();
            validarDatosMinimos(config);

            Files.createDirectories(Path.of(OUTPUT_DIR));
            LocalDateTime fecha = LocalDateTime.now();
            String version = valor(config.getVerifactuVersionSistema(), "sin-version");
            String filename = "declaracion-responsable-" + sanitizar(config.getCif()) + "-" + fecha.format(FILE_DATE) + ".pdf";
            File pdf = Path.of(OUTPUT_DIR, filename).toFile();

            String html = generarHtml(config, fecha, version);
            try (FileOutputStream output = new FileOutputStream(pdf)) {
                HtmlConverter.convertToPdf(html, output);
            }

            String hash = HashUtils.sha256Hex(Files.readAllBytes(pdf.toPath()));
            config.setDeclaracionResponsableEmitida(true);
            config.setDeclaracionResponsableFecha(fecha);
            config.setDeclaracionResponsableVersion(version);
            config.setDeclaracionResponsableHash(hash);
            config.setDeclaracionResponsableRuta(pdf.getAbsolutePath());
            empresaConfigService.save(config);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("modalidadSif", config.getSifModalidad() != null ? config.getSifModalidad().name() : null);
            metadata.put("origenSistema", "DeclaracionResponsableService");
            metadata.put("versionSistema", version);
            metadata.put("hashDocumento", hash);
            metadata.put("ruta", pdf.getAbsolutePath());
            facturacionEventoService.registrarEvento(
                FacturacionEventoService.AMBITO_GLOBAL,
                "DECLARACION_RESPONSABLE_EMITIDA",
                version,
                metadata
            );

            return pdf;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar la declaracion responsable: " + e.getMessage(), e);
        }
    }

    private void validarDatosMinimos(EmpresaConfig config) {
        if (esVacio(config.getCif()) || esVacio(config.getNombreEmpresa())) {
            throw new IllegalStateException("Configura razon social y NIF/CIF antes de generar la declaracion responsable.");
        }
        if (esVacio(config.getVerifactuNombreSistema()) || esVacio(config.getVerifactuVersionSistema()) || esVacio(config.getVerifactuIdDispositivo())) {
            throw new IllegalStateException("Configura nombre, version e identificador del sistema SIF.");
        }
        if (esVacio(config.getProductorSoftware()) || esVacio(config.getNifProductorSoftware())) {
            throw new IllegalStateException("Configura productor del software y NIF del productor.");
        }
    }

    private String generarHtml(EmpresaConfig config, LocalDateTime fecha, String version) {
        String modalidad = config.getSifModalidad() != null ? config.getSifModalidad().name() : "VERIFACTU";
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>"
            + "body{font-family:Arial,sans-serif;font-size:12px;line-height:1.45;margin:32px;color:#111}"
            + "h1{font-size:20px;text-align:center;margin-bottom:24px}"
            + "h2{font-size:14px;margin-top:20px;border-bottom:1px solid #ccc;padding-bottom:4px}"
            + "table{width:100%;border-collapse:collapse;margin-top:8px}td{padding:5px;border-bottom:1px solid #eee;vertical-align:top}"
            + ".label{font-weight:bold;width:32%}.firma{margin-top:42px}.small{font-size:10px;color:#555}"
            + "</style></head><body>"
            + "<h1>Declaracion responsable del sistema informatico de facturacion</h1>"
            + "<p>El productor y la entidad usuaria identificados en este documento declaran que el sistema descrito esta configurado para operar conforme a los requisitos de integridad, conservacion, accesibilidad, legibilidad, trazabilidad e inalterabilidad exigidos para los sistemas informaticos de facturacion.</p>"
            + "<h2>Empresa usuaria</h2><table>"
            + row("Razon social", config.getNombreEmpresa())
            + row("NIF/CIF", config.getCif())
            + row("Direccion", config.getDireccion())
            + "</table>"
            + "<h2>Productor del software</h2><table>"
            + row("Productor", config.getProductorSoftware())
            + row("NIF productor", config.getNifProductorSoftware())
            + "</table>"
            + "<h2>Sistema declarado</h2><table>"
            + row("Nombre del sistema", config.getVerifactuNombreSistema())
            + row("Version", version)
            + row("Identificador instalacion", config.getVerifactuIdDispositivo())
            + row("Modalidad SIF", modalidad)
            + row("Normativa tecnica", FacturacionEventoService.NORMATIVA_SIF)
            + row("Fecha emision", fecha.format(DISPLAY_DATE))
            + "</table>"
            + "<h2>Manifestacion</h2>"
            + "<p>La presente declaracion debe conservarse junto con la version del sistema, configuracion aplicada, certificados, registros de eventos y evidencias generadas por la aplicacion.</p>"
            + "<div class='firma'>Firma responsable: ____________________________________</div>"
            + "<p class='small'>Documento generado por ERP Tahona. La huella SHA-256 se guarda en la configuracion de empresa tras generar el PDF.</p>"
            + "</body></html>";
    }

    private String row(String label, String value) {
        return "<tr><td class='label'>" + escape(label) + "</td><td>" + escape(valor(value, "")) + "</td></tr>";
    }

    private boolean esVacio(String value) {
        return value == null || value.isBlank();
    }

    private String valor(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }

    private String sanitizar(String value) {
        return valor(value, "empresa").replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private String escape(String value) {
        return value == null ? "" : value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
    }
}
