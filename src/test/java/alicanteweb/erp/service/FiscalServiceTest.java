package alicanteweb.erp.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FiscalServiceTest {

    private final FiscalService service = new FiscalService();

    @Test
    void calcularIVA_aplicaTiposEspanolesYRounding() {
        assertEquals(new BigDecimal("21.00"), service.calcularIVA(new BigDecimal("100"), "GENERAL"));
        assertEquals(new BigDecimal("10.00"), service.calcularIVA(new BigDecimal("100"), "10%"));
        assertEquals(new BigDecimal("4.00"), service.calcularIVA(new BigDecimal("100"), "SUPER_REDUCIDO"));
        assertEquals(new BigDecimal("0.00"), service.calcularIVA(new BigDecimal("100"), "EXENTO"));
        assertEquals(new BigDecimal("2.10"), service.calcularIVA(new BigDecimal("10.01"), "21%"));
    }

    @Test
    void calcularTotalFactura_sumaIvaYRestaRetencion() {
        BigDecimal total = service.calcularTotalFactura(new BigDecimal("100.00"), "21%", new BigDecimal("15.00"));
        assertEquals(new BigDecimal("106.00"), total);
    }

    @Test
    void validarDocumentos_formatoBasico() {
        assertTrue(service.validarNIF("12345678Z"));
        assertTrue(service.validarCIF("B12345678"));
        assertFalse(service.validarNIF("123"));
        assertFalse(service.validarCIF("Z123"));
    }
}
