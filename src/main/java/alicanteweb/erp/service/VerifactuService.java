package alicanteweb.erp.service;

import alicanteweb.erp.entities.VerifactuEvidence;
import alicanteweb.erp.repository.VerifactuEvidenceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

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
}
