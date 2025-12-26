package alicanteweb.erp.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para FacturaValidacionService
 */
@SpringBootTest
class FacturaValidacionServiceTest {

    @Autowired
    private FacturaValidacionService validacionService;

    @Test
    void testValidarCIF() {
        // CIFs válidos
        assertTrue(validacionService.validarCifNif("A12345674"));
        assertTrue(validacionService.validarCifNif("B12345678"));

        // CIFs inválidos
        assertFalse(validacionService.validarCifNif("A12345675")); // Dígito control incorrecto
        assertFalse(validacionService.validarCifNif("12345678A")); // Formato incorrecto
    }

    @Test
    void testValidarNIF() {
        // NIFs válidos
        assertTrue(validacionService.validarCifNif("12345678Z"));
        assertTrue(validacionService.validarCifNif("87654321X"));

        // NIFs inválidos
        assertFalse(validacionService.validarCifNif("12345678A")); // Letra incorrecta
        assertFalse(validacionService.validarCifNif("1234567Z")); // Muy corto
    }

    @Test
    void testValidarNIE() {
        // NIEs válidos
        assertTrue(validacionService.validarCifNif("X1234567L"));
        assertTrue(validacionService.validarCifNif("Y7654321A"));
        assertTrue(validacionService.validarCifNif("Z9876543R"));

        // NIEs inválidos
        assertFalse(validacionService.validarCifNif("X1234567Z")); // Letra incorrecta
        assertFalse(validacionService.validarCifNif("W1234567L")); // Primera letra inválida
    }

    @Test
    void testValidarDocumentoVacio() {
        assertFalse(validacionService.validarCifNif(""));
        assertFalse(validacionService.validarCifNif(null));
        assertFalse(validacionService.validarCifNif("   "));
    }

    @Test
    void testValidarCIFConEspacios() {
        // Debe funcionar con espacios (se trimean)
        assertTrue(validacionService.validarCifNif("  A12345674  "));
    }

    @Test
    void testValidarCIFMinusculas() {
        // Debe funcionar con minúsculas (se convierten a mayúsculas)
        assertTrue(validacionService.validarCifNif("a12345674"));
        assertTrue(validacionService.validarCifNif("b12345678"));
    }
}

