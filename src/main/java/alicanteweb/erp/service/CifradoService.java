package alicanteweb.erp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Servicio de cifrado para cumplimiento RGPD
 * - AES-256 para datos personales
 * - BCrypt para contraseÃ±as
 */
@Service
@Slf4j
public class CifradoService {

    @Value("${cifrado.aes.key:DEFAULT_KEY_32_CHARACTERS_MIN!!}")
    private String aesKeyString;

    private final BCryptPasswordEncoder passwordEncoder;

    public CifradoService(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Cifra un texto usando AES-256
     * @param texto Texto a cifrar
     * @return Texto cifrado en Base64
     */
    public String cifrarAES256(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }

        try {
            SecretKey key = getSecretKey();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("Error cifrando con AES-256", e);
            throw new RuntimeException("Error en cifrado AES-256", e);
        }
    }

    /**
     * Descifra un texto cifrado con AES-256
     * @param textoCifrado Texto cifrado en Base64
     * @return Texto original
     */
    public String descifrarAES256(String textoCifrado) {
        if (textoCifrado == null || textoCifrado.isEmpty()) {
            return textoCifrado;
        }

        try {
            SecretKey key = getSecretKey();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decoded = Base64.getDecoder().decode(textoCifrado);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error descifrando con AES-256", e);
            throw new RuntimeException("Error en descifrado AES-256", e);
        }
    }

    /**
     * Cifra una contraseÃ±a usando BCrypt
     * @param password ContraseÃ±a en texto plano
     * @return Hash BCrypt
     */
    public String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("La contraseÃ±a no puede estar vacÃ­a");
        }
        return passwordEncoder.encode(password);
    }

    /**
     * Verifica si una contraseÃ±a coincide con un hash BCrypt
     * @param password ContraseÃ±a en texto plano
     * @param hash Hash BCrypt almacenado
     * @return true si coinciden
     */
    public boolean verificarPassword(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        return passwordEncoder.matches(password, hash);
    }

    /**
     * Genera una clave AES-256 aleatoria
     * @return Clave en Base64 (para configurar en properties)
     */
    public static String generarKeyAES() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256, new SecureRandom());
            SecretKey secretKey = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generando clave AES", e);
        }
    }

    /**
     * Obtiene la clave secreta desde la configuraciÃ³n
     */
    private SecretKey getSecretKey() {
        try {
            // Si la clave es la default, generamos una y avisamos
            if (aesKeyString.equals("DEFAULT_KEY_32_CHARACTERS_MIN!!")) {
                log.warn("âš ï¸ Usando clave AES por defecto. CONFIGURA una clave segura en application.properties");
                log.warn("   Genera una con: CifradoService.generarKeyAES()");
            }

            // Asegurar que la clave tenga al menos 32 caracteres para AES-256
            String key = aesKeyString;
            if (key.length() < 32) {
                key = String.format("%-32s", key).replace(' ', '0');
            } else if (key.length() > 32) {
                key = key.substring(0, 32);
            }

            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo clave AES", e);
        }
    }

    /**
     * Genera un token aleatorio seguro (para recuperaciÃ³n de contraseÃ±a, etc.)
     * @param length Longitud del token
     * @return Token aleatorio
     */
    public String generarTokenSeguro(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}


