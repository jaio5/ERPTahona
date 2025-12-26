package alicanteweb.erp.service;

import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaLinea;
import alicanteweb.erp.entities.VerifactuEvidence;
import alicanteweb.erp.repository.VerifactuEvidenceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class VerifactuService {

    private static final Logger log = LoggerFactory.getLogger(VerifactuService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    @Value("${verifactu.keystore.path}")
    private String keystorePath;

    @Value("${verifactu.keystore.password}")
    private String keystorePassword;

    @Value("${verifactu.key.alias}")
    private String keyAlias;

    @Value("${verifactu.key.password:${verifactu.keystore.password}}")
    private String keyPassword;

    @Value("${verifactu.aeat.enabled:false}")
    private boolean aeatEnabled;

    @Value("${verifactu.aeat.endpoint:https://www2.agenciatributaria.gob.es/wlpl/AVAC-FACT/ws/fe/SiiVerifactu}")
    private String aeatEndpoint;

    private final VerifactuEvidenceRepository evidenceRepository;
    private final EmpresaConfigService empresaConfigService;
    private final FacturaLineaService facturaLineaService;
    private final KeyStore keyStore;
    private final PrivateKey privateKey;
    private final X509Certificate certificate;
    private final boolean enabled;

    public VerifactuService(VerifactuEvidenceRepository evidenceRepository,
                           EmpresaConfigService empresaConfigService,
                           FacturaLineaService facturaLineaService) {
        this.evidenceRepository = evidenceRepository;
        this.empresaConfigService = empresaConfigService;
        this.facturaLineaService = facturaLineaService;
        KeyStore ks = null;
        PrivateKey pk = null;
        X509Certificate cert = null;
        boolean ok = false;

        try (InputStream is = openKeystoreStream(keystorePath)) {
            if (is == null) {
                log.warn("Keystore no encontrado en {} (classpath o disco) — verifactu deshabilitado", keystorePath);
            } else {
                ks = KeyStore.getInstance("PKCS12");
                ks.load(is, getPassword(keystorePassword).toCharArray());

                Key key = ks.getKey(keyAlias, getPassword(keyPassword, keystorePassword).toCharArray());
                if (key instanceof PrivateKey) {
                    pk = (PrivateKey) key;
                } else {
                    log.warn("La clave con alias {} no es privada; verifactu deshabilitado", keyAlias);
                }

                Certificate c = ks.getCertificate(keyAlias);
                if (c instanceof X509Certificate) {
                    cert = (X509Certificate) c;
                } else {
                    log.warn("El certificado con alias {} no es X509; verifactu deshabilitado", keyAlias);
                }

                if (pk != null && cert != null) {
                    ok = true;
                }
            }
        } catch (Exception e) {
            log.warn("Error cargando el keystore para verifactu — verifactu deshabilitado: {}", e.getMessage());
            if (log.isDebugEnabled()) log.debug("Stack:", e);
        }

        this.keyStore = ks;
        this.privateKey = pk;
        this.certificate = cert;
        this.enabled = ok;

        if (this.enabled) {
            log.info("Verifactu inicializado usando keystore {}", keystorePath);
        } else {
            log.info("Verifactu deshabilitado — la aplicación continuará sin registrar evidencias");
        }
    }

    /**
     * Indica si el servicio VeriFactu está habilitado
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Genera el hash SHA-256 de los datos proporcionados
     */
    public String generarHash(String datos) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(datos.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Genera el hash de una factura con encadenamiento (incluye hash anterior)
     */
    public String generarHashEncadenado(String datosFactura, String hashAnterior) throws NoSuchAlgorithmException {
        String datosCompletos = datosFactura;
        if (hashAnterior != null && !hashAnterior.isEmpty()) {
            datosCompletos += "|" + hashAnterior;
        }
        return generarHash(datosCompletos);
    }

    /**
     * Firma digitalmente los datos usando la clave privada del certificado
     */
    public byte[] firmarDatos(byte[] datos) throws Exception {
        if (!enabled) {
            throw new IllegalStateException("VeriFactu no está habilitado - no se puede firmar");
        }

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(datos);
        return signature.sign();
    }

    /**
     * Verifica una firma digital
     */
    public boolean verificarFirma(byte[] datos, byte[] firma) throws Exception {
        if (!enabled) {
            throw new IllegalStateException("VeriFactu no está habilitado - no se puede verificar");
        }

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(certificate);
        signature.update(datos);
        return signature.verify(firma);
    }

    /**
     * Obtiene la huella digital (fingerprint) del certificado
     */
    public String getCertificateFingerprint() throws NoSuchAlgorithmException {
        if (!enabled || certificate == null) {
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(certificate.getEncoded());
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            log.error("Error obteniendo fingerprint del certificado", e);
            throw new NoSuchAlgorithmException("Error obteniendo fingerprint", e);
        }
    }

    /**
     * Obtiene el hash anterior de la cadena (último registro guardado)
     */
    public String obtenerHashAnterior(String serie) {
        Optional<VerifactuEvidence> ultimaEvidencia = evidenceRepository.findFirstBySerieOrderByFechaEmisionDesc(serie);
        return ultimaEvidencia.map(VerifactuEvidence::getHash).orElse(null);
    }

    /**
     * Valida la integridad de la cadena de evidencias
     */
    public boolean validarCadenaIntegridad(String serie) {
        List<VerifactuEvidence> evidencias = evidenceRepository.findAllBySerieOrderByFechaEmisionAsc(serie);
        if (evidencias == null || evidencias.isEmpty()) {
            return true;
        }

        String hashAnterior = null;
        for (VerifactuEvidence evidencia : evidencias) {
            // Verificar que el hash anterior coincida
            if (hashAnterior != null && !hashAnterior.equals(evidencia.getHashAnterior())) {
                log.warn("Cadena rota en evidencia {} - hash anterior no coincide", evidencia.getId());
                return false;
            }
            hashAnterior = evidencia.getHash();
        }
        return true;
    }

    private static InputStream openKeystoreStream(String keystorePath) {
        InputStream is = VerifactuService.class.getResourceAsStream(keystorePath);
        if (is == null) {
            try {
                is = new java.io.FileInputStream(keystorePath);
            } catch (Exception ex) {
                // No es necesario loggear aquí, ya se hace en el constructor
            }
        }
        return is;
    }

    private static String getPassword(String password) {
        return password != null ? password : "";
    }

    private static String getPassword(String password, String fallback) {
        return password != null ? password : getPassword(fallback);
    }

    /**
     * Genera el XML de Verifactu para una factura usando datos de empresa_config
     */
    public String generarXMLFactura(Factura factura, List<FacturaLinea> lineas) {
        // Obtener configuración de empresa (GRUPO BABO)
        EmpresaConfig empresa = empresaConfigService.getConfiguracionActivaOrThrow();

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<RegistroFacturaVerifactu xmlns=\"https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RegistroFacturaVerifactu.xsd\">\n");

        // Datos del emisor (de empresa_config)
        xml.append("  <Cabecera>\n");
        xml.append("    <Emisor>\n");
        xml.append("      <NIF>").append(escapeXml(empresa.getVerifactuNifEmisor())).append("</NIF>\n");
        xml.append("      <NombreRazonSocial>").append(escapeXml(empresa.getNombreEmpresa())).append("</NombreRazonSocial>\n");
        xml.append("    </Emisor>\n");

        // Sistema informático (de empresa_config)
        xml.append("    <SistemaInformatico>\n");
        xml.append("      <NombreSistema>").append(escapeXml(empresa.getVerifactuNombreSistema())).append("</NombreSistema>\n");
        xml.append("      <Version>").append(escapeXml(empresa.getVerifactuVersionSistema())).append("</Version>\n");
        if (empresa.getVerifactuIdDispositivo() != null && !empresa.getVerifactuIdDispositivo().isEmpty()) {
            xml.append("      <IdDispositivo>").append(escapeXml(empresa.getVerifactuIdDispositivo())).append("</IdDispositivo>\n");
        }
        xml.append("    </SistemaInformatico>\n");
        xml.append("  </Cabecera>\n");

        // Datos de la factura
        xml.append("  <Factura>\n");
        xml.append("    <NumFactura>").append(escapeXml(factura.getNumero())).append("</NumFactura>\n");
        xml.append("    <FechaExpedicion>").append(factura.getFecha() != null ? factura.getFecha().format(DATE_FORMATTER) : "").append("</FechaExpedicion>\n");
        xml.append("    <HoraExpedicion>").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append("</HoraExpedicion>\n");

        // Cliente
        if (factura.getCliente() != null) {
            xml.append("    <Destinatario>\n");
            if (factura.getCliente().getCif() != null && !factura.getCliente().getCif().isEmpty()) {
                xml.append("      <NIF>").append(escapeXml(factura.getCliente().getCif())).append("</NIF>\n");
            }
            xml.append("      <NombreRazonSocial>").append(escapeXml(factura.getCliente().getNombre())).append("</NombreRazonSocial>\n");
            xml.append("    </Destinatario>\n");
        }

        // Líneas de factura
        if (lineas != null && !lineas.isEmpty()) {
            xml.append("    <Desglose>\n");
            BigDecimal baseImponible = BigDecimal.ZERO;
            BigDecimal totalIva = BigDecimal.ZERO;

            for (FacturaLinea linea : lineas) {
                BigDecimal subtotal = linea.getCantidad().multiply(linea.getPrecio());
                baseImponible = baseImponible.add(subtotal);
                if (linea.getIva() != null && linea.getIva().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal importeIva = subtotal.multiply(linea.getIva()).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                    totalIva = totalIva.add(importeIva);
                }
            }

            xml.append("      <BaseImponible>").append(baseImponible.setScale(2, java.math.RoundingMode.HALF_UP)).append("</BaseImponible>\n");
            xml.append("      <CuotaIVA>").append(totalIva.setScale(2, java.math.RoundingMode.HALF_UP)).append("</CuotaIVA>\n");
            xml.append("    </Desglose>\n");
        }

        // Total
        xml.append("    <ImporteTotal>").append(factura.getTotal() != null ? factura.getTotal().setScale(2, java.math.RoundingMode.HALF_UP) : "0.00").append("</ImporteTotal>\n");

        // Hash encadenado
        try {
            String hashAnterior = obtenerHashAnterior(factura.getNumero().substring(0, Math.min(4, factura.getNumero().length())));
            String datosFactura = factura.getNumero() + "|" + factura.getFecha() + "|" + factura.getTotal();
            String hash = generarHashEncadenado(datosFactura, hashAnterior);
            xml.append("    <Hash>").append(hash).append("</Hash>\n");
            if (hashAnterior != null) {
                xml.append("    <HashAnterior>").append(hashAnterior).append("</HashAnterior>\n");
            }
        } catch (Exception e) {
            log.error("Error generando hash para factura {}", factura.getNumero(), e);
        }

        xml.append("  </Factura>\n");
        xml.append("</RegistroFacturaVerifactu>\n");

        return xml.toString();
    }

    /**
     * Envía una factura a Verifactu/AEAT con todas las validaciones
     */
    public void enviarFacturaVerifactu(Factura factura) throws Exception {
        log.info("Iniciando envío de factura {} a Verifactu", factura.getNumero());

        // 1. Verificar que existe configuración de empresa
        if (!empresaConfigService.existeConfiguracionActiva()) {
            throw new IllegalStateException("Configure los datos de empresa antes de enviar facturas a Verifactu");
        }

        EmpresaConfig empresa = empresaConfigService.getConfiguracionActivaOrThrow();

        // 2. Verificar que Verifactu está habilitado en la empresa
        if (!Boolean.TRUE.equals(empresa.getVerifactuHabilitado())) {
            throw new IllegalStateException("Verifactu está deshabilitado en la configuración de empresa");
        }

        // 3. Verificar estado de la factura
        if (!"REVISION".equals(factura.getEstado())) {
            throw new IllegalStateException("Solo se pueden emitir facturas en estado REVISION. Estado actual: " + factura.getEstado());
        }

        // 4. Verificar que no se envió antes
        if (Boolean.TRUE.equals(factura.getVerifactuEnviada())) {
            throw new IllegalStateException("Esta factura ya fue enviada a Verifactu el " +
                (factura.getFechaEmisionVerifactu() != null ? factura.getFechaEmisionVerifactu().format(DATETIME_FORMATTER) : ""));
        }

        // 5. Verificar que el servicio Verifactu está habilitado (certificado)
        if (!this.enabled) {
            log.warn("Certificado Verifactu no disponible - registrando evidencia local solamente");
        }

        // 6. Obtener líneas de la factura
        List<FacturaLinea> lineas = facturaLineaService.findByFacturaId(factura.getId());
        if (lineas == null || lineas.isEmpty()) {
            throw new IllegalStateException("La factura no tiene líneas, no se puede emitir");
        }

        // 7. Generar XML con datos de GRUPO BABO
        String xml = generarXMLFactura(factura, lineas);
        log.info("XML generado para factura {}: {} caracteres", factura.getNumero(), xml.length());

        // 8. Generar hash y firmar (si está habilitado)
        String hash = generarHash(xml);
        byte[] firma = null;
        if (this.enabled) {
            firma = firmarDatos(xml.getBytes(StandardCharsets.UTF_8));
            log.info("Factura {} firmada digitalmente", factura.getNumero());
        }

        // 9. Guardar evidencia en BD
        VerifactuEvidence evidencia = new VerifactuEvidence();
        evidencia.setSerie(factura.getNumero().substring(0, Math.min(4, factura.getNumero().length())));
        evidencia.setNumero(factura.getNumero());
        evidencia.setFacturaId(factura.getId().toString());
        evidencia.setFechaEmision(factura.getFecha() != null ?
            factura.getFecha().atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant() :
            java.time.Instant.now());
        evidencia.setHash(hash);
        evidencia.setHashAnterior(obtenerHashAnterior(evidencia.getSerie()));
        if (firma != null) {
            evidencia.setSignature(firma);
        }
        evidencia.setEstado("ENVIADO");
        evidencia.setFechaEnvio(java.time.Instant.now());

        // Metadata como Map
        java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put("empresa", empresa.getNombreEmpresa());
        metadata.put("cif", empresa.getCif());
        metadata.put("xml_length", xml.length());
        evidencia.setMetadata(metadata);

        evidenceRepository.save(evidencia);
        log.info("Evidencia guardada para factura {} con ID {}", factura.getNumero(), evidencia.getId());

        // 10. Envío real a la AEAT (si está habilitado)
        if (aeatEnabled && this.enabled) {
            try {
                log.info("Enviando factura {} a AEAT endpoint: {}", factura.getNumero(), aeatEndpoint);
                String respuestaAEAT = enviarXMLaAEAT(xml, firma);

                // Actualizar evidencia con respuesta
                evidencia.setCodigoRespuestaAEAT(respuestaAEAT != null ? "OK" : "ERROR");
                if (respuestaAEAT != null) {
                    metadata.put("respuesta_aeat", respuestaAEAT);
                    metadata.put("fecha_envio_real", java.time.Instant.now().toString());
                    evidencia.setMetadata(metadata);
                }
                evidenceRepository.save(evidencia);

                log.info("✅ Factura {} enviada y registrada correctamente en AEAT", factura.getNumero());
            } catch (Exception e) {
                log.error("❌ Error enviando factura {} a AEAT: {}", factura.getNumero(), e.getMessage());
                evidencia.setEstado("ERROR");
                evidencia.setErrorMessage("Error al enviar a AEAT: " + e.getMessage());
                evidencia.setCodigoRespuestaAEAT("ERROR");
                evidenceRepository.save(evidencia);
                throw new Exception("Error al enviar factura a AEAT: " + e.getMessage(), e);
            }
        } else {
            if (!aeatEnabled) {
                log.info("ℹ️ Envío a AEAT deshabilitado (verifactu.aeat.enabled=false). Evidencia guardada localmente.");
            }
            if (!this.enabled) {
                log.info("ℹ️ Certificado no disponible. Evidencia guardada localmente sin firma digital.");
            }
            log.info("✅ Factura {} registrada localmente (modo de pruebas)", factura.getNumero());
        }
    }

    /**
     * Envía el XML de la factura a la AEAT mediante HTTP POST
     * @param xml El XML de la factura
     * @param firma La firma digital (puede ser null)
     * @return La respuesta de la AEAT
     * @throws Exception Si hay error en el envío
     */
    private String enviarXMLaAEAT(String xml, byte[] firma) throws Exception {
        try {
            // Crear cliente HTTP
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(30))
                .build();

            // Preparar el body (XML + firma si existe)
            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            StringBuilder body = new StringBuilder();

            // Parte XML
            body.append("--").append(boundary).append("\r\n");
            body.append("Content-Disposition: form-data; name=\"file\"; filename=\"factura.xml\"\r\n");
            body.append("Content-Type: application/xml\r\n\r\n");
            body.append(xml).append("\r\n");

            // Parte firma (si existe)
            if (firma != null) {
                body.append("--").append(boundary).append("\r\n");
                body.append("Content-Disposition: form-data; name=\"signature\"\r\n");
                body.append("Content-Type: application/octet-stream\r\n\r\n");
                body.append(Base64.getEncoder().encodeToString(firma)).append("\r\n");
            }

            body.append("--").append(boundary).append("--\r\n");

            // Crear request
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(aeatEndpoint))
                .timeout(java.time.Duration.ofSeconds(60))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("Accept", "application/xml, application/json")
                .header("User-Agent", "ERP-Tahona/1.0 (GRUPO BABO)")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

            // Enviar request
            java.net.http.HttpResponse<String> response = client.send(request,
                java.net.http.HttpResponse.BodyHandlers.ofString());

            // Verificar respuesta
            int statusCode = response.statusCode();
            String responseBody = response.body();

            log.info("Respuesta AEAT - Status: {}, Body: {}", statusCode, responseBody);

            if (statusCode >= 200 && statusCode < 300) {
                return responseBody;
            } else {
                throw new Exception("Error HTTP " + statusCode + ": " + responseBody);
            }

        } catch (java.net.http.HttpTimeoutException e) {
            throw new Exception("Timeout al conectar con AEAT", e);
        } catch (java.io.IOException e) {
            throw new Exception("Error de conexión con AEAT", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Exception("Envío interrumpido", e);
        }
    }

    /**
     * Escapa caracteres especiales XML
     */
    private String escapeXml(String texto) {
        if (texto == null) return "";
        return texto
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
    }
}
