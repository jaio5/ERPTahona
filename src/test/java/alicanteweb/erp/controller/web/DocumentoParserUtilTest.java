package alicanteweb.erp.controller.web;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DocumentoParserUtilTest {

    @Test
    void filasVaciasOConCantidadCeroDanListaVaciaNoNull() {
        List<Map<String, Object>> lineas = DocumentoParserUtil.construirLineas(
                new String[]{"", "3"}, new String[]{"5", "0"}, new String[]{"1", "1"}, new String[]{"4", "4"});
        assertNotNull(lineas);
        assertTrue(lineas.isEmpty());
    }

    @Test
    void sinArraysDelFormularioDaListaVacia() {
        List<Map<String, Object>> lineas = DocumentoParserUtil.construirLineas(null, null, null, null, null, null);
        assertNotNull(lineas);
        assertTrue(lineas.isEmpty());
    }

    @Test
    void parseDecimalAceptaComaPuntoYMiles() {
        assertEquals(new BigDecimal("12.50"), DocumentoParserUtil.parseDecimal("12,50"));
        assertEquals(new BigDecimal("1234.50"), DocumentoParserUtil.parseDecimal("1.234,50"));
        assertEquals(new BigDecimal("12.5"), DocumentoParserUtil.parseDecimal("12.5"));
        assertEquals(new BigDecimal("1234.50"), DocumentoParserUtil.parseDecimal("1,234.50"));
        assertEquals(new BigDecimal("2.5"), DocumentoParserUtil.parseDecimal(" 2,5 "));
    }

    @Test
    void parseDecimalMantieneSuContrato() {
        assertEquals(BigDecimal.ZERO, DocumentoParserUtil.parseDecimal(null));
        assertEquals(BigDecimal.ZERO, DocumentoParserUtil.parseDecimal("  "));
        assertThrows(NumberFormatException.class, () -> DocumentoParserUtil.parseDecimal("abc"));
    }

    @Test
    void parseDecimalCampoDaNullSiVacioYMensajeLegibleSiInvalido() {
        assertNull(DocumentoParserUtil.parseDecimalCampo("", "Importe no válido"));
        assertNull(DocumentoParserUtil.parseDecimalCampo(null, "Importe no válido"));
        assertEquals(new BigDecimal("100.50"), DocumentoParserUtil.parseDecimalCampo("100,50", "Importe no válido"));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> DocumentoParserUtil.parseDecimalCampo("abc", "Importe no válido"));
        assertEquals("Importe no válido: abc", e.getMessage());
    }
}
