package alicanteweb.erp.entities;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Guard de inalterabilidad RRSIF: una factura emitida no admite cambios en su
 * contenido fiscal ni borrado; sí admite cobro, anulación y metadatos VeriFactu.
 * Se invocan los callbacks JPA directamente (simulando load → modificación → flush).
 */
class FacturaInalterabilidadTest {

    private Factura facturaEmitida() {
        Factura factura = new Factura();
        factura.setNumero("FA-TPV-2026-0001");
        factura.setSerie("TPV");
        factura.setFecha(LocalDate.of(2026, 7, 1));
        factura.setEstado("EMITIDA");
        factura.setTotal(new BigDecimal("100.00"));
        factura.setBaseImponible(new BigDecimal("90.91"));
        factura.setTotalIva(new BigDecimal("9.09"));
        simularCargaDesdeBd(factura);
        return factura;
    }

    private void simularCargaDesdeBd(Factura factura) {
        ReflectionTestUtils.invokeMethod(factura, "capturarEstadoPersistido");
    }

    private void simularFlushUpdate(Factura factura) {
        ReflectionTestUtils.invokeMethod(factura, "protegerContenidoFiscal");
    }

    private void simularDelete(Factura factura) {
        ReflectionTestUtils.invokeMethod(factura, "protegerBorrado");
    }

    @Test
    void bloqueaCambiosDeContenidoFiscalEnFacturaEmitida() {
        Factura factura = facturaEmitida();
        factura.setTotal(new BigDecimal("999.99"));
        assertThrows(IllegalStateException.class, () -> simularFlushUpdate(factura));

        Factura otra = facturaEmitida();
        otra.setNumero("FA-TPV-2026-9999");
        assertThrows(IllegalStateException.class, () -> simularFlushUpdate(otra));
    }

    @Test
    void permiteCobroYMetadatosVerifactuEnFacturaEmitida() {
        Factura factura = facturaEmitida();
        factura.setPagado(new BigDecimal("100.00"));
        factura.setPagada(true);
        factura.setVerifactuEnviada(true);
        factura.setVerifactuHash("abc123");
        assertDoesNotThrow(() -> simularFlushUpdate(factura));
    }

    @Test
    void permiteAnularPeroNoVolverABorrador() {
        Factura anulable = facturaEmitida();
        anulable.setEstado("ANULADA");
        assertDoesNotThrow(() -> simularFlushUpdate(anulable));

        Factura lavada = facturaEmitida();
        lavada.setEstado("BORRADOR");
        assertThrows(IllegalStateException.class, () -> simularFlushUpdate(lavada));

        // ANULADA es terminal
        Factura resucitada = facturaEmitida();
        resucitada.setEstado("ANULADA");
        simularCargaDesdeBd(resucitada);
        resucitada.setEstado("EMITIDA");
        assertThrows(IllegalStateException.class, () -> simularFlushUpdate(resucitada));
    }

    @Test
    void noRestringeFacturasEnBorradorORevision() {
        Factura borrador = new Factura();
        borrador.setNumero("BORRADOR-1");
        borrador.setEstado("BORRADOR");
        borrador.setTotal(new BigDecimal("10.00"));
        simularCargaDesdeBd(borrador);

        borrador.setTotal(new BigDecimal("20.00"));
        borrador.setEstado("REVISION");
        assertDoesNotThrow(() -> simularFlushUpdate(borrador));
        assertDoesNotThrow(() -> simularDelete(borrador));
    }

    @Test
    void bloqueaBorradoDeFacturaEmitidaOEnviadaAVerifactu() {
        assertThrows(IllegalStateException.class, () -> simularDelete(facturaEmitida()));

        Factura enviada = new Factura();
        enviada.setNumero("FA-1");
        enviada.setEstado("BORRADOR");
        enviada.setVerifactuEnviada(true);
        assertThrows(IllegalStateException.class, () -> simularDelete(enviada));
    }

    @Test
    void bloqueaLineasDeFacturaEmitidaYPermiteLasDeBorrador() {
        FacturaLinea lineaEmitida = new FacturaLinea();
        lineaEmitida.setFactura(facturaEmitida());
        assertThrows(IllegalStateException.class,
                () -> ReflectionTestUtils.invokeMethod(lineaEmitida, "protegerLineaDeFacturaEmitida"));

        Factura borrador = new Factura();
        borrador.setEstado("BORRADOR");
        simularCargaDesdeBd(borrador);
        FacturaLinea lineaBorrador = new FacturaLinea();
        lineaBorrador.setFactura(borrador);
        assertDoesNotThrow(
                () -> ReflectionTestUtils.invokeMethod(lineaBorrador, "protegerLineaDeFacturaEmitida"));

        // Factura recién creada (sin snapshot aún): la emisión no debe bloquearse
        FacturaLinea lineaNueva = new FacturaLinea();
        lineaNueva.setFactura(new Factura());
        assertDoesNotThrow(
                () -> ReflectionTestUtils.invokeMethod(lineaNueva, "protegerLineaDeFacturaEmitida"));
    }
}
