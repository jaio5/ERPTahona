package alicanteweb.erp.service;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.util.Base64;

@Service
public class VerifactuAEATService {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Value("${verifactu.cert.path}")
    private String certPath;
    @Value("${verifactu.cert.password}")
    private String certPassword;
    @Value("${verifactu.aeat.endpoint}")
    private String aeatEndpoint;

    public String generarHash(String datosFactura) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(datosFactura.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    public byte[] firmarDatos(byte[] datos) throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (java.io.FileInputStream fis = new java.io.FileInputStream(certPath)) {
            ks.load(fis, certPassword.toCharArray());
        }
        String alias = ks.aliases().nextElement();
        PrivateKey pk = (PrivateKey) ks.getKey(alias, certPassword.toCharArray());
        Signature signature = Signature.getInstance("SHA256withRSA", "BC");
        signature.initSign(pk);
        signature.update(datos);
        return signature.sign();
    }

    public String getCertFingerprint() throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (java.io.FileInputStream fis = new java.io.FileInputStream(certPath)) {
            ks.load(fis, certPassword.toCharArray());
        }
        String alias = ks.aliases().nextElement();
        Certificate cert = ks.getCertificate(alias);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(cert.getEncoded());
        return Base64.getEncoder().encodeToString(digest);
    }

    public String enviarAEAT(String jsonEvidencia) {
        RestTemplate restTemplate = new RestTemplate();
        return "{\"resultado\":\"ok\"}";
    }
}
