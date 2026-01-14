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
    @ValueSource(strings = {"12345678Z", "00000000T", "11111111H"})
    void testValidarDNI_Validos(String dni) {
        boolean resultado = validacionService.validarNIF(dni);
        assertTrue(resultado, "DNI valido deberia ser aceptado: " + dni);
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678B", "87654321A", "00000000A", "1234567"})
    void testValidarDNI_Invalidos(String dni) {
        boolean resultado = validacionService.validarNIF(dni);
        assertFalse(resultado, "DNI invalido deberia ser rechazado: " + dni);
    }

    @ParameterizedTest
    @ValueSource(strings = {"X0000000T", "Y0000000Z", "Z0000000M"})
    void testValidarNIE_Validos(String nie) {
        boolean resultado = validacionService.validarNIF(nie);
        assertTrue(resultado, "NIE valido deberia ser aceptado: " + nie);
    }

    @ParameterizedTest
    @ValueSource(strings = {"A28017895", "B82568718", "G08169815"})
    void testValidarCIF_Validos(String cif) {
        boolean resultado = validacionService.validarCIF(cif);
        assertTrue(resultado, "CIF valido deberia ser aceptado: " + cif);
    }

    @ParameterizedTest
    @ValueSource(strings = {"test@example.com", "user@domain.es", "correo@empresa.com"})
    void testValidarEmail_Validos(String email) {
        boolean resultado = validacionService.validarEmail(email);
        assertTrue(resultado, "Email valido deberia ser aceptado: " + email);
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalido", "test@", "@domain.com"})
    void testValidarEmail_Invalidos(String email) {
        boolean resultado = validacionService.validarEmail(email);
        assertFalse(resultado, "Email invalido deberia ser rechazado: " + email);
    }

    @ParameterizedTest
    @ValueSource(strings = {"03001", "28001", "46001", "08001", "41001"})
    void testValidarCodigoPostal_Validos(String cp) {
        boolean resultado = validacionService.validarCodigoPostal(cp);
        assertTrue(resultado, "CP valido deberia ser aceptado: " + cp);
    }

    @ParameterizedTest
    @ValueSource(strings = {"00000", "53000", "99999", "1234"})
    void testValidarCodigoPostal_Invalidos(String cp) {
        boolean resultado = validacionService.validarCodigoPostal(cp);
        assertFalse(resultado, "CP invalido deberia ser rechazado: " + cp);
    }

    @Test
    void testValidarImportePositivo() {
        BigDecimal importeValido = new BigDecimal("100.00");
        BigDecimal importeInvalido = new BigDecimal("-10.00");
        BigDecimal importeCero = BigDecimal.ZERO;

        assertTrue(validacionService.validarImportePositivo(importeValido));
        assertFalse(validacionService.validarImportePositivo(importeInvalido));
        assertFalse(validacionService.validarImportePositivo(importeCero));
    }

    @Test
    void testValidarImporteNoNegativo() {
        BigDecimal importeValido = new BigDecimal("100.00");
        BigDecimal importeInvalido = new BigDecimal("-10.00");
        BigDecimal importeCero = BigDecimal.ZERO;

        assertTrue(validacionService.validarImporteNoNegativo(importeValido));
        assertFalse(validacionService.validarImporteNoNegativo(importeInvalido));
        assertTrue(validacionService.validarImporteNoNegativo(importeCero));
    }

    @Test
    void testValidarFechaNoFutura() {
        LocalDate hoy = LocalDate.now();
        LocalDate ayer = hoy.minusDays(1);
        LocalDate manana = hoy.plusDays(1);

        assertTrue(validacionService.validarFechaNoFutura(hoy));
        assertTrue(validacionService.validarFechaNoFutura(ayer));
        assertFalse(validacionService.validarFechaNoFutura(manana));
    }

    @Test
    void testValidarCliente() {
        String nombre = "Panaderia Test";
        String cif = "B82568718";
        String email = "test@panaderia.com";

        ValidacionService.ValidationResult resultado = validacionService.validarCliente(nombre, cif, email);

        assertTrue(resultado.isValid());
        assertEquals(0, resultado.getErrors().size());
    }

    @Test
    void testValidarCliente_ConErrores() {
        String nombre = "";
        String cif = "INVALIDO";
        String email = "invalido";

        ValidacionService.ValidationResult resultado = validacionService.validarCliente(nombre, cif, email);

        assertFalse(resultado.isValid());
        assertTrue(resultado.getErrors().size() > 0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ES9121000418450200051332", "ES6621000418401234567891"})
    void testValidarIBAN_Validos(String iban) {
        boolean resultado = validacionService.validarIBAN(iban);
        assertTrue(resultado, "IBAN valido deberia ser aceptado: " + iban);
    }
}

