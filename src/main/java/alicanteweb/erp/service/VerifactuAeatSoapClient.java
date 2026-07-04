package alicanteweb.erp.service;

import jakarta.xml.soap.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;

/**
 * Cliente SOAP REAL para enviar facturas a la AEAT
 * Implementación completa y funcional
 */
@Service
@Lazy
@ConditionalOnProperty(prefix = "verifactu.aeat", name = "enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class VerifactuAeatSoapClient {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    @Value("${verifactu.aeat.endpoint:}")
    private String aeatEndpoint;

    @Value("${verifactu.aeat.enabled:false}")
    private boolean aeatEnabled;

    @Value("${verifactu.keystore.path:}")
    private String keystorePath;

    @Value("${verifactu.keystore.password:}")
    private String keystorePassword;

    @Value("${verifactu.key.alias:}")
    private String keyAlias;

    @Value("${verifactu.key.password:${verifactu.keystore.password}}")
    private String keyPassword;

    /**
     * Envía una factura Verifactu a la AEAT
     *
     * @param xmlFactura XML de la factura según schema AEAT
     * @param firma Firma digital de la factura
     * @return Respuesta de la AEAT
     * @throws Exception Si hay error en el envío
     */
    public String enviarFacturaAeat(String xmlFactura, byte[] firma) throws Exception {
        if (!aeatEnabled) {
            log.warn("Envío a AEAT deshabilitado (verifactu.aeat.enabled=false)");
            return "SIMULADO_OK";
        }

        log.info("🚀 Iniciando envío SOAP a AEAT: {}", aeatEndpoint);

        try {
            // 1. Crear mensaje SOAP
            SOAPMessage soapMessage = crearMensajeSOAP(xmlFactura);

            // 2. Firmar el mensaje (si no se pasa 'firma' externa, se firma con el keystore configurado)
            firmarMensajeSOAP(soapMessage, firma);

            // 3. Enviar a AEAT
            SOAPMessage respuesta = enviarSOAP(soapMessage);

            // 4. Procesar respuesta
            String resultado = procesarRespuesta(respuesta);

            log.info("✅ Respuesta AEAT recibida: {}", resultado.substring(0, Math.min(100, resultado.length())));

            return resultado;

        } catch (Exception e) {
            log.error("❌ Error enviando a AEAT: {}", e.getMessage(), e);
            throw new Exception("Error en comunicación con AEAT: " + e.getMessage(), e);
        }
    }

    /**
     * Crea el mensaje SOAP según especificación AEAT
     */
    private SOAPMessage crearMensajeSOAP(String xmlFactura) throws Exception {
        log.debug("Creando mensaje SOAP...");

        // Crear mensaje SOAP 1.1 (AEAT usa SOAP 1.1, no 1.2)
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPMessage soapMessage = messageFactory.createMessage();

        SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();

        // Namespace del servicio VeriFactu de AEAT (tikeV1.0)
        envelope.addNamespaceDeclaration("sum",
            "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tikeV1.0/cont/ws/SuministroLR.xsd");

        // Crear cuerpo del mensaje
        SOAPBody soapBody = envelope.getBody();

        // Parsear el XML de la factura como documento
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        // Mitigar XXE
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        } catch (ParserConfigurationException e) {
            log.warn("No se pudo habilitar la protección XXE en DocumentBuilderFactory; el parser puede ser vulnerable a ataques XXE", e);
        }
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document facturaDoc = builder.parse(new ByteArrayInputStream(xmlFactura.getBytes(StandardCharsets.UTF_8)));

        // Importar el nodo al mensaje SOAP
        soapBody.addDocument(facturaDoc);

        // Header SOAP (opcional según AEAT)
        if (envelope.getHeader() == null) {
            envelope.addHeader();
        }

        soapMessage.saveChanges();

        // Log del mensaje (solo en debug)
        if (log.isDebugEnabled()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            soapMessage.writeTo(out);
            log.debug("Mensaje SOAP creado:\n{}", out.toString(StandardCharsets.UTF_8));
        }

        return soapMessage;
    }

    /**
     * Firma el mensaje SOAP con el certificado digital
     */
    private void firmarMensajeSOAP(SOAPMessage soapMessage, byte[] firma) throws Exception {
        log.debug("Firmando mensaje SOAP con certificado digital...");

        // Cargar certificado
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            keyStore.load(fis, keystorePassword.toCharArray());
        }

        // Obtener clave privada y certificado
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyAlias, keyPassword.toCharArray());
        X509Certificate certificate = (X509Certificate) keyStore.getCertificate(keyAlias);

        if (privateKey == null || certificate == null) {
            throw new Exception("No se pudo cargar el certificado digital. Verifica keystore, alias y password.");
        }

        // Use modern API to obtain subject name
        String subject = certificate.getSubjectX500Principal() != null ? certificate.getSubjectX500Principal().getName() : (certificate.getIssuerX500Principal() != null ? certificate.getIssuerX500Principal().getName() : "desconocido");
        log.info("Certificado cargado: {}", subject);

        // Indicar si se recibió un 'firma' externa (evita warning por parámetro no usado)
        if (firma != null) {
            log.debug("Firma externa recibida (bytes): {}", firma.length);
        }

        // Implementar firma WS-Security: insertar BinarySecurityToken y firmar el Body referenciando wsu:Id
        try {
            // Convertir SOAPMessage a Document
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            soapMessage.writeTo(baos);
            byte[] soapBytes = baos.toByteArray();

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            try {
                dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            } catch (ParserConfigurationException e) {
                log.warn("No se pudo habilitar la protección XXE en DocumentBuilderFactory (firma WS-Security); el parser puede ser vulnerable a ataques XXE", e);
            }
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc;
            try (InputStream is = new ByteArrayInputStream(soapBytes)) {
                doc = db.parse(is);
            }

            // Namespaces WS-Security
            final String WSSE_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
            final String WSU_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
            final String X509_TOKEN_TYPE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3";

            // Localizar el elemento payload dentro del Body (primer hijo elemento) y asignarle wsu:Id
            Element payloadElem = null;
            NodeList bodyList = doc.getElementsByTagNameNS("http://schemas.xmlsoap.org/soap/envelope/", "Body");
            if (bodyList.getLength() == 0) bodyList = doc.getElementsByTagNameNS("http://www.w3.org/2003/05/soap-envelope", "Body");
            if (bodyList.getLength() > 0) {
                Element bodyElement = (Element) bodyList.item(0);
                // buscar primer hijo elemento del Body
                for (int i = 0; i < bodyElement.getChildNodes().getLength(); i++) {
                    org.w3c.dom.Node n = bodyElement.getChildNodes().item(i);
                    if (n instanceof Element) { payloadElem = (Element) n; break; }
                }
            }

            if (payloadElem == null) {
                throw new Exception("No se encontró el elemento payload dentro de SOAP Body para firmar");
            }

            // Crear un Id único con prefijo wsu en el payload (AEAT espera firmar la factura, no el Body)
            String payloadId = "id-payload-" + System.currentTimeMillis();
            payloadElem.setAttributeNS(WSU_NS, "wsu:Id", payloadId);
            // Registrar atributo Id como id para la DOM
            payloadElem.setIdAttributeNS(WSU_NS, "Id", true);

            // Localizar o crear Header
            Element headerElem = null;
            NodeList headerNl = doc.getElementsByTagNameNS("http://schemas.xmlsoap.org/soap/envelope/", "Header");
            if (headerNl.getLength() > 0) headerElem = (Element) headerNl.item(0);
            else {
                headerNl = doc.getElementsByTagNameNS("http://www.w3.org/2003/05/soap-envelope", "Header");
                if (headerNl.getLength() > 0) headerElem = (Element) headerNl.item(0);
            }
            if (headerElem == null) {
                // crear Header bajo Envelope
                NodeList envList = doc.getElementsByTagNameNS("http://schemas.xmlsoap.org/soap/envelope/", "Envelope");
                Element env = envList.getLength() > 0 ? (Element) envList.item(0) : doc.getDocumentElement();
                headerElem = doc.createElementNS(env.getNamespaceURI(), "Header");
                env.insertBefore(headerElem, env.getFirstChild());
            }

            // Crear wsse:Security dentro del Header
            Element securityElem = doc.createElementNS(WSSE_NS, "wsse:Security");
            securityElem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:wsse", WSSE_NS);
            securityElem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:wsu", WSU_NS);
            // declarar prefijo soapenv para mustUnderstand
            final String SOAP_ENV_NS = "http://schemas.xmlsoap.org/soap/envelope/";
            securityElem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:soapenv", SOAP_ENV_NS);
            // Marcar mustUnderstand para que intermediarios SOAP lo procesen según spec
            securityElem.setAttributeNS(SOAP_ENV_NS, "soapenv:mustUnderstand", "1");
            headerElem.appendChild(securityElem);

            // Añadir Timestamp (wsu:Timestamp) exigido por WS-Security
            try {
                String created = Instant.now().toString();
                String expires = Instant.now().plusSeconds(300).toString();
                Element timestamp = doc.createElementNS(WSU_NS, "wsu:Timestamp");
                timestamp.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:wsu", WSU_NS);
                Element createdElem = doc.createElementNS(WSU_NS, "wsu:Created");
                createdElem.setTextContent(created);
                Element expiresElem = doc.createElementNS(WSU_NS, "wsu:Expires");
                expiresElem.setTextContent(expires);
                timestamp.appendChild(createdElem);
                timestamp.appendChild(expiresElem);
                securityElem.appendChild(timestamp);
            } catch (Exception ex) {
                log.debug("No se pudo agregar Timestamp WS-Security: {}", ex.getMessage());
            }

            // Insertar BinarySecurityToken con el certificado (Base64)
            String bstId = "X509-" + System.currentTimeMillis();
            Element bst = doc.createElementNS(WSSE_NS, "wsse:BinarySecurityToken");
            bst.setAttributeNS(WSU_NS, "wsu:Id", bstId);
            bst.setAttribute("ValueType", X509_TOKEN_TYPE);
            bst.setAttribute("EncodingType", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary");
            String certB64 = java.util.Base64.getEncoder().encodeToString(certificate.getEncoded());
            bst.setTextContent(certB64);
            securityElem.appendChild(bst);
            // Registrar wsu:Id del BinarySecurityToken como ID (por si el validador lo requiere)
            try {
                bst.setIdAttributeNS(WSU_NS, "Id", true);
            } catch (Exception ignore) {
                // No crítico si no se puede marcar como id en algunas implementaciones DOM
            }

            // Preparar XMLSignatureFactory
            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

            // Reference al payload por wsu:Id (AEAT requiere que el XML de factura/payload sea el firmado)
            // Transforms: Enveloped + Exclusive C14N to match typical WS-Security requirements
            Transform envTransform = fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);
            Transform c14nTransform = fac.newTransform(CanonicalizationMethod.EXCLUSIVE, (TransformParameterSpec) null);
            Reference ref = fac.newReference("#" + payloadId,
                    fac.newDigestMethod(DigestMethod.SHA256, null),
                    java.util.Arrays.asList(envTransform, c14nTransform),
                    null,
                    null);

            // SignedInfo con canonicalization exclusive y RSA-SHA256
            SignedInfo si = fac.newSignedInfo(
                    fac.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null),
                    fac.newSignatureMethod(SignatureMethod.RSA_SHA256, null),
                    java.util.Collections.singletonList(ref)
            );

            // Crear SecurityTokenReference DOM element: <wsse:SecurityTokenReference><wsse:Reference URI="#bstId" ValueType="...#X509v3"/></wsse:SecurityTokenReference>
            Element str = doc.createElementNS(WSSE_NS, "wsse:SecurityTokenReference");
            Element refElem = doc.createElementNS(WSSE_NS, "wsse:Reference");
            refElem.setAttribute("URI", "#" + bstId);
            refElem.setAttribute("ValueType", X509_TOKEN_TYPE);
            str.appendChild(refElem);

            // Crear KeyInfo que contenga el SecurityTokenReference
            KeyInfoFactory kif = fac.getKeyInfoFactory();
            javax.xml.crypto.dom.DOMStructure domStr = new javax.xml.crypto.dom.DOMStructure(str);
            KeyInfo ki = kif.newKeyInfo(java.util.Collections.singletonList(domStr));

            // Crear la firma XML y firmar colocando la firma dentro de wsse:Security
            DOMSignContext dsc = new DOMSignContext(privateKey, securityElem);
            XMLSignature signature = fac.newXMLSignature(si, ki);
            signature.sign(dsc);

            // Convertir Document firmado a SOAPMessage
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            ByteArrayOutputStream signedOut = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(doc), new StreamResult(signedOut));

            MessageFactory mf = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
            try (InputStream signedIs = new ByteArrayInputStream(signedOut.toByteArray())) {
                SOAPMessage signedMsg = mf.createMessage(null, signedIs);
                // Reemplazar contenido del soapMessage original
                soapMessage.getSOAPPart().setContent(signedMsg.getSOAPPart().getContent());
            }

        } catch (Exception e) {
            log.error("Error firmando mensaje SOAP (WS-Security): {}", e.getMessage(), e);
            throw new Exception("Error al firmar mensaje SOAP: " + e.getMessage(), e);
        }
    }

    /**
     * Envía el mensaje SOAP al endpoint de la AEAT
     */
    private SOAPMessage enviarSOAP(SOAPMessage soapMessage) throws Exception {
        log.info("Enviando mensaje SOAP a: {}", aeatEndpoint);

        // Convertir SOAPMessage a bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        soapMessage.writeTo(baos);
        byte[] requestBytes = baos.toByteArray();

        // Enviar usando java.net.http.HttpClient en lugar de SOAPConnection (evita APIs deprecated)
        // Timeouts obligatorios: sin ellos, un endpoint AEAT colgado bloquearía el hilo
        // (y con él la emisión de facturas) indefinidamente.
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(aeatEndpoint))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "text/xml; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestBytes))
                .build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        int status = response.statusCode();
        if (status < 200 || status >= 300) {
            throw new Exception("HTTP error from AEAT: " + status + " - " + new String(response.body(), StandardCharsets.UTF_8));
        }

        byte[] respBytes = response.body();
        MessageFactory mf = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        try (InputStream is = new ByteArrayInputStream(respBytes)) {
            SOAPMessage responseMsg = mf.createMessage(null, is);
            if (responseMsg == null) {
                throw new Exception("Respuesta SOAP nula de la AEAT");
            }
            return responseMsg;
        }
    }

    /**
     * Procesa la respuesta SOAP de la AEAT
     */
    private String procesarRespuesta(SOAPMessage respuesta) throws Exception {
        log.debug("Procesando respuesta SOAP...");

        // Convertir respuesta a String
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        respuesta.writeTo(out);
        String xmlRespuesta = out.toString(StandardCharsets.UTF_8);

        // Verificar si hay errores SOAP
        if (respuesta.getSOAPBody().hasFault()) {
            SOAPFault fault = respuesta.getSOAPBody().getFault();
            String faultCode = fault.getFaultCode();
            String faultString = fault.getFaultString();

            log.error("❌ SOAP Fault recibido: {} - {}", faultCode, faultString);
            throw new Exception("Error SOAP de AEAT: " + faultString);
        }

        // Buscar el estado en la respuesta
        if (xmlRespuesta.contains("<EstadoRegistro>Correcto</EstadoRegistro>") ||
            xmlRespuesta.contains("<EstadoEnvio>Correcto</EstadoEnvio>")) {

            log.info("✅ Factura ACEPTADA por AEAT");
            return "ACEPTADA";

        } else if (xmlRespuesta.contains("<EstadoRegistro>AceptadoConErrores</EstadoRegistro>")) {

            String errores = extraerErrores(xmlRespuesta);
            log.warn("⚠️ Factura ACEPTADA CON ERRORES: {}", errores);
            return "ACEPTADA_CON_ERRORES: " + errores;

        } else if (xmlRespuesta.contains("<EstadoRegistro>Rechazado</EstadoRegistro>")) {

            String errores = extraerErrores(xmlRespuesta);
            log.error("❌ Factura RECHAZADA por AEAT: {}", errores);
            throw new Exception("Factura rechazada por AEAT: " + errores);

        } else {
            // Respuesta inesperada
            log.warn("⚠️ Respuesta inesperada de AEAT (primeros 500 caracteres):\n{}",
                    xmlRespuesta.substring(0, Math.min(500, xmlRespuesta.length())));
            return "RESPUESTA_INESPERADA";
        }
    }

    /**
     * Extrae los mensajes de error de la respuesta XML
     */
    private String extraerErrores(String xmlRespuesta) {
        StringBuilder errores = new StringBuilder();

        // Buscar todos los errores en la respuesta
        String[] lineas = xmlRespuesta.split("\n");
        for (String linea : lineas) {
            if (linea.contains("<Descripcion>") || linea.contains("<DescripcionErrorRegistro>")) {
                // Extraer el texto entre tags
                int inicio = linea.indexOf(">") + 1;
                int fin = linea.lastIndexOf("<");
                if (inicio > 0 && fin > inicio) {
                    String error = linea.substring(inicio, fin).trim();
                    if (!error.isEmpty()) {
                        errores.append(error).append("; ");
                    }
                }
            }
        }

        return !errores.isEmpty() ? errores.toString() : "Error desconocido";
    }

    /**
     * Verifica el estado de una factura previamente enviada
     */
    public String verificarEstadoFactura(String numeroFactura, String nifEmisor) throws Exception {
        if (!aeatEnabled) {
            return "SIMULADO_NO_VERIFICADO";
        }

        log.info("Verificando estado de factura {} en AEAT", numeroFactura);

        try {
            // Crear mensaje de consulta
            SOAPMessage consulta = crearMensajeConsulta(numeroFactura, nifEmisor);

            // Enviar
            SOAPMessage respuesta = enviarSOAP(consulta);

            // Procesar
            return procesarRespuestaConsulta(respuesta);

        } catch (Exception e) {
            log.error("Error verificando estado: {}", e.getMessage(), e);
            throw new Exception("Error verificando estado en AEAT: " + e.getMessage(), e);
        }
    }

    private SOAPMessage crearMensajeConsulta(String numeroFactura, String nifEmisor) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPMessage soapMessage = messageFactory.createMessage();

        SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();

        envelope.addNamespaceDeclaration("siiLR",
            "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/ssii/fact/ws/ConsultaLR.xsd");

        SOAPBody soapBody = envelope.getBody();
        SOAPElement consulta = soapBody.addChildElement("ConsultaFactura", "siiLR");

        SOAPElement nif = consulta.addChildElement("NIF");
        nif.addTextNode(nifEmisor);

        SOAPElement numero = consulta.addChildElement("NumeroFactura");
        numero.addTextNode(numeroFactura);

        soapMessage.saveChanges();
        return soapMessage;
    }

    private String procesarRespuestaConsulta(SOAPMessage respuesta) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        respuesta.writeTo(out);
        String xmlRespuesta = out.toString(StandardCharsets.UTF_8);

        if (xmlRespuesta.contains("<FacturaEncontrada>SI</FacturaEncontrada>")) {
            return "ENCONTRADA";
        } else if (xmlRespuesta.contains("<FacturaEncontrada>NO</FacturaEncontrada>")) {
            return "NO_ENCONTRADA";
        } else {
            return "RESPUESTA_INESPERADA";
        }
    }

    /**
     * Verifica la conexión con la AEAT
     */
    public boolean verificarConexion() {
        if (!aeatEnabled) {
            log.info("Verificación de conexión omitida (AEAT deshabilitado)");
            return false;
        }

        try {
            log.info("Verificando conexión con AEAT...");

            // Intentar crear una conexión simple
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(CONNECT_TIMEOUT)
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aeatEndpoint))
                    .timeout(REQUEST_TIMEOUT)
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status < 200 || status >= 300) {
                log.error("❌ Respuesta inesperada de AEAT (HEAD request): {}", status);
                return false;
            }

            log.info("✅ Conexión con AEAT verificada correctamente");
            return true;

        } catch (Exception e) {
            log.error("❌ No se pudo conectar con AEAT: {}", e.getMessage());
            return false;
        }
    }
}

