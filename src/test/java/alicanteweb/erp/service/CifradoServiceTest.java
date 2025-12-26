package alicanteweb.erp.service;
}
    }
        assertTrue(cifradoService.verificarPassword(password, hash2));
        assertTrue(cifradoService.verificarPassword(password, hash1));
        assertNotEquals(hash1, hash2); // BCrypt usa salt diferente cada vez
        // Then

        String hash2 = cifradoService.hashPassword(password);
        String hash1 = cifradoService.hashPassword(password);
        // When

        String password = "miPassword123";
        // Given
    void testHashPasswordsDiferentes() {
    @Test

    }
        assertEquals(textoVacio, cifrado); // Texto vacío no se cifra
        // Then

        String cifrado = cifradoService.cifrarAES256(textoVacio);
        // When

        String textoVacio = "";
        // Given
    void testCifrarTextoVacio() {
    @Test

    }
        assertTrue(token1.length() > 0);
        assertNotEquals(token1, token2); // Deben ser únicos
        assertNotNull(token2);
        assertNotNull(token1);
        // Then

        String token2 = cifradoService.generarTokenSeguro(32);
        String token1 = cifradoService.generarTokenSeguro(32);
        // When
    void testGenerarTokenSeguro() {
    @Test

    }
        assertFalse(invalido);
        assertTrue(valido);
        // Then

        boolean invalido = cifradoService.verificarPassword("passwordIncorrecto", hash);
        boolean valido = cifradoService.verificarPassword(password, hash);
        // When

        String hash = cifradoService.hashPassword(password);
        String password = "miPasswordSeguro123";
        // Given
    void testVerificarPassword() {
    @Test

    }
        assertTrue(hash.startsWith("$2a$")); // BCrypt format
        assertNotEquals(password, hash);
        assertNotNull(hash);
        // Then

        String hash = cifradoService.hashPassword(password);
        // When

        String password = "miPasswordSeguro123";
        // Given
    void testHashPassword() {
    @Test

    }
        assertEquals(textoOriginal, descifrado);
        assertNotEquals(textoOriginal, cifrado);
        assertNotNull(cifrado);
        // Then

        String descifrado = cifradoService.descifrarAES256(cifrado);
        String cifrado = cifradoService.cifrarAES256(textoOriginal);
        // When

        String textoOriginal = "Datos sensibles del cliente";
        // Given
    void testCifrarYDescifrarAES256() {
    @Test

    private CifradoService cifradoService;
    @Autowired

class CifradoServiceTest {
@SpringBootTest
 */
 * Tests para CifradoService
/**

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;


