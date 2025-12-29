package alicanteweb.erp.test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestBCrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "admin";
        String hash1 = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        String hash2 = "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG";

        System.out.println("=".repeat(60));
        System.out.println("TEST BCRYPT - Verificación de Hashes");
        System.out.println("=".repeat(60));
        System.out.println();
        System.out.println("Password a verificar: " + password);
        System.out.println();

        System.out.println("Hash 1: " + hash1);
        boolean match1 = encoder.matches(password, hash1);
        System.out.println("Coincide: " + (match1 ? "✓ SÍ" : "✗ NO"));
        System.out.println();

        System.out.println("Hash 2: " + hash2);
        boolean match2 = encoder.matches(password, hash2);
        System.out.println("Coincide: " + (match2 ? "✓ SÍ" : "✗ NO"));
        System.out.println();

        System.out.println("Generando nuevo hash para 'admin':");
        String newHash = encoder.encode(password);
        System.out.println(newHash);
        System.out.println("Verificando nuevo hash: " + (encoder.matches(password, newHash) ? "✓ SÍ" : "✗ NO"));
        System.out.println();
        System.out.println("=".repeat(60));
        System.out.println("SQL para actualizar:");
        System.out.println("UPDATE users SET password='" + newHash + "' WHERE username='admin';");
        System.out.println("=".repeat(60));
    }
}

