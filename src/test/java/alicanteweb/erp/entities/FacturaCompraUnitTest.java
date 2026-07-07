package alicanteweb.erp.entities;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FacturaCompraUnitTest {

    @Test
    void calcularTotal_y_getters_canónicos_funcionan() {
        FacturaCompra fc = new FacturaCompra();
        fc.setBaseImponible(new BigDecimal("100.00"));
        fc.setImporteIva(new BigDecimal("21.00"));
        fc.setImporteRecargo(new BigDecimal("0.00"));
        fc.setImporteRetencion(new BigDecimal("0.00"));

        fc.calcularTotal();

        assertEquals(new BigDecimal("121.00"), fc.getTotal());
        assertEquals(new BigDecimal("121.00"), fc.getTotal());
        assertEquals(new BigDecimal("21.00"), fc.getImporteIva());

        // marcar como pagada
        fc.marcarComoPagada(LocalDate.now());
        assertTrue(fc.getPagada());
        assertEquals("PAGADA", fc.getEstado());

        // marcar como contabilizada
        fc.setEstado("PENDIENTE");
        fc.marcarComoContabilizada();
        assertTrue(fc.getContabilizada());
        assertNotNull(fc.getFechaContabilizacion());

        // fecha alias
        fc.setFecha(LocalDate.of(2026,1,1));
        assertEquals(LocalDate.of(2026,1,1), fc.getFecha());
    }
}
