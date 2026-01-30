package alicanteweb.erp.service;

import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaLinea;
import alicanteweb.erp.entities.VerifactuEvidence;
import alicanteweb.erp.repository.VerifactuEvidenceRepository;
import alicanteweb.erp.util.HashUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import alicanteweb.erp.util.CertificateUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import lombok.Getter;
import org.springframework.beans.factory.ObjectProvider;

@Service
public class VerifactuService implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(VerifactuService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    @Value("${verifactu.keystore.path:}")
    private String keystorePath;

    @Value("${verifactu.keystore.password:}")
    private String keystorePassword;

    @Value("${verifactu.key.alias:}")
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
    private final QrCodeService qrCodeService;
    private final VerifactuAeatSoapClient aeatSoapClient;
    private PrivateKey privateKey;
    private X509Certificate certificate;

    // Eliminadas referencias reflejadas innecesarias que solo existían para "silenciar" warnings.
    // Si en el futuro necesitamos invocar métodos por reflexión, añadiremos una API explícita.

    @Getter
    private boolean enabled;

    public VerifactuService(VerifactuEvidenceRepository evidenceRepository,
                           EmpresaConfigService empresaConfigService,
                           FacturaLineaService facturaLineaService,
                           QrCodeService qrCodeService,
                           ObjectProvider<VerifactuAeatSoapClient> aeatSoapClientProvider) {
        this.evidenceRepository = evidenceRepository;
        this.empresaConfigService = empresaConfigService;
        this.facturaLineaService = facturaLineaService;
        this.qrCodeService = qrCodeService;
        // Obtener el bean si está disponible (el cliente SOAP puede estar deshabilitado por properties)
        this.aeatSoapClient = aeatSoapClientProvider.getIfAvailable();
        // Inicialización provisional; la carga real del keystore se hace en @PostConstruct
        this.privateKey = null;
        this.certificate = null;
        this.enabled = false;
    }

    private void initKeystore() {
        PrivateKey pk = null;
        X509Certificate cert = null;
        boolean ok = false;

        // Comprobación rápida: si la ruta apunta a un fichero vacío, evitar intentar cargarlo
        try {
            if (keystorePath != null && !keystorePath.trim().isEmpty()) {
                File kf = new File(keystorePath);
                if (kf.exists() && kf.isFile() && kf.length() == 0) {
                    log.warn("Keystore file exists but is empty: {} - Verifactu deshabilitado", keystorePath);
                    this.privateKey = null;
                    this.certificate = null;
                    this.enabled = false;
                    return;
                }
            }
        } catch (Exception ex) {
            if (log.isDebugEnabled()) log.debug("Error checking keystore file size: {}", ex.getMessage(), ex);
            // continuar; la carga seguirá usando openKeystoreStream
        }

        try (InputStream is = openKeystoreStream(keystorePath)) {
            if (is == null) {
                log.warn("Keystore not found at {} (classpath or file system) - Verifactu disabled", keystorePath);
            } else {
                // Seguridad: si el keystore es grande (> MAX_KEYSTORE_SIZE_BYTES) considerarlo sospechoso
                try {
                    if (keystorePath != null) {
                        File kf = new File(keystorePath);
                        if (kf.exists() && kf.isFile() && kf.length() > CertificateUtils.MAX_KEYSTORE_SIZE_BYTES) {
                            log.warn("Keystore {} too large ({} bytes) - Verifactu disabled for safety", keystorePath, kf.length());
                            this.privateKey = null;
                            this.certificate = null;
                            this.enabled = false;
                            return;
                        }
                    }
                } catch (Exception ex) {
                    if (log.isDebugEnabled()) log.debug("Could not check keystore size: {}", ex.getMessage(), ex);
                }

                // Defensive: si el stream aparenta estar vacío, evitar parsearlo
                try {
                    if (is.available() == 0) {
                        log.warn("Keystore stream available==0 (posible fichero vacío or unreadable): {} - Verifactu deshabilitado", keystorePath);
                        this.privateKey = null;
                        this.certificate = null;
                        this.enabled = false;
                        return;
                    }
                } catch (IOException e) {
                    if (log.isDebugEnabled()) log.debug("Could not check InputStream.available(): {}", e.getMessage(), e);
                }

                try {
                    KeyStore ks = KeyStore.getInstance("PKCS12");
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
                        try {
                            // Validación defensiva del certificado para mitigar parsing errors
                            CertificateUtils.validateCertificateSafe(cert);
                        } catch (Exception cvEx) {
                            log.warn("Certificado con problemas de validación: {} - verifactu deshabilitado", cvEx.getMessage());
                            cert = null;
                        }
                    } else {
                        log.warn("El certificado con alias {} no es X509; verifactu deshabilitado", keyAlias);
                    }

                    if (pk != null && cert != null) {
                        ok = true;
                    }
                } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException ex) {
                    // Mensaje claro para casos conocidos (fichero corrupto, formato no esperado, contraseña incorrecta, etc.)
                    log.warn("Error parsing/loading keystore (probable fichero corrupto o formato/contraseña incorrecta) - Verifactu deshabilitado: {}", ex.getMessage());
                    if (log.isDebugEnabled()) log.debug("Stack:", ex);
                } catch (Throwable t) {
                    // Capturar errores inesperados (p. ej. ASN.1 parsing errors internos) y no romper el arranque
                    log.warn("Unexpected error while loading keystore - Verifactu deshabilitado: {}", t.getMessage());
                    if (log.isDebugEnabled()) log.debug("Stack:", t);
                }
            }
        } catch (Exception e) {
            log.warn("Error cargando el keystore para verifactu - verifactu deshabilitado: {}", e.getMessage());
            if (log.isDebugEnabled()) log.debug("Stack:", e);
        }

        this.privateKey = pk;
        this.certificate = cert;
        this.enabled = ok;

        if (this.enabled) {
            log.info("Verifactu inicializado usando keystore {}", keystorePath);
        } else {
            log.info("Verifactu deshabilitado - la aplicación continuará sin registrar evidencias");
        }
    }

    @Override
    public void afterPropertiesSet() {
        // Ejecutar la inicialización del keystore después de la inyección de propiedades/beans
        initKeystore();

        // NOTA: eliminadas las asignaciones reflejadas a métodos públicos usadas solo para
        // silenciar advertencias. No se necesita reflexión aquí: los métodos públicos
        // `verificarFirma` y `enviarFacturaVerifactu` están disponibles para llamados
        // directos desde controladores o tests.

        // Realizar una auto-verificación ligera para usar/validar los métodos de firma y verificación
        // Esto también evita warnings de "método nunca usado" y ofrece diagnóstico al iniciar.
        if (this.enabled) {
            try {
                byte[] payload = "verifactu-selftest".getBytes(StandardCharsets.UTF_8);
                byte[] sig = firmarDatos(payload);
                boolean ok = verificarFirma(payload, sig);
                log.info("Verifactu self-test: firma/verificación => {}", ok ? "OK" : "FALLÓ");
            } catch (Exception e) {
                log.warn("Self-test de firma/verificación falló: {}", e.getMessage());
                if (log.isDebugEnabled()) log.debug("Stack:", e);
            }
        }
    }

    /**
     * Método público de delegación para compatibilidad con controladores que llaman a
     * `enviarFactura(...)`. Mantener este método evita renombrados masivos y preserva
     * la intención del API público.
     */
    public java.util.Map<String,Object> enviarFactura(Factura factura) {
        java.util.Map<String,Object> resultado = new java.util.HashMap<>();
        try {
            enviarFacturaVerifactu(factura);
            resultado.put("exito", true);
            resultado.put("mensaje", "Factura registrada/enviada correctamente (local o AEAT dependiendo de configuración)");
        } catch (Exception e) {
            log.error("Error en enviarFactura wrapper: {}", e.getMessage());
            resultado.put("exito", false);
            resultado.put("mensaje", e.getMessage());
            resultado.put("error", e.toString());
        }
        return resultado;
    }

    /**
     * Genera el hash SHA-256 de los datos proporcionados (Base64 URL-safe sin padding)
     */
    public String generarHash(String datos) throws Exception {
        // Usar formato URL-safe para persistir y usar en QR
        return HashUtils.sha256Base64UrlSafe(datos);
    }

    /**
     * Genera el hash de una factura con encadenamiento (incluye hash anterior)
     */
    public String generarHashEncadenado(String datosFactura, String hashAnterior) throws Exception {
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
        // Use the certificate's public key explicitly to initialize verification
        signature.initVerify(certificate.getPublicKey());
        signature.update(datos);
        return signature.verify(firma);
    }

    /**
     * Obtiene la huella digital (fingerprint) del certificado
     */
    public String getCertificateFingerprint() throws Exception {
        if (!enabled || certificate == null) {
            return null;
        }

        try {
            // Usar HashUtils para fingerprint en Base64 URL-safe
            byte[] certBytes = certificate.getEncoded();
            return HashUtils.sha256Base64UrlSafe(new String(certBytes, StandardCharsets.ISO_8859_1));
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Indica si la conexión a AEAT está disponible y el certificado cargado.
     * Usado por otros servicios para decidir si el envío a AEAT debe permitirse.
     */
    public boolean isAeatAvailable() {
        return this.aeatEnabled && this.enabled && this.aeatSoapClient != null;
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

    // Cambiado a public para permitir la reutilización por el servicio de diagnóstico
    public static InputStream openKeystoreStream(String keystorePath) {
        // Validar entrada para evitar NullPointerException dentro de Class.getResourceAsStream
        if (keystorePath == null || keystorePath.trim().isEmpty()) {
            if (log.isWarnEnabled()) log.warn("Keystore path is null or empty");
            return null;
        }

        InputStream is = null;
        try {
            // Intentar cargar desde classpath con ClassLoader (recomendado)
            try {
                is = Thread.currentThread().getContextClassLoader().getResourceAsStream(keystorePath.startsWith("/") ? keystorePath.substring(1) : keystorePath);
                if (is == null) {
                    // fallback al comportamiento anterior
                    is = VerifactuService.class.getResourceAsStream(keystorePath);
                }
            } catch (NullPointerException npe) {
                // Defensive: getResourceAsStream lanza NPE si recibe null, ya manejado arriba
                if (log.isDebugEnabled()) log.debug("getResourceAsStream threw NPE for path: {}", keystorePath, npe);
            }

            // Si no lo encontramos, intentar con prefijo '/'
            if (is == null) {
                String alt = keystorePath.startsWith("/") ? keystorePath : ("/" + keystorePath);
                try {
                    is = VerifactuService.class.getResourceAsStream(alt);
                } catch (NullPointerException npe) {
                    if (log.isDebugEnabled()) log.debug("getResourceAsStream threw NPE for alt path: {}", alt, npe);
                }
            }

            // Si sigue null, intentar abrir desde sistema de ficheros
            if (is == null) {
                java.io.File f = new java.io.File(keystorePath);
                if (f.exists() && f.isFile()) {
                    is = new java.io.FileInputStream(f);
                }
            }
        } catch (Exception ex) {
            // No queremos propagar la excepción: el constructor maneja la situación y deshabilita Verifactu si hay error
            if (log.isDebugEnabled()) log.debug("Could not open keystore at '{}': {}", keystorePath, ex.getMessage(), ex);
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

        // 8.5 Generar código QR con la URL de verificación
        String qrBase64 = qrCodeService.generarQRVeriFactu(
                hash,
                empresa.getCif(),
                factura.getNumero(),
                factura.getFecha() != null ? factura.getFecha().format(DATE_FORMATTER) : "",
                factura.getTotal() != null ? factura.getTotal().toString() : "0.00"
        );
        log.info("QR generado para factura {} ({} bytes)", factura.getNumero(), qrCodeService.obtenerTamanoQR(qrBase64));

        // 8.6 Actualizar factura con hash y QR
        String hashAnterior = obtenerHashAnterior(factura.getNumero().substring(0, Math.min(4, factura.getNumero().length())));
        factura.setVerifactuHash(hash);
        factura.setVerifactuHashAnterior(hashAnterior);
        factura.setVerifactuQr(qrBase64);
        factura.setVerifactuEnviada(true);
        factura.setFechaEmisionVerifactu(LocalDateTime.now());
        factura.setEstado("EMITIDA");
        // La factura se guardará después por el controlador

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

                log.info("âœ… Factura {} enviada y registrada correctamente en AEAT", factura.getNumero());
            } catch (Exception e) {
                log.error("âŒ Error enviando factura {} a AEAT: {}", factura.getNumero(), e.getMessage());
                evidencia.setEstado("ERROR");
                evidencia.setErrorMessage("Error al enviar a AEAT: " + e.getMessage());
                evidencia.setCodigoRespuestaAEAT("ERROR");
                evidenceRepository.save(evidencia);
                throw new Exception("Error al enviar factura a AEAT: " + e.getMessage(), e);
            }
        } else {
            if (!aeatEnabled) {
                log.info("â„¹ï¸ Envío a AEAT deshabilitado (verifactu.aeat.enabled=false). Evidencia guardada localmente.");
            }
            if (!this.enabled) {
                log.info("â„¹ï¸ Certificado no disponible. Evidencia guardada localmente sin firma digital.");
            }
            log.info("âœ… Factura {} registrada localmente (modo de pruebas)", factura.getNumero());
        }
    }

    /**
     * Envía el XML de la factura a la AEAT mediante SOAP
     * Este método implementa el envío REAL a la AEAT usando el cliente SOAP
     *
     * @param xml El XML de la factura según esquema Verifactu
     * @param firma La firma digital (puede ser null si no hay certificado)
     * @return La respuesta de la AEAT
     * @throws Exception Si hay error en el envío
     */
    private String enviarXMLaAEAT(String xml, byte[] firma) throws Exception {
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("   ENVIANDO FACTURA A LA AEAT VÍA SOAP");
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("Endpoint: {}", aeatEndpoint);
        log.info("Tamaño XML: {} caracteres", xml.length());
        log.info("Firma digital: {}", firma != null ? "SÍ (" + firma.length + " bytes)" : "NO");

        try {
            if (this.aeatSoapClient == null) {
                throw new IllegalStateException("Cliente AEAT SOAP no disponible en el contexto Spring (bean ausente). Verifica la propiedad verifactu.aeat.enabled o implementa un bean fallback para dev.");
            }
            // Delegar al cliente SOAP real
            String respuesta = aeatSoapClient.enviarFacturaAeat(xml, firma);

            log.info("═══════════════════════════════════════════════════════════════");
            log.info("   ✅ RESPUESTA RECIBIDA DE LA AEAT");
            log.info("═══════════════════════════════════════════════════════════════");
            log.info("Resultado: {}", respuesta);

            return respuesta;

        } catch (Exception e) {
            log.error("❌ Error al enviar a AEAT: {}", e.getMessage());
            throw e;
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

