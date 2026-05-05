package alicanteweb.erp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;

/**
 * Servicio de cifrado para cumplimiento RGPD
 * - AES-256-GCM para datos personales (autenticado)
 * - PBKDF2 (o el encoder configurado) para contraseñas
 */
@Service
@Slf4j
public class CifradoService {

    @Value("${cifrado.aes.key:DEFAULT_KEY_32_CHARACTERS_MIN!!}")
    private String aesKeyString;

    @Value("${cifrado.aes.use-pbkdf2:false}")
    private boolean usePbkdf2;

    @Value("${cifrado.aes.pbkdf2.salt:}")
    private String pbkdf2Salt;

    @Value("${cifrado.aes.pbkdf2.iterations:100000}")
    private int pbkdf2Iterations;

    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int GCM_IV_LENGTH = 12; // bytes (96 bits recommended)

    // Máxima longitud soportada por la columna 'password' en la entidad Usuario
    private static final int MAX_PASSWORD_COLUMN_LENGTH = 4096;

    public CifradoService(PasswordEncoder passwordEncoder, Environment environment) {
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @PostConstruct
    private void validateAesKeyInProduction() {
        // Si estamos en producción, exigir que la clave AES sea Base64 y represente 16/24/32 bytes
        try {
            String[] profiles = environment.getActiveProfiles();
            boolean prod = false;
            for (String p : profiles) {
                if (p != null && (p.equalsIgnoreCase("prod") || p.equalsIgnoreCase("production"))) { prod = true; break; }
            }

            if (prod) {
                if (aesKeyString == null || aesKeyString.isBlank() || aesKeyString.equals("DEFAULT_KEY_32_CHARACTERS_MIN!!")) {
                    throw new IllegalStateException("Clave AES no configurada para producción: establece 'cifrado.aes.key' con una clave Base64 segura");
                }
                try {
                    byte[] decoded = java.util.Base64.getDecoder().decode(aesKeyString);
                    if (!(decoded.length == 16 || decoded.length == 24 || decoded.length == 32)) {
                        throw new IllegalStateException("La clave AES en 'cifrado.aes.key' debe ser Base64 que represente 16/24/32 bytes (preferible 32 para AES-256)");
                    }
                } catch (IllegalArgumentException iae) {
                    throw new IllegalStateException("La clave AES en 'cifrado.aes.key' no es Base64 válida. En producción debe ser Base64 (use CifradoService.generarKeyAES())", iae);
                }
            } else {
                if (aesKeyString == null || aesKeyString.isBlank() || aesKeyString.equals("DEFAULT_KEY_32_CHARACTERS_MIN!!")) {
                    log.warn("╔══════════════════════════════════════════════════════════════╗");
                    log.warn("║  ⚠️  ADVERTENCIA DE SEGURIDAD - CLAVE AES POR DEFECTO       ║");
                    log.warn("║  La clave 'cifrado.aes.key' no está configurada.             ║");
                    log.warn("║  Los datos cifrados en dev NO son seguros en producción.     ║");
                    log.warn("║  Configure 'cifrado.aes.key' en application-prod.properties  ║");
                    log.warn("╚══════════════════════════════════════════════════════════════╝");

                    // Generar una clave de ejemplo para desarrolladores y setearla localmente
                    try {
                        String ejemploKey = generarKeyAES();
                        log.info("Clave AES de ejemplo (no use en producción): {}", ejemploKey);
                        // Usamos setSecretKey para demostrar su utilidad en entornos de desarrollo
                        setSecretKey(ejemploKey);
                    } catch (Exception e) {
                        log.debug("No se pudo generar clave de ejemplo: {}", e.getMessage());
                    }
                } else {
                    // Si hay clave configurada (aunque sea legible), probamos cifrar/descifrar para validar
                    try {
                        String prueba = "verificacion-cifrado";
                        String cifrado = cifrarAES256(prueba);
                        String desc = descifrarAES256(cifrado);
                        if (!prueba.equals(desc)) {
                            log.warn("Verificación cifrado/descifrado falló en entorno no productivo");
                        } else {
                            log.debug("Verificación cifrado OK (entorno no productivo)");
                        }
                    } catch (Exception e) {
                        log.warn("No se pudo verificar cifrado AES en entorno no productivo: {}", e.getMessage());
                    }
                }

                // Generar un token seguro de diagnóstico (no se guarda)
                try {
                    String token = generarTokenSeguro(16);
                    log.debug("Token seguro de diagnóstico generado: {}", token);
                } catch (Exception ignored) {}
             }
        } catch (RuntimeException e) {
            // No encapsular; preferimos fallar rápido en caso de mala configuración en prod
            log.error("Fallo en validación de clave AES en @PostConstruct", e);
            throw e;
        }
    }

    /**
     * Cifra un texto usando AES-256-GCM. Devuelve Base64( IV || ciphertext )
     */
    public String cifrarAES256(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }

        try {
            SecretKey key = getSecretKey();

            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom rnd = new SecureRandom();
            rnd.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] ciphertext = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));

            // Prefijar IV al ciphertext para poder desencriptar luego: [IV||CIPHERTEXT]
            ByteBuffer bb = ByteBuffer.allocate(iv.length + ciphertext.length);
            bb.put(iv);
            bb.put(ciphertext);
            byte[] ivAndCipher = bb.array();

            return Base64.getEncoder().encodeToString(ivAndCipher);
        } catch (Exception e) {
            log.error("Error cifrando con AES-256-GCM", e);
            throw new RuntimeException("Error en cifrado AES-256-GCM", e);
        }
    }

    /**
     * Descifra un texto cifrado con AES-256-GCM (recibe Base64(IV||ciphertext))
     */
    public String descifrarAES256(String textoCifrado) {
        if (textoCifrado == null || textoCifrado.isEmpty()) {
            return textoCifrado;
        }

        try {
            SecretKey key = getSecretKey();

            byte[] ivAndCipher = Base64.getDecoder().decode(textoCifrado);
            if (ivAndCipher.length < GCM_IV_LENGTH) {
                throw new IllegalArgumentException("Texto cifrado inválido");
            }

            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(ivAndCipher, 0, iv, 0, GCM_IV_LENGTH);
            byte[] ciphertext = new byte[ivAndCipher.length - GCM_IV_LENGTH];
            System.arraycopy(ivAndCipher, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] decrypted = cipher.doFinal(ciphertext);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error descifrando con AES-256-GCM", e);
            throw new RuntimeException("Error en descifrado AES-256-GCM", e);
        }
    }

    /**
     * Cifra una contraseña usando el encoder configurado
     * @param password Contraseña en texto plano
     * @return Hash (formato según PasswordEncoder)
     */
    public String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        String hashed = passwordEncoder.encode(password);

        // Registramos la longitud en DEBUG y un prefijo limitado para depuración
        try {
            int len = hashed != null ? hashed.length() : 0;
            String prefix = hashed != null ? hashed.substring(0, Math.min(hashed.length(), 200)) : "null";
            log.debug("Generated password hash length={} prefix={}", len, prefix);

            // Validación: asegurarnos que cabe en la columna 'password'
            if (len > MAX_PASSWORD_COLUMN_LENGTH) {
                log.error("Generated password hash too long: {} (max allowed {})", len, MAX_PASSWORD_COLUMN_LENGTH);
                throw new IllegalStateException("Hash de contraseña demasiado largo: " + len + " (max " + MAX_PASSWORD_COLUMN_LENGTH + ")");
            }
        } catch (Exception e) {
            // Si el logging falla por cualquier motivo, no ocultamos el hash, lanzamos Runtime
            log.error("Error validando hash de contraseña", e);
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        }

        return hashed;
    }

    /**
     * Verifica si una contraseña coincide con un hash
     * @param password Contraseña en texto plano
     * @param hash Hash almacenado o texto plano
     * @return true si coinciden
     */
    public boolean verificarPassword(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }

        // Verificación usando el PasswordEncoder configurado (BCrypt / PBKDF2)
        // NO se permite comparación directa en texto plano por seguridad.
        try {
            boolean match = passwordEncoder.matches(password, hash);
            if (!match) {
                log.warn("NO MATCH - Las credenciales no coinciden");
            }
            return match;
        } catch (Exception e) {
            log.error("Error verificando password: {}", e.getMessage());
            return false;
        }
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
     * Obtiene la clave secreta desde la configuración.
     * Se acepta que `aesKeyString` esté en Base64 (recomendado). Si no, se deriva
     * una clave de 256 bits aplicando SHA-256 sobre el string (menos ideal pero
     * útil para compatibilidad con valores legibles).
     */
    private SecretKey getSecretKey() {
        try {
            if (aesKeyString == null || aesKeyString.isBlank() || aesKeyString.equals("DEFAULT_KEY_32_CHARACTERS_MIN!!")) {
                log.warn("⚠️ Usando clave AES por defecto o no configurada. CONFIGURA una clave segura (Base64) en application.properties");
                log.warn("   Genera una con: CifradoService.generarKeyAES()");
            }

            byte[] keyBytes;
            // Intentar interpretar la clave como Base64
            try {
                keyBytes = Base64.getDecoder().decode(aesKeyString);
                if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
                    // No tiene longitud válida para AES -> fallback
                    keyBytes = new byte[0];
                }
            } catch (IllegalArgumentException ignored) {
                keyBytes = new byte[0];
            }

            if (keyBytes.length == 0) {
                // Fallback: si está habilitado PBKDF2 y hay salt, derivar con PBKDF2;
                // en caso contrario usar SHA-256 (compatibilidad).
                if (usePbkdf2 && pbkdf2Salt != null && !pbkdf2Salt.isBlank()) {
                    try {
                        byte[] saltBytes;
                        try {
                            saltBytes = Base64.getDecoder().decode(pbkdf2Salt);
                        } catch (IllegalArgumentException ex) {
                            // Si no es Base64, usar la cadena directamente como salt (UTF-8)
                            saltBytes = pbkdf2Salt.getBytes(StandardCharsets.UTF_8);
                        }

                        PBEKeySpec spec = new PBEKeySpec(aesKeyString.toCharArray(), saltBytes, pbkdf2Iterations, 256);
                        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                        keyBytes = skf.generateSecret(spec).getEncoded();
                    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                        log.warn("PBKDF2 no disponible o falló la derivación, usando SHA-256 como fallback", e);
                        MessageDigest md = MessageDigest.getInstance("SHA-256");
                        keyBytes = md.digest(aesKeyString.getBytes(StandardCharsets.UTF_8));
                    }
                } else {
                    // Fallback: derivar 32 bytes con SHA-256
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    keyBytes = md.digest(aesKeyString.getBytes(StandardCharsets.UTF_8));
                }
            }

            // Asegurar longitud 32 bytes para AES-256
            if (keyBytes.length != 32) {
                // Si es 16 o 24, no cambiamos; pero preferimos 32
                byte[] tmp = new byte[32];
                System.arraycopy(keyBytes, 0, tmp, 0, Math.min(keyBytes.length, 32));
                keyBytes = tmp;
            }

            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo clave AES", e);
        }
    }

    /**
     * Genera un token aleatorio seguro (para recuperación de contraseña, etc.)
     * @param length Longitud en bytes antes de codificar en Base64 URL-safe
     * @return Token aleatorio (Base64 URL-safe)
     */
    public String generarTokenSeguro(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Setter para pruebas: permite inyectar una clave AES (Base64 o legible) en tests.
     * No recomendado para uso en producción.
     */
    public void setSecretKey(String key) {
        this.aesKeyString = key;
    }
}
