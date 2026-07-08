package alicanteweb.erp.service;

import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaLinea;
import alicanteweb.erp.entities.VerifactuEvidence;
import alicanteweb.erp.exception.ErpException;
import alicanteweb.erp.repository.VerifactuEvidenceRepository;
import alicanteweb.erp.util.DesgloseFiscal;
import alicanteweb.erp.util.FinancialMath;
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
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import lombok.Getter;
import org.springframework.beans.factory.ObjectProvider;
import org.xml.sax.SAXException;

@Service
public class VerifactuService implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(VerifactuService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter RECORD_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    /** XSD internos que transcriben el esquema oficial de AEAT (SuministroLR / SuministroInformacion, tikeV1.0). */
    private static final String XSD_SUMINISTRO_INFORMACION = "xsd/verifactu-suministro-informacion.xsd";
    private static final String XSD_SUMINISTRO_LR = "xsd/verifactu-suministro-lr.xsd";

    /** Namespaces oficiales del servicio VeriFactu de AEAT (tikeV1.0). */
    private static final String NS_SUMINISTRO_LR =
        "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tikeV1.0/cont/ws/SuministroLR.xsd";
    private static final String NS_SUMINISTRO_INFORMACION =
        "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tikeV1.0/cont/ws/SuministroInformacion.xsd";

    /** Versión del esquema de registro de facturación de AEAT. */
    private static final String ID_VERSION = "1.0";
    /** Código oficial de algoritmo de huella: 01 = SHA-256. */
    private static final String TIPO_HUELLA_SHA256 = "01";

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

    @Value("${verifactu.aeat.endpoint:https://www1.agenciatributaria.gob.es/wlpl/TIKE-CONT/ws/SistemaFacturacion/VerifactuSOAP}")
    private String aeatEndpoint;

    // Datos del bloque obligatorio <SistemaInformatico> (art. 9 RRSIF). Si el productor
    // del software es la propia empresa, se dejan vacíos y se usan sus datos fiscales.
    @Value("${verifactu.sistema.nombre-productor:}")
    private String sistemaNombreProductor;

    @Value("${verifactu.sistema.nif-productor:}")
    private String sistemaNifProductor;

    @Value("${verifactu.sistema.id:01}")
    private String sistemaId;

    @Value("${verifactu.sistema.numero-instalacion:1}")
    private String sistemaNumeroInstalacion;

    @Value("${verifactu.sistema.solo-verifactu:S}")
    private String sistemaSoloVerifactu;

    private final VerifactuEvidenceRepository evidenceRepository;
    private final EmpresaConfigService empresaConfigService;
    private final FacturaLineaService facturaLineaService;
    private final QrCodeService qrCodeService;
    private final VerifactuAeatSoapClient aeatSoapClient;
    private final FacturacionEventoService facturacionEventoService;
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
                           ObjectProvider<VerifactuAeatSoapClient> aeatSoapClientProvider,
                           FacturacionEventoService facturacionEventoService) {
        this.evidenceRepository = evidenceRepository;
        this.empresaConfigService = empresaConfigService;
        this.facturaLineaService = facturaLineaService;
        this.qrCodeService = qrCodeService;
        this.facturacionEventoService = facturacionEventoService;
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
            if (keystorePath != null && !keystorePath.isBlank()) {
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
     * Genera el hash SHA-256 de los datos proporcionados (Base64 URL-safe sin padding)
     */
    public String generarHash(String datos) {
        return HashUtils.sha256Base64UrlSafe(datos);
    }

    public String generarHashEncadenado(String datosFactura, String hashAnterior) {
        String datosCompletos = datosFactura;
        if (hashAnterior != null && !hashAnterior.isEmpty()) {
            datosCompletos += "|" + hashAnterior;
        }
        return generarHash(datosCompletos);
    }

    public byte[] firmarDatos(byte[] datos) {
        if (!enabled) {
            throw new IllegalStateException("VeriFactu no está habilitado - no se puede firmar");
        }
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(datos);
            return signature.sign();
        } catch (Exception e) {
            throw new ErpException("Error firmando datos con certificado VeriFactu", e);
        }
    }

    public boolean verificarFirma(byte[] datos, byte[] firma) {
        if (!enabled) {
            throw new IllegalStateException("VeriFactu no está habilitado - no se puede verificar");
        }
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(certificate.getPublicKey());
            signature.update(datos);
            return signature.verify(firma);
        } catch (Exception e) {
            throw new ErpException("Error verificando firma VeriFactu", e);
        }
    }

    public String getCertificateFingerprint() {
        if (!enabled || certificate == null) {
            return null;
        }
        try {
            byte[] certBytes = certificate.getEncoded();
            return HashUtils.sha256Base64UrlSafe(new String(certBytes, StandardCharsets.ISO_8859_1));
        } catch (Exception e) {
            throw new ErpException("Error obteniendo fingerprint del certificado", e);
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
     * Obtiene el último registro guardado de la cadena de una serie, con todos sus datos
     * (necesarios para el bloque oficial Encadenamiento/RegistroAnterior).
     */
    public Optional<VerifactuEvidence> obtenerRegistroAnterior(String serie) {
        return evidenceRepository.findFirstBySerieOrderByFechaGeneracionRegistroDescIdDesc(serie)
            .or(() -> evidenceRepository.findFirstBySerieOrderByFechaEmisionDesc(serie));
    }

    /**
     * Obtiene el hash anterior de la cadena (último registro guardado)
     */
    public String obtenerHashAnterior(String serie) {
        return obtenerRegistroAnterior(serie).map(this::huellaDe).orElse(null);
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
        if (keystorePath == null || keystorePath.isBlank()) {
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
     * Genera el XML de Verifactu para una factura usando datos de empresa_config.
     * @deprecated Mantener por compatibilidad: delega en {@link #generarRegistroAltaXml(Factura, List)},
     * que produce el registro de alta con el formato oficial de AEAT.
     */
    @Deprecated
    public String generarXMLFactura(Factura factura, List<FacturaLinea> lineas) {
        return generarRegistroAltaXml(factura, lineas);
    }

    /**
     * Genera el registro de facturación de alta con la estructura oficial de AEAT
     * (RegFactuSistemaFacturacion, esquemas SuministroLR.xsd / SuministroInformacion.xsd, tikeV1.0),
     * incluyendo encadenamiento, bloque SistemaInformatico y huella según la Orden HAC/1177/2024.
     */
    public String generarRegistroAltaXml(Factura factura, List<FacturaLinea> lineas) {
        EmpresaConfig empresa = empresaConfigService.getConfiguracionActivaOrThrow();
        Optional<VerifactuEvidence> registroAnterior = obtenerRegistroAnterior(resolverSerie(factura));
        String huellaAnterior = registroAnterior.map(this::huellaDe).orElse(null);
        String fechaHoraGeneracion = fechaHoraRegistro();
        BigDecimal cuotaTotal = factura.getTotalIva() != null ? factura.getTotalIva() : BigDecimal.ZERO;
        BigDecimal importeTotal = factura.getTotal() != null ? factura.getTotal() : BigDecimal.ZERO;

        try {
            String nifEmisor = valorSeguro(empresa.getVerifactuNifEmisor()).trim();
            String tipoFactura = tipoFacturaOficial(factura);
            String huella = generarHuellaRegistroAlta(empresa, factura, cuotaTotal, importeTotal, huellaAnterior, fechaHoraGeneracion);

            StringBuilder xml = new StringBuilder();
            xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            appendAperturaEnvoltorio(xml, empresa);
            xml.append("  <sum:RegistroFactura>\n");
            xml.append("    <sum1:RegistroAlta>\n");
            xml.append("      <sum1:IDVersion>").append(ID_VERSION).append("</sum1:IDVersion>\n");
            xml.append("      <sum1:IDFactura>\n");
            xml.append("        <sum1:IDEmisorFactura>").append(escapeXml(nifEmisor)).append("</sum1:IDEmisorFactura>\n");
            xml.append("        <sum1:NumSerieFactura>").append(escapeXml(factura.getNumero())).append("</sum1:NumSerieFactura>\n");
            xml.append("        <sum1:FechaExpedicionFactura>").append(formatearFecha(factura.getFecha())).append("</sum1:FechaExpedicionFactura>\n");
            xml.append("      </sum1:IDFactura>\n");
            xml.append("      <sum1:NombreRazonEmisor>").append(escapeXml(empresa.getNombreEmpresa())).append("</sum1:NombreRazonEmisor>\n");
            xml.append("      <sum1:TipoFactura>").append(tipoFactura).append("</sum1:TipoFactura>\n");
            appendRectificativa(xml, factura, nifEmisor);
            xml.append("      <sum1:DescripcionOperacion>")
               .append(escapeXml(descripcionOperacion(factura)))
               .append("</sum1:DescripcionOperacion>\n");
            appendDestinatarios(xml, factura);
            appendDesgloseOficial(xml, factura, lineas);
            xml.append("      <sum1:CuotaTotal>").append(normalizarImporte(cuotaTotal)).append("</sum1:CuotaTotal>\n");
            xml.append("      <sum1:ImporteTotal>").append(normalizarImporte(importeTotal)).append("</sum1:ImporteTotal>\n");
            appendEncadenamiento(xml, registroAnterior.orElse(null));
            appendSistemaInformatico(xml, empresa);
            xml.append("      <sum1:FechaHoraHusoGenRegistro>").append(escapeXml(fechaHoraGeneracion)).append("</sum1:FechaHoraHusoGenRegistro>\n");
            xml.append("      <sum1:TipoHuella>").append(TIPO_HUELLA_SHA256).append("</sum1:TipoHuella>\n");
            xml.append("      <sum1:Huella>").append(huella).append("</sum1:Huella>\n");
            xml.append("    </sum1:RegistroAlta>\n");
            xml.append("  </sum:RegistroFactura>\n");
            xml.append("</sum:RegFactuSistemaFacturacion>\n");

            String xmlGenerado = xml.toString();
            validarXmlRegistroFacturacion(xmlGenerado);
            return xmlGenerado;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el registro de alta VeriFactu", e);
        }
    }

    /**
     * Genera el registro de facturación de anulación con la estructura oficial de AEAT.
     * El motivo no forma parte del registro oficial; se conserva en los metadatos de la evidencia.
     */
    public String generarRegistroAnulacionXml(Factura factura, String motivo) {
        EmpresaConfig empresa = empresaConfigService.getConfiguracionActivaOrThrow();
        Optional<VerifactuEvidence> registroAnterior = obtenerRegistroAnterior(resolverSerie(factura));
        String huellaAnterior = registroAnterior.map(this::huellaDe).orElse(null);
        String fechaHoraGeneracion = fechaHoraRegistro();

        try {
            String nifEmisor = valorSeguro(empresa.getVerifactuNifEmisor()).trim();
            String huella = generarHuellaRegistroAnulacion(empresa, factura, huellaAnterior, fechaHoraGeneracion);

            StringBuilder xml = new StringBuilder();
            xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            appendAperturaEnvoltorio(xml, empresa);
            xml.append("  <sum:RegistroFactura>\n");
            xml.append("    <sum1:RegistroAnulacion>\n");
            xml.append("      <sum1:IDVersion>").append(ID_VERSION).append("</sum1:IDVersion>\n");
            xml.append("      <sum1:IDFacturaAnulada>\n");
            xml.append("        <sum1:IDEmisorFacturaAnulada>").append(escapeXml(nifEmisor)).append("</sum1:IDEmisorFacturaAnulada>\n");
            xml.append("        <sum1:NumSerieFacturaAnulada>").append(escapeXml(factura.getNumero())).append("</sum1:NumSerieFacturaAnulada>\n");
            xml.append("        <sum1:FechaExpedicionFacturaAnulada>").append(formatearFecha(factura.getFecha())).append("</sum1:FechaExpedicionFacturaAnulada>\n");
            xml.append("      </sum1:IDFacturaAnulada>\n");
            appendEncadenamiento(xml, registroAnterior.orElse(null));
            appendSistemaInformatico(xml, empresa);
            xml.append("      <sum1:FechaHoraHusoGenRegistro>").append(escapeXml(fechaHoraGeneracion)).append("</sum1:FechaHoraHusoGenRegistro>\n");
            xml.append("      <sum1:TipoHuella>").append(TIPO_HUELLA_SHA256).append("</sum1:TipoHuella>\n");
            xml.append("      <sum1:Huella>").append(huella).append("</sum1:Huella>\n");
            xml.append("    </sum1:RegistroAnulacion>\n");
            xml.append("  </sum:RegistroFactura>\n");
            xml.append("</sum:RegFactuSistemaFacturacion>\n");

            String xmlGenerado = xml.toString();
            validarXmlRegistroFacturacion(xmlGenerado);
            return xmlGenerado;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el registro de anulacion VeriFactu", e);
        }
    }

    /**
     * Envía una factura a Verifactu/AEAT con todas las validaciones
     */
    public void enviarFacturaVerifactu(Factura factura) {
        log.info("Iniciando envío de factura {} a Verifactu", factura.getNumero());

        // 1. Verificar que existe configuración de empresa
        if (!empresaConfigService.existeConfiguracionActiva()) {
            throw new IllegalStateException("Configure los datos de empresa antes de enviar facturas a Verifactu");
        }

        EmpresaConfig empresa = empresaConfigService.getConfiguracionActivaOrThrow();

        // 2. Verificar que Verifactu está habilitado en la empresa
        if (!empresaConfigService.isFuncionamientoVerifactuVigente(empresa)) {
            throw new IllegalStateException("La empresa no esta en funcionamiento VERI*FACTU vigente. Debe iniciarse desde la pantalla VERI*FACTU.");
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
        String xml = generarRegistroAltaXml(factura, lineas);
        log.info("XML generado para factura {}: {} caracteres", factura.getNumero(), xml.length());

        // 8. La huella oficial (Orden HAC/1177/2024) viaja dentro del registro; se extrae para
        // conservarla en la evidencia y encadenar el siguiente registro. Firmar si está habilitado.
        String huellaXml = extraerValorXml(xml, "Huella");
        String hash = huellaXml != null && !huellaXml.isBlank() ? huellaXml : generarHash(xml);
        byte[] firma = null;
        if (this.enabled) {
            firma = firmarDatos(xml.getBytes(StandardCharsets.UTF_8));
            log.info("Factura {} firmada digitalmente", factura.getNumero());
        }

        // 8.5 Generar el QR tributario con la URL oficial de cotejo (nif, numserie, fecha, importe)
        String nifQr = empresa.getVerifactuNifEmisor() != null && !empresa.getVerifactuNifEmisor().isBlank()
                ? empresa.getVerifactuNifEmisor().trim() : empresa.getCif();
        String qrBase64 = qrCodeService.generarQRVeriFactu(
                nifQr,
                factura.getNumero(),
                factura.getFecha() != null ? factura.getFecha().format(DATE_FORMATTER) : "",
                factura.getTotal() != null ? factura.getTotal().setScale(FinancialMath.SCALE, FinancialMath.ROUND).toPlainString() : "0.00"
        );
        log.info("QR generado para factura {} ({} bytes)", factura.getNumero(), qrCodeService.obtenerTamanoQR(qrBase64));

        // 8.6 Preparar metadatos de la factura (hash, QR) — el estado se actualiza solo tras confirmación
        String hashAnterior = obtenerHashAnterior(resolverSerie(factura));
        factura.setVerifactuHash(hash);
        factura.setVerifactuHashAnterior(hashAnterior);
        factura.setVerifactuQr(qrBase64);

        // 9. Guardar evidencia en estado PENDIENTE antes de intentar el envío
        VerifactuEvidence evidencia = new VerifactuEvidence();
        evidencia.setSerie(resolverSerie(factura));
        evidencia.setNumero(factura.getNumero());
        evidencia.setFacturaId(factura.getId().toString());
        evidencia.setCreatedAt(java.time.Instant.now());
        evidencia.setFechaEmision(factura.getFecha() != null ?
            factura.getFecha().atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant() :
            java.time.Instant.now());
        evidencia.setHash(hash);
        evidencia.setHashAnterior(hashAnterior);
        evidencia.setTipoRegistro("ALTA");
        evidencia.setXmlGenerado(xml);
        evidencia.setFechaGeneracionRegistro(java.time.Instant.now());
        evidencia.setHuellaRegistro(hash);
        evidencia.setNifEmisor(empresa.getVerifactuNifEmisor());
        evidencia.setFechaExpedicionFactura(factura.getFecha());
        if (firma != null) {
            evidencia.setSignature(firma);
        }
        evidencia.setEstado("PENDIENTE");

        java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put("empresa", empresa.getNombreEmpresa());
        metadata.put("cif", empresa.getCif());
        metadata.put("xml_length", xml.length());
        evidencia.setMetadata(metadata);

        evidenceRepository.save(evidencia);
        log.info("Evidencia guardada para factura {} con ID {}", factura.getNumero(), evidencia.getId());

        // 10. Envío real a la AEAT (si está habilitado) — la factura solo pasa a EMITIDA si esto va bien
        if (aeatEnabled && this.enabled) {
            try {
                log.info("Enviando factura {} a AEAT endpoint: {}", factura.getNumero(), aeatEndpoint);
                String respuestaAEAT = enviarXMLaAEAT(xml, firma);

                evidencia.setEstado("ENVIADO");
                evidencia.setFechaEnvio(java.time.Instant.now());
                evidencia.setCodigoRespuestaAEAT(respuestaAEAT != null ? "OK" : "ERROR");
                metadata.put("respuesta_aeat", respuestaAEAT);
                metadata.put("fecha_envio_real", java.time.Instant.now().toString());
                evidencia.setMetadata(metadata);
                evidenceRepository.save(evidencia);

                // Solo aquí, con AEAT confirmada, marcamos la factura como emitida
                factura.setVerifactuEnviada(true);
                factura.setFechaEmisionVerifactu(LocalDateTime.now());
                factura.setEstado("EMITIDA");

                registrarEventoVerifactu("ENVIO_AEAT_OK", factura, java.util.Map.of(
                    "evidenciaId", evidencia.getId(),
                    "codigoRespuesta", evidencia.getCodigoRespuestaAEAT(),
                    "endpoint", aeatEndpoint
                ));
                log.info("Factura {} enviada y registrada correctamente en AEAT", factura.getNumero());
            } catch (Exception e) {
                log.error("Error enviando factura {} a AEAT: {}", factura.getNumero(), e.getMessage());
                evidencia.setEstado("ERROR");
                evidencia.setErrorMessage("Error al enviar a AEAT: " + e.getMessage());
                evidencia.setCodigoRespuestaAEAT("ERROR");
                evidenceRepository.save(evidencia);
                registrarEventoVerifactu("ENVIO_AEAT_ERROR", factura, java.util.Map.of(
                    "evidenciaId", evidencia.getId(),
                    "mensajeError", e.getMessage(),
                    "endpoint", aeatEndpoint
                ));
                // La factura NO cambia de estado: el controlador verá la excepción y no guardará EMITIDA
                throw new ErpException("Error al enviar factura a AEAT: " + e.getMessage(), e);
            }
        } else {
            // Sin envío AEAT (modo local/pruebas): se marca como emitida igualmente
            evidencia.setEstado("ENVIADO");
            evidencia.setFechaEnvio(java.time.Instant.now());
            evidenceRepository.save(evidencia);

            factura.setVerifactuEnviada(true);
            factura.setFechaEmisionVerifactu(LocalDateTime.now());
            factura.setEstado("EMITIDA");

            if (!aeatEnabled) {
                log.info("Envio a AEAT deshabilitado (verifactu.aeat.enabled=false). Evidencia guardada localmente.");
            }
            if (!this.enabled) {
                log.info("Certificado no disponible. Evidencia guardada localmente sin firma digital.");
            }
            registrarEventoVerifactu("ENVIO_AEAT_OMITIDO", factura, java.util.Map.of(
                "aeatEnabled", aeatEnabled,
                "certificadoDisponible", this.enabled
            ));
            log.info("Factura {} registrada localmente (modo de pruebas)", factura.getNumero());
        }

        registrarEventoVerifactu("REGISTRO_VERIFACTU_LOCAL", factura, java.util.Map.of(
            "evidenciaId", evidencia.getId(),
            "serie", evidencia.getSerie(),
            "hash", hash
        ));
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
    private String enviarXMLaAEAT(String xml, byte[] firma) {
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
            log.info("   [OK] RESPUESTA RECIBIDA DE LA AEAT");
            log.info("═══════════════════════════════════════════════════════════════");
            log.info("Resultado: {}", respuesta);

            return respuesta;

        } catch (ErpException e) {
            log.error("[ERROR] Error al enviar a AEAT: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[ERROR] Error al enviar a AEAT: {}", e.getMessage());
            throw new ErpException("Error de comunicación con AEAT", e);
        }
    }


    public void registrarAnulacionLocal(Factura factura, String motivo) {
        if (factura == null || factura.getId() == null) {
            return;
        }

        Optional<EmpresaConfig> empresaOpt = empresaConfigService.getConfiguracionActiva();
        if (empresaOpt.isEmpty()) {
            log.warn("No hay configuración de empresa — omitiendo registro de anulación VeriFactu para {}", factura.getNumero());
            return;
        }
        EmpresaConfig empresa = empresaOpt.get();

        String xml = null;
        String hashDocumento;
        boolean verifactuConfigurado = empresa.getVerifactuNifEmisor() != null && !empresa.getVerifactuNifEmisor().isBlank();
        if (verifactuConfigurado) {
            try {
                xml = generarRegistroAnulacionXml(factura, motivo);
            } catch (Exception e) {
                log.warn("No se pudo generar XML VeriFactu para anulación de {} — se registra evidencia sin XML: {}", factura.getNumero(), e.getMessage());
            }
        }
        if (xml != null) {
            String huellaXml = extraerValorXml(xml, "Huella");
            hashDocumento = huellaXml != null && !huellaXml.isBlank() ? huellaXml : generarHash(xml);
        } else {
            hashDocumento = generarHash(factura.getNumero() + "|ANULACION|FALLBACK");
        }

        VerifactuEvidence evidencia = new VerifactuEvidence();
        evidencia.setSerie(resolverSerie(factura));
        evidencia.setNumero(factura.getNumero());
        evidencia.setFacturaId(factura.getId().toString());
        evidencia.setCreatedAt(java.time.Instant.now());
        evidencia.setFechaEmision(factura.getFecha() != null
            ? factura.getFecha().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
            : java.time.Instant.now());
        evidencia.setHash(hashDocumento);
        evidencia.setHashAnterior(obtenerHashAnterior(evidencia.getSerie()));
        evidencia.setTipoRegistro("ANULACION");
        evidencia.setXmlGenerado(xml);
        evidencia.setFechaGeneracionRegistro(java.time.Instant.now());
        evidencia.setHuellaRegistro(hashDocumento);
        evidencia.setNifEmisor(empresa.getVerifactuNifEmisor());
        evidencia.setFechaExpedicionFactura(factura.getFecha());
        evidencia.setEstado("ANULADO");

        java.util.Map<String, Object> metadata = evidencia.getMetadata() != null
            ? new java.util.HashMap<>(evidencia.getMetadata())
            : new java.util.HashMap<>();
        metadata.put("motivoAnulacion", motivo);
        metadata.put("tipoRegistro", "ANULACION");
        evidencia.setMetadata(metadata);

        VerifactuEvidence saved = evidenceRepository.save(evidencia);
        registrarEventoVerifactu("REGISTRO_ANULACION_LOCAL", factura, java.util.Map.of(
            "evidenciaId", saved.getId(),
            "motivo", motivo != null ? motivo : "",
            "tipoRegistro", "ANULACION"
        ));
    }

    /** Abre el envoltorio oficial RegFactuSistemaFacturacion con la cabecera del obligado a emitir. */
    private void appendAperturaEnvoltorio(StringBuilder xml, EmpresaConfig empresa) {
        xml.append("<sum:RegFactuSistemaFacturacion xmlns:sum=\"").append(NS_SUMINISTRO_LR)
           .append("\" xmlns:sum1=\"").append(NS_SUMINISTRO_INFORMACION).append("\">\n");
        xml.append("  <sum:Cabecera>\n");
        xml.append("    <sum1:ObligadoEmision>\n");
        xml.append("      <sum1:NombreRazon>").append(escapeXml(empresa.getNombreEmpresa())).append("</sum1:NombreRazon>\n");
        xml.append("      <sum1:NIF>").append(escapeXml(valorSeguro(empresa.getVerifactuNifEmisor()).trim())).append("</sum1:NIF>\n");
        xml.append("    </sum1:ObligadoEmision>\n");
        xml.append("  </sum:Cabecera>\n");
    }

    /** Bloque de rectificativa: tipo (S/I) y referencia a la factura rectificada, si consta. */
    private void appendRectificativa(StringBuilder xml, Factura factura, String nifEmisor) {
        if (!esRectificativa(factura)) return;
        xml.append("      <sum1:TipoRectificativa>").append(tipoRectificativaOficial(factura.getTipoRectificacion())).append("</sum1:TipoRectificativa>\n");
        if (factura.getFacturaRectificadaNumero() != null && !factura.getFacturaRectificadaNumero().isBlank()) {
            xml.append("      <sum1:FacturasRectificadas>\n");
            xml.append("        <sum1:IDFacturaRectificada>\n");
            xml.append("          <sum1:IDEmisorFactura>").append(escapeXml(nifEmisor)).append("</sum1:IDEmisorFactura>\n");
            xml.append("          <sum1:NumSerieFactura>").append(escapeXml(factura.getFacturaRectificadaNumero())).append("</sum1:NumSerieFactura>\n");
            xml.append("          <sum1:FechaExpedicionFactura>").append(formatearFecha(factura.getFacturaRectificadaFecha())).append("</sum1:FechaExpedicionFactura>\n");
            xml.append("        </sum1:IDFacturaRectificada>\n");
            xml.append("      </sum1:FacturasRectificadas>\n");
        }
    }

    private void appendDestinatarios(StringBuilder xml, Factura factura) {
        if (factura.getCliente() == null) return;
        xml.append("      <sum1:Destinatarios>\n");
        xml.append("        <sum1:IDDestinatario>\n");
        xml.append("          <sum1:NombreRazon>").append(escapeXml(factura.getCliente().getNombre())).append("</sum1:NombreRazon>\n");
        if (factura.getCliente().getCif() != null && !factura.getCliente().getCif().isEmpty()) {
            xml.append("          <sum1:NIF>").append(escapeXml(factura.getCliente().getCif())).append("</sum1:NIF>\n");
        }
        xml.append("        </sum1:IDDestinatario>\n");
        xml.append("      </sum1:Destinatarios>\n");
    }

    /**
     * Desglose oficial agrupado por tipo impositivo: un DetalleDesglose por cada tipo de IVA.
     * Impuesto 01 = IVA; ClaveRegimen 01 = régimen general; CalificacionOperacion S1 = sujeta y no exenta.
     *
     * <p>Usa la misma fuente única {@link DesgloseFiscal} que los totales de la factura y que la
     * impresión (incluye el descuento global prorrateado y redondea la cuota por tipo), de modo
     * que la suma del desglose coincide siempre con la BaseImponible/CuotaTotal declaradas y
     * firmadas — imprescindible para que el registro remitido a la AEAT sea consistente.
     */
    private void appendDesgloseOficial(StringBuilder xml, Factura factura, List<FacturaLinea> lineas) {
        xml.append("      <sum1:Desglose>\n");
        List<DesgloseFiscal.Linea> calculo = new java.util.ArrayList<>();
        if (lineas != null) {
            for (FacturaLinea linea : lineas) {
                if (linea.getCantidad() == null || linea.getPrecioUnitario() == null) continue;
                calculo.add(new DesgloseFiscal.Linea(linea.getCantidad(), linea.getPrecioUnitario(),
                        linea.getIva(), linea.getDescuentoTipo(), linea.getDescuento()));
            }
        }
        DesgloseFiscal.Resultado resultado = DesgloseFiscal.calcular(
                calculo, factura.getDescuentoGlobalTipo(), factura.getDescuentoGlobalValor());
        for (DesgloseFiscal.TipoIva t : resultado.porTipo()) {
            xml.append("        <sum1:DetalleDesglose>\n");
            xml.append("          <sum1:Impuesto>01</sum1:Impuesto>\n");
            xml.append("          <sum1:ClaveRegimen>01</sum1:ClaveRegimen>\n");
            xml.append("          <sum1:CalificacionOperacion>S1</sum1:CalificacionOperacion>\n");
            xml.append("          <sum1:TipoImpositivo>")
               .append(t.tipo().setScale(FinancialMath.SCALE, FinancialMath.ROUND).toPlainString())
               .append("</sum1:TipoImpositivo>\n");
            xml.append("          <sum1:BaseImponibleOimporteNoSujeto>").append(normalizarImporte(t.base())).append("</sum1:BaseImponibleOimporteNoSujeto>\n");
            xml.append("          <sum1:CuotaRepercutida>").append(normalizarImporte(t.cuota())).append("</sum1:CuotaRepercutida>\n");
            xml.append("        </sum1:DetalleDesglose>\n");
        }
        xml.append("      </sum1:Desglose>\n");
    }

    /** Encadenamiento oficial: primer registro de la cadena o referencia al registro anterior. */
    private void appendEncadenamiento(StringBuilder xml, VerifactuEvidence anterior) {
        xml.append("      <sum1:Encadenamiento>\n");
        if (anterior == null) {
            xml.append("        <sum1:PrimerRegistro>S</sum1:PrimerRegistro>\n");
        } else {
            xml.append("        <sum1:RegistroAnterior>\n");
            xml.append("          <sum1:IDEmisorFactura>").append(escapeXml(valorSeguro(anterior.getNifEmisor()).trim())).append("</sum1:IDEmisorFactura>\n");
            xml.append("          <sum1:NumSerieFactura>").append(escapeXml(anterior.getNumero())).append("</sum1:NumSerieFactura>\n");
            xml.append("          <sum1:FechaExpedicionFactura>").append(formatearFecha(anterior.getFechaExpedicionFactura())).append("</sum1:FechaExpedicionFactura>\n");
            xml.append("          <sum1:Huella>").append(escapeXml(huellaDe(anterior))).append("</sum1:Huella>\n");
            xml.append("        </sum1:RegistroAnterior>\n");
        }
        xml.append("      </sum1:Encadenamiento>\n");
    }

    /** Bloque obligatorio con los datos del sistema informático de facturación (art. 9 RRSIF). */
    private void appendSistemaInformatico(StringBuilder xml, EmpresaConfig empresa) {
        // Prioridad: datos de productor configurados en empresa (pantalla Cumplimiento),
        // después properties verifactu.sistema.*, y por último los datos fiscales de la empresa
        String nombreProductor = primerValor(empresa.getProductorSoftware(), sistemaNombreProductor, empresa.getNombreEmpresa());
        String nifProductor = primerValor(empresa.getNifProductorSoftware(), sistemaNifProductor,
                valorSeguro(empresa.getVerifactuNifEmisor()).trim());
        String nombreSistema = empresa.getVerifactuNombreSistema() != null && !empresa.getVerifactuNombreSistema().isBlank()
                ? empresa.getVerifactuNombreSistema() : "ERP Tahona";
        String version = empresa.getVerifactuVersionSistema() != null && !empresa.getVerifactuVersionSistema().isBlank()
                ? empresa.getVerifactuVersionSistema() : ID_VERSION;
        String numeroInstalacion = empresa.getVerifactuIdDispositivo() != null && !empresa.getVerifactuIdDispositivo().isBlank()
                ? empresa.getVerifactuIdDispositivo() : sistemaNumeroInstalacion;

        xml.append("      <sum1:SistemaInformatico>\n");
        xml.append("        <sum1:NombreRazon>").append(escapeXml(nombreProductor)).append("</sum1:NombreRazon>\n");
        xml.append("        <sum1:NIF>").append(escapeXml(nifProductor)).append("</sum1:NIF>\n");
        xml.append("        <sum1:NombreSistemaInformatico>").append(escapeXml(nombreSistema)).append("</sum1:NombreSistemaInformatico>\n");
        xml.append("        <sum1:IdSistemaInformatico>").append(escapeXml(sistemaId)).append("</sum1:IdSistemaInformatico>\n");
        xml.append("        <sum1:Version>").append(escapeXml(version)).append("</sum1:Version>\n");
        xml.append("        <sum1:NumeroInstalacion>").append(escapeXml(numeroInstalacion)).append("</sum1:NumeroInstalacion>\n");
        xml.append("        <sum1:TipoUsoPosibleSoloVerifactu>").append(escapeXml(sistemaSoloVerifactu)).append("</sum1:TipoUsoPosibleSoloVerifactu>\n");
        xml.append("        <sum1:TipoUsoPosibleMultiOT>N</sum1:TipoUsoPosibleMultiOT>\n");
        xml.append("        <sum1:IndicadorMultiplesOT>N</sum1:IndicadorMultiplesOT>\n");
        xml.append("      </sum1:SistemaInformatico>\n");
    }

    /**
     * Mapea el tipo interno de factura a los códigos oficiales de AEAT:
     * F1 completa, F2 simplificada, R4 rectificativa (resto de causas del art. 80 LIVA),
     * R5 rectificativa de factura simplificada.
     */
    String tipoFacturaOficial(Factura factura) {
        String tipo = factura.getTipoFactura();
        if ("RECTIFICATIVA".equalsIgnoreCase(tipo)) {
            return "R4";
        }
        if ("SIMPLIFICADA".equalsIgnoreCase(tipo)) {
            return "F2";
        }
        return "F1";
    }

    /** S = rectificación por sustitución, I = por diferencias. */
    String tipoRectificativaOficial(String tipoRectificacion) {
        return "DIFERENCIAS".equalsIgnoreCase(tipoRectificacion) ? "I" : "S";
    }

    private boolean esRectificativa(Factura factura) {
        return "RECTIFICATIVA".equalsIgnoreCase(factura.getTipoFactura());
    }

    private String descripcionOperacion(Factura factura) {
        if (esRectificativa(factura) && factura.getFacturaRectificadaNumero() != null) {
            return "Rectificación de la factura " + factura.getFacturaRectificadaNumero();
        }
        return "Venta de productos de panadería y obrador";
    }

    private String formatearFecha(java.time.LocalDate fecha) {
        return fecha != null ? fecha.format(DATE_FORMATTER) : "";
    }

    private String huellaDe(VerifactuEvidence evidencia) {
        return evidencia.getHuellaRegistro() != null && !evidencia.getHuellaRegistro().isBlank()
                ? evidencia.getHuellaRegistro() : evidencia.getHash();
    }

    private String fechaHoraRegistro() {
        return java.time.ZonedDateTime.now(ZoneId.systemDefault()).format(RECORD_DATETIME_FORMATTER);
    }

    /**
     * Huella del registro de alta según la fórmula oficial de la Orden HAC/1177/2024 (anexo I):
     * concatenación {@code campo=valor} separada por '&', SHA-256 sobre UTF-8, hexadecimal en mayúsculas.
     * Si es el primer registro de la cadena, el campo Huella va vacío.
     */
    private String generarHuellaRegistroAlta(EmpresaConfig empresa,
                                             Factura factura,
                                             BigDecimal cuotaTotal,
                                             BigDecimal importeTotal,
                                             String huellaAnterior,
                                             String fechaHoraGeneracion) {
        String entrada = "IDEmisorFactura=" + valorSeguro(empresa.getVerifactuNifEmisor()).trim()
                + "&NumSerieFacturaEmisor=" + valorSeguro(factura.getNumero()).trim()
                + "&FechaExpedicionFacturaEmisor=" + formatearFecha(factura.getFecha())
                + "&TipoFactura=" + tipoFacturaOficial(factura)
                + "&CuotaTotal=" + normalizarImporte(cuotaTotal)
                + "&ImporteTotal=" + normalizarImporte(importeTotal)
                + "&Huella=" + valorSeguro(huellaAnterior).trim()
                + "&FechaHoraHusoGenRegistro=" + valorSeguro(fechaHoraGeneracion).trim();
        return HashUtils.sha256Hex(entrada).toUpperCase(java.util.Locale.ROOT);
    }

    /**
     * Huella del registro de anulación según la fórmula oficial de la Orden HAC/1177/2024 (anexo I).
     */
    private String generarHuellaRegistroAnulacion(EmpresaConfig empresa,
                                                  Factura factura,
                                                  String huellaAnterior,
                                                  String fechaHoraGeneracion) {
        String entrada = "IDEmisorFacturaAnulada=" + valorSeguro(empresa.getVerifactuNifEmisor()).trim()
                + "&NumSerieFacturaAnulada=" + valorSeguro(factura.getNumero()).trim()
                + "&FechaExpedicionFacturaAnulada=" + formatearFecha(factura.getFecha())
                + "&Huella=" + valorSeguro(huellaAnterior).trim()
                + "&FechaHoraHusoGenRegistro=" + valorSeguro(fechaHoraGeneracion).trim();
        return HashUtils.sha256Hex(entrada).toUpperCase(java.util.Locale.ROOT);
    }

    private void validarXmlRegistroFacturacion(String xml) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try (InputStream sumInfoStream = cl.getResourceAsStream(XSD_SUMINISTRO_INFORMACION);
             InputStream sumLrStream = cl.getResourceAsStream(XSD_SUMINISTRO_LR)) {
            if (sumInfoStream == null || sumLrStream == null) {
                throw new IllegalStateException("No se encontraron los esquemas XSD internos de VeriFactu");
            }

            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            // El orden importa: primero el esquema base (SuministroInformacion) y luego el que lo importa
            Schema schema = factory.newSchema(new Source[]{
                new StreamSource(sumInfoStream),
                new StreamSource(sumLrStream)
            });
            Source source = new StreamSource(new StringReader(xml));
            schema.newValidator().validate(source);
        } catch (SAXException | IOException e) {
            throw new IllegalStateException("El XML de registro de facturacion no supera la validacion estructural: " + e.getMessage(), e);
        }
    }

    private String extraerValorXml(String xml, String tag) {
        try {
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setNamespaceAware(true);
            org.w3c.dom.Document doc = factory.newDocumentBuilder()
                    .parse(new org.xml.sax.InputSource(new java.io.StringReader(xml)));
            org.w3c.dom.NodeList nodes = doc.getElementsByTagNameNS("*", tag);
            if (nodes.getLength() == 0) {
                nodes = doc.getElementsByTagName(tag);
            }
            // Última aparición: la Huella del propio registro va al final; la primera puede ser
            // la del RegistroAnterior dentro del bloque Encadenamiento
            return nodes.getLength() > 0 ? nodes.item(nodes.getLength() - 1).getTextContent().trim() : null;
        } catch (Exception e) {
            log.warn("No se pudo extraer <{}> del XML de Verifactu: {}", tag, e.getMessage());
            return null;
        }
    }

    private String valorSeguro(String valor) {
        return valor != null ? valor : "";
    }

    /** Devuelve el primer valor no vacío de la lista. */
    private String primerValor(String... valores) {
        for (String v : valores) {
            if (v != null && !v.isBlank()) return v;
        }
        return "";
    }

    private String normalizarImporte(BigDecimal importe) {
        return (importe != null ? importe : BigDecimal.ZERO).setScale(FinancialMath.SCALE, FinancialMath.ROUND).toPlainString();
    }

    /**
     * Resuelve la serie de una factura para Verifactu.
     * Usa el campo {@code serie} de la entidad si está relleno; si no, extrae
     * los primeros 4 caracteres del número de factura como fallback.
     */
    private String resolverSerie(Factura factura) {
        if (factura.getSerie() != null && !factura.getSerie().isBlank()) {
            return factura.getSerie();
        }
        String num = factura.getNumero();
        if (num == null || num.isBlank()) return "GEN";
        return num.substring(0, Math.min(4, num.length()));
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

    private void registrarEventoVerifactu(String tipoEvento, Factura factura, java.util.Map<String, Object> metadata) {
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        if (metadata != null) {
            payload.putAll(metadata);
        }
        payload.put("facturaId", factura.getId());
        payload.put("numero", factura.getNumero());
        payload.put("serie", resolverSerie(factura));
        facturacionEventoService.registrarEvento(
            FacturacionEventoService.AMBITO_FACTURAS,
            tipoEvento,
            factura.getNumero(),
            payload
        );
    }
}

