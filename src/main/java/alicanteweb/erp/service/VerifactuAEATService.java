package alicanteweb.erp.service;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Base64;

/**
 * Servicio que encapsula operaciones necesarias para comunicarse con la AEAT
 * (simulado en esta versión). Incluye utilidades para generar hash, firmar
 * y calcular fingerprint del certificado.
 *
 * Comentarios para un estudiante de DAM:
 * - En producción la comunicación con AEAT requerirá endpoints concretos y
 *   manejo seguro de certificados/keys.
 * - Aquí firmamos con BouncyCastle y usamos un KeyStore PKCS12 cargado desde
 *   una ruta configurada en application.properties.
 */
@Service
public class VerifactuAEATService {

    @Value("${verifactu.cert.path}")
    private String certPath;
    @Value("${verifactu.cert.password}")
    private String certPassword;
    @Value("${verifactu.aeat.endpoint}")
    private String aeatEndpoint;

    public VerifactuAEATService() {
        // Agregamos el proveedor BouncyCastle para disponer de algoritmos adicionales.
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Genera un hash SHA-256 (Base64) para los datos de la factura.
     */
    public String generarHash(String datosFactura) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(datosFactura.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Firma los bytes proporcionados con la clave privada del keystore configurado.
     * IMPORTANTE: el path y passwords vienen de la configuración, asegúrate de no
     * comitear secrets en el repositorio.
     */
    public byte[] firmarDatos(byte[] datos) throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new java.io.FileInputStream(certPath), certPassword.toCharArray());
        String alias = ks.aliases().nextElement();
        PrivateKey pk = (PrivateKey) ks.getKey(alias, certPassword.toCharArray());
        Signature signature = Signature.getInstance("SHA256withRSA", "BC");
        signature.initSign(pk);
        signature.update(datos);
        return signature.sign();
    }

    /**
     * Devuelve el fingerprint (Base64) del certificado contenido en el keystore.
     */
    public String getCertFingerprint() throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new java.io.FileInputStream(certPath), certPassword.toCharArray());
        String alias = ks.aliases().nextElement();
        Certificate cert = ks.getCertificate(alias);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(cert.getEncoded());
        return Base64.getEncoder().encodeToString(digest);
    }

    /**
     * Simula el envío a la AEAT. En producción usar RestTemplate o WebClient con TLS
     * y autenticación adecuada. Aquí devolvemos una respuesta simulada.
     */
    public String enviarAEAT(String jsonEvidencia) {
        RestTemplate restTemplate = new RestTemplate();
        // Aquí se haría la llamada real a la AEAT
        // return restTemplate.postForObject(aeatEndpoint, jsonEvidencia, String.class);
        return "{\"resultado\":\"ok\"}"; // Simulación
    }
}
