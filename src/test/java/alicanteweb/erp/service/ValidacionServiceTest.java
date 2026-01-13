package alicanteweb.erp.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para ValidacionService
 */
class ValidacionServiceTest {

    private final ValidacionService validacionService = new ValidacionService();

    @ParameterizedTest
    @ValueSource(strings = {"12345678A", "87654321B", "00000000T"})
    void testValidarDNI_Validos(String dni) {
        // When
        boolean resultado = validacionService.validarNIF(dni);

        // Then
        assertTrue(resultado, "DNI válido debería ser aceptado: " + dni);
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678B", "87654321A", "00000000A", "1234567", "123456789"})
    void testValidarDNI_Invalidos(String dni) {
        // When
        boolean resultado = validacionService.validarNIF(dni);

        // Then
        assertFalse(resultado, "DNI inválido debería ser rechazado: " + dni);
    }

    @ParameterizedTest
    @ValueSource(strings = {"X1234567L", "Y7654321Z", "Z1111111Z"})
    void testValidarNIE_Validos(String nie) {
        // When
        boolean resultado = validacionService.validarNIF(nie);

        // Then
        assertTrue(resultado, "NIE válido debería ser aceptado: " + nie);
    }

    @ParameterizedTest
    @ValueSource(strings = {"A12345678", "B87654321", "G12345678"})
    void testValidarCIF_Validos(String cif) {
        // When
        boolean resultado = validacionService.validarCIF(cif);

        // Then
        assertTrue(resultado, "CIF válido debería ser aceptado: " + cif);
    }

    @ParameterizedTest
    @ValueSource(strings = {"test@example.com", "user@domain.es", "correo@empresa.com"})
    void testValidarEmail_Validos(String email) {
        // When
        boolean resultado = validacionService.validarEmail(email);

        // Then
        assertTrue(resultado, "Email válido debería ser aceptado: " + email);
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalido", "test@", "@domain.com", "test@domain"})
    void testValidarEmail_Invalidos(String email) {
        // When
        boolean resultado = validacionService.validarEmail(email);

        // Then
        assertFalse(resultado, "Email inválido debería ser rechazado: " + email);
    }

    @ParameterizedTest
    @ValueSource(strings = {"03001", "28001", "46001", "08001", "41001"})
    void testValidarCodigoPostal_Validos(String cp) {
        // When
        boolean resultado = validacionService.validarCodigoPostal(cp);

        // Then
        assertTrue(resultado, "CP válido debería ser aceptado: " + cp);
    }

    @ParameterizedTest
    @ValueSource(strings = {"00000", "53000", "99999", "1234", "123456"})
    void testValidarCodigoPostal_Invalidos(String cp) {
        // When
        boolean resultado = validacionService.validarCodigoPostal(cp);

        // Then
        assertFalse(resultado, "CP inválido debería ser rechazado: " + cp);
    }

    @Test
    void testValidarImportePositivo() {
        // Given
        BigDecimal importeValido = new BigDecimal("100.00");
        BigDecimal importeInvalido = new BigDecimal("-10.00");
        BigDecimal importeCero = BigDecimal.ZERO;

        // Then
        assertTrue(validacionService.validarImportePositivo(importeValido));
        assertFalse(validacionService.validarImportePositivo(importeInvalido));
        assertFalse(validacionService.validarImportePositivo(importeCero));
    }

    @Test
    void testValidarImporteNoNegativo() {
        // Given
        BigDecimal importeValido = new BigDecimal("100.00");
        BigDecimal importeInvalido = new BigDecimal("-10.00");
        BigDecimal importeCero = BigDecimal.ZERO;

        // Then
        assertTrue(validacionService.validarImporteNoNegativo(importeValido));
        assertFalse(validacionService.validarImporteNoNegativo(importeInvalido));
        assertTrue(validacionService.validarImporteNoNegativo(importeCero));
    }

    @Test
    void testValidarFechaNoFutura() {
        // Given
        LocalDate hoy = LocalDate.now();
        LocalDate ayer = hoy.minusDays(1);
        LocalDate manana = hoy.plusDays(1);

        // Then
        assertTrue(validacionService.validarFechaNoFutura(hoy));
        assertTrue(validacionService.validarFechaNoFutura(ayer));
        assertFalse(validacionService.validarFechaNoFutura(manana));
    }

    @Test
    void testValidarCliente() {
        // Given
        String nombre = "Panadería Test";
        String cif = "B12345678";
        String email = "test@panaderia.com";
        String cp = "03001";

        // When
        ValidacionService.ValidationResult resultado = validacionService.validarCliente(nombre, cif, email, cp);

        // Then
        assertTrue(resultado.isValid());
        assertEquals(0, resultado.getErrores().size());
    }

    @Test
    void testValidarCliente_ConErrores() {
        // Given
        String nombre = "";
        String cif = "INVALIDO";
        String email = "invalido";
        String cp = "99999";

        // When
        ValidacionService.ValidationResult resultado = validacionService.validarCliente(nombre, cif, email, cp);

        // Then
        assertFalse(resultado.isValid());
        assertTrue(resultado.getErrores().size() > 0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ES9121000418450200051332", "ES6621000418401234567891"})
    void testValidarIBAN_Validos(String iban) {
        // When
        boolean resultado = validacionService.validarIBAN(iban);

        // Then
        assertTrue(resultado, "IBAN válido debería ser aceptado: " + iban);
    }
}

