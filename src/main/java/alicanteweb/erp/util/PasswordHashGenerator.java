package alicanteweb.erp.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilidad para generar hashes BCrypt de contraseñas
 * Ejecutar este programa para generar el hash de una contraseña
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "admin";
        String hash = encoder.encode(password);

        System.out.println("========================================");
        System.out.println("GENERADOR DE HASH BCRYPT");
        System.out.println("========================================");
        System.out.println("Contraseña: " + password);
        System.out.println("Hash BCrypt: " + hash);
        System.out.println("========================================");
        System.out.println();
        System.out.println("SQL para actualizar en la base de datos:");
        System.out.println("UPDATE users SET password='" + hash + "' WHERE username='admin';");
        System.out.println("========================================");

        // Verificar que el hash funciona
        boolean matches = encoder.matches(password, hash);
        System.out.println("Verificación: " + (matches ? "✓ CORRECTO" : "✗ ERROR"));
    }
}

