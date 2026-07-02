package alicanteweb.erp.util;

import alicanteweb.erp.entities.enums.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class UtilityCoverageTest {

    @Test
    void hashUtilsProduceRepresentacionesConsistentes() throws Exception {
        byte[] bytes = "abc".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(HashUtils.sha256(bytes), HashUtils.sha256("abc"));
        assertEquals(HashUtils.sha256Base64(bytes), HashUtils.sha256Base64("abc"));
        assertEquals(HashUtils.sha256Base64UrlSafe(bytes), HashUtils.sha256Base64UrlSafe("abc"));
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                HashUtils.sha256Hex("abc"));
    }

    @Test
    void enumsExponenEtiquetas() {
        assertEquals("Planificada", EstadoHojaRutaEnum.PLANIFICADA.getEtiqueta());
        assertEquals("Confirmado", EstadoPedidoEnum.CONFIRMADO.getEtiqueta());
        assertEquals("Caducado", EstadoLoteEnum.CADUCADO.getEtiqueta());
        assertEquals("Completada", EstadoRgpdSolicitudEnum.COMPLETADA.getEtiqueta());
        assertEquals("Confirmada", EstadoRecepcionEnum.CONFIRMADA.getEtiqueta());
        assertEquals("Convertido a pedido", EstadoPresupuestoEnum.CONVERTIDO.getEtiqueta());
        assertEquals("En curso", EstadoOrdenProduccionEnum.EN_CURSO.getEtiqueta());
        assertEquals("Rectificada", EstadoFacturaEnum.RECTIFICADA.getEtiqueta());
        assertEquals("Facturado", EstadoAlbaranEnum.FACTURADO.getEtiqueta());

        assertTrue(EstadoHojaRutaEnum.values().length > 0);
        assertTrue(EstadoPedidoEnum.values().length > 0);
        assertTrue(EstadoLoteEnum.values().length > 0);
        assertTrue(EstadoRgpdSolicitudEnum.values().length > 0);
        assertTrue(EstadoRecepcionEnum.values().length > 0);
        assertTrue(EstadoPresupuestoEnum.values().length > 0);
        assertTrue(EstadoOrdenProduccionEnum.values().length > 0);
        assertTrue(EstadoFacturaEnum.values().length > 0);
        assertTrue(EstadoAlbaranEnum.values().length > 0);
    }

    @Test
    void financialMathCalculaCorrectamente() {
        BigDecimal base = new BigDecimal("100.00");
        BigDecimal iva21 = new BigDecimal("21");
        BigDecimal descuento10 = new BigDecimal("10");

        assertEquals(new BigDecimal("21.00"), FinancialMath.porcentaje(base, iva21));
        assertEquals(new BigDecimal("10.00"), FinancialMath.porcentaje(base, descuento10));
        assertEquals(BigDecimal.ZERO, FinancialMath.porcentaje(base, BigDecimal.ZERO));
        assertEquals(BigDecimal.ZERO, FinancialMath.porcentaje(null, iva21));
        assertEquals(BigDecimal.ZERO, FinancialMath.porcentaje(base, null));

        BigDecimal subtotal = FinancialMath.subtotalConDescuento(new BigDecimal("2"), new BigDecimal("50"), descuento10);
        assertEquals(new BigDecimal("90.00"), subtotal);

        BigDecimal sinDescuento = FinancialMath.subtotalConDescuento(new BigDecimal("3"), new BigDecimal("10"), BigDecimal.ZERO);
        assertEquals(new BigDecimal("30.00"), sinDescuento);
    }
}
