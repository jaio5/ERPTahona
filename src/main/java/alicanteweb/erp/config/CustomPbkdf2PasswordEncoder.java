package alicanteweb.erp.config;

import alicanteweb.erp.exception.ErpException;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Implementación ligera de PBKDF2 que genera hashes compactos en formato:
 * base64(salt) + "$" + base64(derivedKey)
 * - Salt aleatorio por hash de 16 bytes
 * - Algoritmo: PBKDF2WithHmacSHA256
 * - Iteraciones/configurable
 */
public class CustomPbkdf2PasswordEncoder implements PasswordEncoder {

    private final String secret;
    private final int iterations;
    private final int hashWidthBits;
    private final SecureRandom secureRandom = new SecureRandom();

    public CustomPbkdf2PasswordEncoder(String secret, int iterations, int hashWidthBits) {
        this.secret = secret == null ? "" : secret;
        this.iterations = Math.max(10000, iterations);
        this.hashWidthBits = Math.max(128, hashWidthBits);
    }

    @Override
    public String encode(CharSequence rawPassword) {
        try {
            byte[] salt = new byte[16];
            secureRandom.nextBytes(salt);

            byte[] derived = pbkdf2(rawPassword.toString(), salt, iterations, hashWidthBits);

            String saltB64 = Base64.getEncoder().encodeToString(salt);
            String derivedB64 = Base64.getEncoder().encodeToString(derived);

            return saltB64 + "$" + derivedB64;
        } catch (Exception e) {
            throw new ErpException("Error generando hash PBKDF2", e);
        }
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        try {
            if (encodedPassword == null || !encodedPassword.contains("$")) return false;
            String[] parts = encodedPassword.split("\\$", 2);
            if (parts.length != 2) return false;
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expected = Base64.getDecoder().decode(parts[1]);

            byte[] derived = pbkdf2(rawPassword.toString(), salt, iterations, expected.length * 8);

            if (derived.length != expected.length) return false;

            // Comparación en tiempo constante usando MessageDigest.isEqual
            return MessageDigest.isEqual(derived, expected);
        } catch (Exception e) {
            return false;
        }
    }

    private byte[] pbkdf2(String password, byte[] salt, int iterations, int hashWidthBits) throws Exception {
        PBEKeySpec spec = new PBEKeySpec((password + secret).toCharArray(), salt, iterations, hashWidthBits);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return skf.generateSecret(spec).getEncoded();
    }
}
