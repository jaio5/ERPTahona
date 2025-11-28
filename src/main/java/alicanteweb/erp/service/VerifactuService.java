package alicanteweb.erp.service;

import alicanteweb.erp.repository.VerifactuEvidenceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

@Service
public class VerifactuService {

    private static final Logger log = LoggerFactory.getLogger(VerifactuService.class);

    @Value("${verifactu.keystore.path}")
    private String keystorePath;

    @Value("${verifactu.keystore.password}")
    private String keystorePassword;

    @Value("${verifactu.key.alias}")
    private String keyAlias;

    @Value("${verifactu.key.password:${verifactu.keystore.password}}")
    private String keyPassword;

    private final VerifactuEvidenceRepository evidenceRepository;
    private final KeyStore keyStore;
    private final PrivateKey privateKey;
    private final X509Certificate certificate;
    private final boolean enabled;

    public VerifactuService(VerifactuEvidenceRepository evidenceRepository) {
        this.evidenceRepository = evidenceRepository;
        KeyStore ks = null;
        PrivateKey pk = null;
        X509Certificate cert = null;
        boolean ok = false;

        try (InputStream is = openKeystoreStream(keystorePath)) {
            if (is == null) {
                log.warn("Keystore no encontrado en {} (classpath o disco) — verifactu deshabilitado", keystorePath);
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
            log.warn("Error cargando el keystore para verifactu — verifactu deshabilitado: {}", e.getMessage());
            if (log.isDebugEnabled()) log.debug("Stack:", e);
        }

        this.keyStore = ks;
        this.privateKey = pk;
        this.certificate = cert;
        this.enabled = ok;

        if (this.enabled) {
            log.info("Verifactu inicializado usando keystore {}", keystorePath);
        } else {
            log.info("Verifactu deshabilitado — la aplicación continuará sin registrar evidencias");
        }
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
}
