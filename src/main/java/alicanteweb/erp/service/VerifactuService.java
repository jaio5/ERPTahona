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
    private final QrCodeService qrCodeService;
    private final KeyStore keyStore;
    private final PrivateKey privateKey;
    private final X509Certificate certificate;
    private final boolean enabled;

    public VerifactuService(VerifactuEvidenceRepository evidenceRepository,
                           EmpresaConfigService empresaConfigService,
                           FacturaLineaService facturaLineaService,
                           QrCodeService qrCodeService) {
        this.evidenceRepository = evidenceRepository;
        this.empresaConfigService = empresaConfigService;
        this.facturaLineaService = facturaLineaService;
        this.qrCodeService = qrCodeService;
        KeyStore ks = null;
        PrivateKey pk = null;
        X509Certificate cert = null;
        boolean ok = false;

        try (InputStream is = openKeystoreStream(keystorePath)) {
            if (is == null) {
                log.warn("Keystore no encontrado en {} (classpath o disco) â€” verifactu deshabilitado", keystorePath);
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
            log.warn("Error cargando el keystore para verifactu â€” verifactu deshabilitado: {}", e.getMessage());
            if (log.isDebugEnabled()) log.debug("Stack:", e);
        }

        this.keyStore = ks;
        this.privateKey = pk;
        this.certificate = cert;
        this.enabled = ok;

        if (this.enabled) {
            log.info("Verifactu inicializado usando keystore {}", keystorePath);
        } else {
            log.info("Verifactu deshabilitado â€” la aplicación continuará sin registrar evidencias");
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
     * Envía el XML de la factura a la AEAT mediante HTTP POST
     * Este método implementa el envío REAL a la AEAT usando el protocolo oficial
     *
     * @param xml El XML de la factura según esquema Verifactu
     * @param firma La firma digital (puede ser null si no hay certificado)
     * @return La respuesta de la AEAT en formato XML o JSON
     * @throws Exception Si hay error en el envío
     */
    private String enviarXMLaAEAT(String xml, byte[] firma) throws Exception {
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("   INICIANDO ENVÍO REAL A LA AEAT");
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("Endpoint: {}", aeatEndpoint);
        log.info("Tamaño XML: {} caracteres", xml.length());
        log.info("Firma digital: {}", firma != null ? "SÍ (" + firma.length + " bytes)" : "NO (sin certificado)");

        try {
            // Crear cliente HTTP con soporte SSL/TLS para AEAT
            javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLSv1.2");

            // Si tenemos certificado, configurar SSL con el certificado
            if (this.enabled && this.keyStore != null) {
                log.info("Configurando SSL con certificado del keystore...");

                // Crear KeyManagerFactory con nuestro keystore
                javax.net.ssl.KeyManagerFactory kmf =
                    javax.net.ssl.KeyManagerFactory.getInstance(
                        javax.net.ssl.KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(this.keyStore, this.keyPassword.toCharArray());

                // Crear TrustManagerFactory para validar certificado de la AEAT
                javax.net.ssl.TrustManagerFactory tmf =
                    javax.net.ssl.TrustManagerFactory.getInstance(
                        javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm());
                tmf.init((KeyStore) null); // Usa el truststore por defecto del JRE

                sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new java.security.SecureRandom());
                log.info("✅ SSL configurado correctamente");
            } else {
                log.warn("⚠️  SSL sin certificado cliente (solo validará servidor AEAT)");
                sslContext.init(null, null, null);
            }

            // Crear cliente HTTP con configuración SSL
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                .version(java.net.http.HttpClient.Version.HTTP_1_1)
                .connectTimeout(java.time.Duration.ofSeconds(30))
                .sslContext(sslContext)
                .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                .build();

            // Preparar el body según el protocolo SOAP de la AEAT
            // La AEAT espera un mensaje SOAP con el XML embebido
            String soapEnvelope = construirMensajeSOAP(xml, firma);

            log.info("Mensaje SOAP preparado: {} caracteres", soapEnvelope.length());
            if (log.isDebugEnabled()) {
                log.debug("Mensaje SOAP completo:\n{}", soapEnvelope);
            }

            // Crear request HTTP POST con el mensaje SOAP
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(aeatEndpoint))
                .timeout(java.time.Duration.ofSeconds(90)) // AEAT puede tardar
                .header("Content-Type", "text/xml; charset=UTF-8")
                .header("SOAPAction", "\"http://www2.agenciatributaria.gob.es/hal/verifactu/EnviarFactura\"")
                .header("Accept", "text/xml, application/xml")
                .header("User-Agent", "ERP-Tahona/1.0 (GRUPO BABO)")
                .header("Connection", "keep-alive")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(soapEnvelope, StandardCharsets.UTF_8))
                .build();

            log.info("Enviando request a la AEAT...");

            // Enviar request y obtener respuesta
            java.net.http.HttpResponse<String> response = client.send(request,
                java.net.http.HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            // Procesar respuesta
            int statusCode = response.statusCode();
            String responseBody = response.body();

            log.info("═══════════════════════════════════════════════════════════════");
            log.info("   RESPUESTA DE LA AEAT");
            log.info("═══════════════════════════════════════════════════════════════");
            log.info("Status HTTP: {}", statusCode);
            log.info("Content-Type: {}", response.headers().firstValue("Content-Type").orElse("N/A"));
            log.info("Tamaño respuesta: {} caracteres", responseBody.length());

            if (log.isDebugEnabled()) {
                log.debug("Respuesta completa:\n{}", responseBody);
            } else {
                // Log de primeros 500 caracteres en modo INFO
                log.info("Respuesta (primeros 500 chars): {}",
                    responseBody.substring(0, Math.min(500, responseBody.length())));
            }

            // Verificar código de respuesta HTTP
            if (statusCode >= 200 && statusCode < 300) {
                log.info("✅ Respuesta HTTP exitosa ({})", statusCode);

                // Parsear respuesta SOAP para extraer el resultado
                String resultado = parsearRespuestaSOAP(responseBody);

                log.info("═══════════════════════════════════════════════════════════════");
                log.info("   ✅ FACTURA ENVIADA EXITOSAMENTE A LA AEAT");
                log.info("═══════════════════════════════════════════════════════════════");

                return resultado;

            } else if (statusCode == 400) {
                log.error("❌ Error 400 - Petición mal formada");
                log.error("El XML o el formato SOAP no es válido");
                throw new Exception("Error 400: Petición mal formada - " + extraerMensajeError(responseBody));

            } else if (statusCode == 401 || statusCode == 403) {
                log.error("❌ Error {} - Autenticación/Autorización", statusCode);
                log.error("El certificado no es válido o no tiene permisos");
                throw new Exception("Error " + statusCode + ": Certificado no autorizado - " + extraerMensajeError(responseBody));

            } else if (statusCode == 500) {
                log.error("❌ Error 500 - Error interno de la AEAT");
                throw new Exception("Error 500: Error del servidor AEAT - " + extraerMensajeError(responseBody));

            } else if (statusCode == 503) {
                log.error("❌ Error 503 - Servicio AEAT no disponible");
                throw new Exception("Error 503: Servicio AEAT temporalmente no disponible - Intenta más tarde");

            } else {
                log.error("❌ Error HTTP inesperado: {}", statusCode);
                throw new Exception("Error HTTP " + statusCode + ": " + extraerMensajeError(responseBody));
            }

        } catch (java.net.http.HttpTimeoutException e) {
            log.error("❌ Timeout al conectar con AEAT (>90 segundos)");
            throw new Exception("Timeout al conectar con AEAT - Verifica tu conexión a internet", e);

        } catch (java.net.ConnectException e) {
            log.error("❌ No se pudo conectar con la AEAT");
            log.error("URL: {}", aeatEndpoint);
            throw new Exception("No se pudo conectar con AEAT - Verifica el endpoint y tu conexión", e);

        } catch (javax.net.ssl.SSLHandshakeException e) {
            log.error("❌ Error en handshake SSL/TLS");
            log.error("Posibles causas:");
            log.error("  - Certificado caducado o no válido");
            log.error("  - Certificado no reconocido por la AEAT");
            log.error("  - Problemas con el truststore de Java");
            throw new Exception("Error SSL/TLS - Verifica el certificado digital", e);

        } catch (java.io.IOException e) {
            log.error("❌ Error de I/O al comunicar con AEAT: {}", e.getMessage());
            throw new Exception("Error de conexión con AEAT - " + e.getMessage(), e);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Envío interrumpido");
            throw new Exception("Envío interrumpido por el usuario", e);

        } catch (Exception e) {
            log.error("❌ Error inesperado al enviar a AEAT: {}", e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Stack trace completo:", e);
            }
            throw new Exception("Error al enviar a AEAT: " + e.getMessage(), e);
        }
    }

    /**
     * Construye un mensaje SOAP para enviar a la AEAT
     */
    private String construirMensajeSOAP(String xml, byte[] firma) {
        StringBuilder soap = new StringBuilder();

        soap.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        soap.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
        soap.append("xmlns:verifactu=\"http://www2.agenciatributaria.gob.es/hal/verifactu\">\n");
        soap.append("  <soapenv:Header/>\n");
        soap.append("  <soapenv:Body>\n");
        soap.append("    <verifactu:EnviarFactura>\n");
        soap.append("      <verifactu:RegistroFactura>\n");

        // Insertar el XML de la factura (escapado si es necesario)
        soap.append(xml);

        soap.append("      </verifactu:RegistroFactura>\n");

        // Si hay firma, incluirla
        if (firma != null) {
            soap.append("      <verifactu:Firma>");
            soap.append(Base64.getEncoder().encodeToString(firma));
            soap.append("</verifactu:Firma>\n");
        }

        soap.append("    </verifactu:EnviarFactura>\n");
        soap.append("  </soapenv:Body>\n");
        soap.append("</soapenv:Envelope>");

        return soap.toString();
    }

    /**
     * Parsea la respuesta SOAP de la AEAT para extraer el resultado
     */
    private String parsearRespuestaSOAP(String soapResponse) {
        // Buscar el estado en la respuesta
        // La AEAT devuelve algo como: <EstadoEnvio>Aceptada</EstadoEnvio>

        if (soapResponse.contains("<EstadoEnvio>Aceptada</EstadoEnvio>") ||
            soapResponse.contains("Aceptada") ||
            soapResponse.contains("ACEPTADA")) {
            return "ACEPTADA";
        }

        if (soapResponse.contains("<EstadoEnvio>AceptadaConErrores</EstadoEnvio>") ||
            soapResponse.contains("AceptadaConErrores")) {
            return "ACEPTADA_CON_ERRORES";
        }

        if (soapResponse.contains("<EstadoEnvio>Rechazada</EstadoEnvio>") ||
            soapResponse.contains("Rechazada") ||
            soapResponse.contains("RECHAZADA")) {
            return "RECHAZADA";
        }

        // Si no encontramos un estado conocido, devolver la respuesta completa
        return soapResponse;
    }

    /**
     * Extrae el mensaje de error de una respuesta SOAP de error
     */
    private String extraerMensajeError(String soapResponse) {
        // Buscar tags comunes de error
        int inicioError = soapResponse.indexOf("<DescripcionError>");
        if (inicioError > 0) {
            int finError = soapResponse.indexOf("</DescripcionError>", inicioError);
            if (finError > inicioError) {
                return soapResponse.substring(inicioError + 18, finError);
            }
        }

        inicioError = soapResponse.indexOf("<faultstring>");
        if (inicioError > 0) {
            int finError = soapResponse.indexOf("</faultstring>", inicioError);
            if (finError > inicioError) {
                return soapResponse.substring(inicioError + 13, finError);
            }
        }

        // Si no encontramos mensaje específico, devolver parte de la respuesta
        return soapResponse.length() > 200 ?
            soapResponse.substring(0, 200) + "..." :
            soapResponse;
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

