package alicanteweb.erp.service;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.util.Base64;

@Service
public class VerifactuAEATService {

    private static final Logger log = LoggerFactory.getLogger(VerifactuAEATService.class);

    private final String certPath;
    private final String certPassword;
    private final String aeatEndpoint;
    private final boolean aeatEnabled;

    public VerifactuAEATService(
            @Value("${verifactu.cert.path}") String certPath,
            @Value("${verifactu.cert.password}") String certPassword,
            @Value("${verifactu.aeat.endpoint}") String aeatEndpoint,
            @Value("${verifactu.aeat.enabled:false}") boolean aeatEnabled) {
        this.certPath = certPath;
        this.certPassword = certPassword;
        this.aeatEndpoint = aeatEndpoint;
        this.aeatEnabled = aeatEnabled;
        Security.addProvider(new BouncyCastleProvider());
    }

    public boolean isAeatEnabled() {
        return aeatEnabled;
    }

    public String enviarAEAT(String evidenciaPayload) {
        if (!aeatEnabled) {
            log.warn("MODO DE PRUEBA: El envío a la AEAT está deshabilitado. Se devolverá una respuesta simulada.");
            return "{\"resultado\":\"ok (simulado)\"}";
        }

        log.info("Iniciando envío de evidencia a la AEAT al endpoint: {}", aeatEndpoint);

        // TODO: En un entorno de producción, el RestTemplate debe ser configurado para
        // usar TLS mutuo (mTLS) con el certificado de la empresa.
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(evidenciaPayload, headers);

        try {
            log.debug("Enviando payload a {}: {}", aeatEndpoint, evidenciaPayload);
            String response = restTemplate.postForObject(aeatEndpoint, request, String.class);
            log.info("Respuesta recibida de la AEAT con éxito: {}", response);
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Error HTTP al conectar con la AEAT. Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error en la comunicación con la AEAT (HTTP " + e.getRawStatusCode() + "): " + e.getResponseBodyAsString(), e);
        } catch (ResourceAccessException e) {
            log.error("Error de acceso al recurso (e.g., timeout, DNS) al conectar con AEAT: {}", e.getMessage());
            throw new RuntimeException("No se pudo conectar con el servicio de la AEAT. Verifique la red y el endpoint.", e);
        } catch (Exception e) {
            log.error("Error inesperado durante el envío a la AEAT.", e);
            throw new RuntimeException("Error inesperado al procesar el envío a la AEAT.", e);
        }
    }

    // --- Métodos de firma y hash (sin cambios) ---

    public String generarHash(String datosFactura) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(datosFactura.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(hash);
    }

    public byte[] firmarDatos(byte[] datos) throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream(certPath), certPassword.toCharArray());
        String alias = ks.aliases().nextElement();
        PrivateKey pk = (PrivateKey) ks.getKey(alias, certPassword.toCharArray());
        Signature signature = Signature.getInstance("SHA256withRSA", "BC");
        signature.initSign(pk);
        signature.update(datos);
        return signature.sign();
    }

    public String getCertFingerprint() throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream(certPath), certPassword.toCharArray());
        String alias = ks.aliases().nextElement();
        Certificate cert = ks.getCertificate(alias);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(cert.getEncoded());
        return Base64.getEncoder().encodeToString(digest);
    }
}
