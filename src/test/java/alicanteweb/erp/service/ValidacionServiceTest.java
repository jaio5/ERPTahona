package alicanteweb.erp.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ValidacionServiceTest {

    private final ValidacionService service = new ValidacionService();

    @Test
    void validaDniNieCifConDigitoControl() {
        assertTrue(service.validarDNI("12345678Z"));
        assertFalse(service.validarDNI("12345678A"));
        assertTrue(service.validarNIE("X1234567L"));
        assertTrue(service.validarCIF("B12345674"));
        assertFalse(service.validarCIF("B12345670"));
    }

    @Test
    void validaContactoYBanco() {
        assertTrue(service.validarEmail("cliente@example.com"));
        assertFalse(service.validarEmail("cliente@"));
        assertTrue(service.validarCodigoPostal("03001"));
        assertFalse(service.validarCodigoPostal("99001"));
        assertTrue(service.validarTelefono("600 123 456"));
        assertFalse(service.validarTelefono("800123456"));
        assertTrue(service.validarIBAN("ES9121000418450200051332"));
        assertTrue(service.validarCCC("21000418450200051332"));
    }

    @Test
    void validaImportesFechasYObjetosCompuestos() {
        assertTrue(service.validarImportePositivo(new BigDecimal("0.01")));
        assertFalse(service.validarImportePositivo(BigDecimal.ZERO));
        assertTrue(service.validarFechaNoFutura(LocalDate.now()));
        assertFalse(service.validarFechaNoFutura(LocalDate.now().plusDays(1)));

        ValidacionService.ValidationResult cliente = service.validarCliente("", "B12345674", "bad");
        assertFalse(cliente.isValid());
        assertEquals("El nombre es obligatorio", cliente.getError("nombre"));
        assertEquals("El email no es valido", cliente.getError("email"));

        ValidacionService.ValidationResult factura = service.validarFactura("", LocalDate.now().plusDays(1), new BigDecimal("-1"));
        assertFalse(factura.isValid());
        assertEquals("El numero de factura es obligatorio", factura.getError("numero"));
        assertEquals("La fecha no puede ser futura", factura.getError("fecha"));
        assertEquals("El total no puede ser negativo", factura.getError("total"));
    }
}
