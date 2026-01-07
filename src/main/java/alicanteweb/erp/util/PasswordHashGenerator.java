package alicanteweb.erp.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class PasswordHashGenerator implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(PasswordHashGenerator.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "admin";
        String hash = encoder.encode(password);

        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  GENERADOR DE HASH BCRYPT                                ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println("\nPassword: " + password);
        System.out.println("Hash BCrypt: " + hash);
        System.out.println("Longitud: " + hash.length() + " caracteres");

        // Verificar que el hash funciona
        boolean matches = encoder.matches(password, hash);
        System.out.println("\n✓ Verificación del hash: " + (matches ? "✅ CORRECTO" : "❌ INCORRECTO"));

        // Probar con el hash que está en la BD
        String hashBD = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        boolean matchesBD = encoder.matches(password, hashBD);
        System.out.println("\n✓ Hash en BD: " + hashBD);
        System.out.println("✓ Verificación con hash de BD: " + (matchesBD ? "✅ CORRECTO" : "❌ INCORRECTO"));

        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  SCRIPT SQL PARA ACTUALIZAR                              ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println("\nUPDATE users SET password = '" + hash + "' WHERE username = 'admin';");
        System.out.println();
    }
}

