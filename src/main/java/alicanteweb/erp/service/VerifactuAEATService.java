package alicanteweb.erp.service;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Base64;

@Service
public class VerifactuAEATService {

    @Value("${verifactu.cert.path}")
    private String certPath;
    @Value("${verifactu.cert.password}")
    private String certPassword;
    @Value("${verifactu.aeat.endpoint}")
    private String aeatEndpoint;

    public VerifactuAEATService() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public String generarHash(String datosFactura) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(datosFactura.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(hash);
    }

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

    public String getCertFingerprint() throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new java.io.FileInputStream(certPath), certPassword.toCharArray());
        String alias = ks.aliases().nextElement();
        Certificate cert = ks.getCertificate(alias);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(cert.getEncoded());
        return Base64.getEncoder().encodeToString(digest);
    }

    public String enviarAEAT(String jsonEvidencia) {
        RestTemplate restTemplate = new RestTemplate();
        // Aquí se haría la llamada real a la AEAT
        // return restTemplate.postForObject(aeatEndpoint, jsonEvidencia, String.class);
        return "{\"resultado\":\"ok\"}"; // Simulación
    }
}

