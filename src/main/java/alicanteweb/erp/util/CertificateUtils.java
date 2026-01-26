package alicanteweb.erp.util;

import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.regex.Pattern;

public final class CertificateUtils {

    private CertificateUtils() {}

    // Máximo tamaño de keystore en bytes (5 MB)
    public static final long MAX_KEYSTORE_SIZE_BYTES = 5L * 1024L * 1024L;

    // Patrón para detectar posibles inyecciones en los campos X.500 (wildcards, paréntesis, objectclass)
    private static final Pattern DANGEROUS_DN_PATTERN = Pattern.compile("[*()\\[\\]{}]|objectclass", Pattern.CASE_INSENSITIVE);

    public static void validateCertificateSafe(X509Certificate cert) throws CertificateException {
        if (cert == null) throw new CertificateException("Certificado nulo");

        // Validación temporal básica
        try {
            cert.checkValidity();
        } catch (CertificateException e) {
            throw new CertificateException("Certificado no válido temporalmente: " + e.getMessage(), e);
        }

        // Validación de algoritmo y longitud de clave (RSA >= 2048)
        PublicKey pk = cert.getPublicKey();
        if (pk instanceof RSAPublicKey) {
            int bits = ((RSAPublicKey) pk).getModulus().bitLength();
            if (bits < 2048) {
                throw new CertificateException("Clave RSA insegura: longitud " + bits + " bits (mínimo 2048)");
            }
        }

        // Prevención: comprobar que el SubjectDN no contiene caracteres peligrosos
        String dn = cert.getSubjectX500Principal() != null ? cert.getSubjectX500Principal().getName() : null;
        if (dn != null && DANGEROUS_DN_PATTERN.matcher(dn).find()) {
            throw new CertificateException("Subject DN contiene caracteres potencialmente peligrosos: " + dn);
        }

        // Comprobar fechas razonables
        Instant notBefore = cert.getNotBefore().toInstant();
        Instant notAfter = cert.getNotAfter().toInstant();
        if (notAfter.isBefore(Instant.now())) {
            throw new CertificateException("Certificado ya expirado");
        }
        if (notBefore.isAfter(Instant.now().plusSeconds(60L * 60L * 24L * 365L * 2L))) {
            throw new CertificateException("Certificado con fecha 'notBefore' inusualmente en el futuro: " + notBefore);
        }
    }
}
