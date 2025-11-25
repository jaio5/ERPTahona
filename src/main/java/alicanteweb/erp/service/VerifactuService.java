// java
package alicanteweb.erp.service;

import alicanteweb.erp.entities.VerifactuEvidence;
import alicanteweb.erp.repository.VerifactuEvidenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.Formatter;

@Service
public class VerifactuService {

    private static final Logger log = LoggerFactory.getLogger(VerifactuService.class);

    private final VerifactuEvidenceRepository evidenceRepository;
    private final KeyStore keyStore;
    private final PrivateKey privateKey;
    private final X509Certificate certificate;
    private final boolean enabled;

    public VerifactuService(VerifactuEvidenceRepository evidenceRepository,
                            @Value("${verifactu.keystore.path}") String keystorePath,
                            @Value("${verifactu.keystore.password}") String keystorePassword,
                            @Value("${verifactu.key.alias}") String keyAlias,
                            @Value("${verifactu.key.password:${verifactu.keystore.password}}") String keyPassword) {
        this.evidenceRepository = evidenceRepository;
        KeyStore ks = null;
        PrivateKey pk = null;
        X509Certificate cert = null;
        boolean ok = false;

        try (InputStream is = getClass().getResourceAsStream(keystorePath)) {
            if (is == null) {
                log.warn("Keystore not found at: {} — verifactu will be disabled", keystorePath);
            } else {
                ks = KeyStore.getInstance("PKCS12");
                ks.load(is, keystorePassword != null ? keystorePassword.toCharArray() : null);

                Key key = ks.getKey(keyAlias, keyPassword != null ? keyPassword.toCharArray() : null);
                if (key instanceof PrivateKey) {
                    pk = (PrivateKey) key;
                } else {
                    log.warn("Key with alias {} is not a private key; verifactu disabled", keyAlias);
                }

                Certificate c = ks.getCertificate(keyAlias);
                if (c instanceof X509Certificate) {
                    cert = (X509Certificate) c;
                } else {
                    log.warn("Certificate with alias {} is not X509; verifactu disabled", keyAlias);
                }

                if (pk != null && cert != null) {
                    ok = true;
                }
            }
        } catch (Exception e) {
            // No fallamos el arranque por un keystore corrupto o formato inesperado
            log.warn("Error loading keystore for verifactu — verifactu disabled: {}", e.toString());
            if (log.isDebugEnabled()) log.debug("Stack:", e);
        }

        this.keyStore = ks;
        this.privateKey = pk;
        this.certificate = cert;
        this.enabled = ok;

        if (this.enabled) {
            log.info("Verifactu initialized using keystore {}", keystorePath);
        } else {
            log.info("Verifactu disabled — application will continue without evidences registered");
        }
    }

    private void ensureEnabled() {
        if (!enabled) throw new IllegalStateException("Verifactu not configured/disabled");
    }

    // calcule la huella SHA-256 de la factura + hash anterior (cadena clara)
    public String computeChainedHash(String facturaPayload, String previousHash) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(facturaPayload.getBytes(StandardCharsets.UTF_8));
        if (previousHash != null) md.update(previousHash.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();
        return bytesToHex(digest);
    }

    public byte[] signHashHex(String hashHex) throws Exception {
        ensureEnabled();
        byte[] data = hashHex.getBytes(StandardCharsets.UTF_8);
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }

    public String certificateFingerprint() throws Exception {
        ensureEnabled();
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] der = certificate.getEncoded();
        byte[] digest = md.digest(der);
        return bytesToHex(digest);
    }

    public VerifactuEvidence registerEvidence(String facturaId, String facturaPayload, String serie, String numero, LocalDateTime fechaEmision) throws Exception {
        ensureEnabled();
        Optional<VerifactuEvidence> prev = evidenceRepository.findByFacturaId(facturaId);
        String prevHash = prev.map(VerifactuEvidence::getHash).orElse(null);

        String computedHash = computeChainedHash(facturaPayload, prevHash);
        byte[] signature = signHashHex(computedHash);
        String fingerprint = certificateFingerprint();

        VerifactuEvidence ev = new VerifactuEvidence();
        ev.setFacturaId(facturaId);
        ev.setSerie(serie);
        ev.setNumero(numero);
        ev.setFechaEmision(fechaEmision);
        ev.setHash(computedHash);
        ev.setHashAnterior(prevHash);
        ev.setSignature(signature);
        ev.setCertFingerprint(fingerprint);
        // metadata opcional: guardar JSON con versión/algoritmos
        ev.setMetadata("{\"algo\":\"SHA-256\",\"sig\":\"SHA256withRSA\"}");
        ev.setCreatedAt(LocalDateTime.now());

        return evidenceRepository.save(ev);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        try (Formatter fmt = new Formatter(sb)) {
            for (byte b : bytes) {
                fmt.format("%02x", b);
            }
        }
        return sb.toString();
    }
}
