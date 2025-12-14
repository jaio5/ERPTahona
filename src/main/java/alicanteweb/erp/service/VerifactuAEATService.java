package alicanteweb.erp.service;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.util.Base64;

@Service
public class VerifactuAEATService {

    private static final Logger log = LoggerFactory.getLogger(VerifactuAEATService.class);

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Value("${verifactu.cert.path}")
    private String certPath;

    @Value("${verifactu.cert.password}")
    private String certPassword;

    @Value("${verifactu.aeat.endpoint}")
    private String aeatEndpoint;

    @Value("${verifactu.aeat.enabled:false}")
    private boolean aeatEnabled;

    /**
     * Genera el hash SHA-256 de los datos de la factura
     */
    public String generarHash(String datosFactura) throws Exception {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(datosFactura.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Error generando hash", e);
            throw new Exception("Error generando hash: " + e.getMessage(), e);
        }
    }

    /**
     * Firma digitalmente los datos usando el certificado
     */
    public byte[] firmarDatos(byte[] datos) throws Exception {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            try (FileInputStream fis = new FileInputStream(certPath)) {
                ks.load(fis, certPassword.toCharArray());
            }

            String alias = ks.aliases().nextElement();
            PrivateKey pk = (PrivateKey) ks.getKey(alias, certPassword.toCharArray());

            Signature signature = Signature.getInstance("SHA256withRSA", "BC");
            signature.initSign(pk);
            signature.update(datos);

            byte[] firma = signature.sign();
            log.debug("Datos firmados correctamente, tamaño firma: {} bytes", firma.length);
            return firma;
        } catch (Exception e) {
            log.error("Error firmando datos", e);
            throw new Exception("Error firmando datos: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene la huella digital del certificado
     */
    public String getCertFingerprint() throws Exception {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            try (FileInputStream fis = new FileInputStream(certPath)) {
                ks.load(fis, certPassword.toCharArray());
            }

            String alias = ks.aliases().nextElement();
            Certificate cert = ks.getCertificate(alias);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(cert.getEncoded());
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            log.error("Error obteniendo fingerprint del certificado", e);
            throw new Exception("Error obteniendo fingerprint: " + e.getMessage(), e);
        }
    }

    /**
     * Envía la evidencia a la AEAT
     * Si aeatEnabled=false, simula el envío sin conectarse realmente
     */
    public String enviarAEAT(String jsonEvidencia) {
        if (!aeatEnabled) {
            log.info("Modo prueba - Evidencia no enviada a AEAT (aeat.enabled=false)");
            return buildSimulatedResponse(true, "SIMULADO", "Registro procesado en modo prueba");
        }

        try {
            log.info("Enviando evidencia a AEAT: {}", aeatEndpoint);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(jsonEvidencia, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(aeatEndpoint, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Evidencia enviada exitosamente a AEAT");
                return response.getBody();
            } else {
                log.error("Error del servidor AEAT: {}", response.getStatusCode());
                return buildSimulatedResponse(false, "ERROR_HTTP_" + response.getStatusCode(),
                    "Error del servidor: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error enviando evidencia a AEAT", e);
            return buildSimulatedResponse(false, "ERROR_CONEXION",
                "Error de conexión: " + e.getMessage());
        }
    }

    /**
     * Reenvía una evidencia que falló previamente
     */
    public String reenviarAEAT(String jsonEvidencia) {
        log.info("Reenviando evidencia a AEAT...");
        return enviarAEAT(jsonEvidencia);
    }

    /**
     * Verifica el estado de una evidencia en la AEAT
     */
    public String verificarEstadoAEAT(String hash) {
        if (!aeatEnabled) {
            return buildSimulatedResponse(true, "VERIFICADO", "Estado verificado en modo prueba");
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = aeatEndpoint + "/verificar/" + hash;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                return buildSimulatedResponse(false, "ERROR_VERIFICACION",
                    "No se pudo verificar el estado");
            }
        } catch (Exception e) {
            log.error("Error verificando estado en AEAT", e);
            return buildSimulatedResponse(false, "ERROR_CONEXION",
                "Error verificando estado: " + e.getMessage());
        }
    }

    /**
     * Construye una respuesta simulada en formato JSON
     */
    private String buildSimulatedResponse(boolean exito, String codigo, String mensaje) {
        return String.format(
            "{\"resultado\":\"%s\",\"codigo\":\"%s\",\"mensaje\":\"%s\",\"timestamp\":\"%s\"}",
            exito ? "OK" : "ERROR",
            codigo,
            mensaje,
            java.time.Instant.now().toString()
        );
    }

    /**
     * Indica si el envío a AEAT está habilitado
     */
    public boolean isAeatEnabled() {
        return aeatEnabled;
    }
}
