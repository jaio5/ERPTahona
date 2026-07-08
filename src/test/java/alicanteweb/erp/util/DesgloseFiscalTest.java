package alicanteweb.erp.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DesgloseFiscalTest {

    private static BigDecimal bd(String s) { return new BigDecimal(s); }

    private DesgloseFiscal.Linea linea(String cant, String precio, String iva, String descTipo, String descVal) {
        return new DesgloseFiscal.Linea(bd(cant), bd(precio), bd(iva), descTipo, descVal == null ? null : bd(descVal));
    }

    @Test
    void variosTiposSinDescuento() {
        // Modelo real: base 4%=102,94 y 10%=17,82; cuotas 4,12 y 1,78; total 126,66.
        var r = DesgloseFiscal.calcular(List.of(
                linea("6", "2.97", "10", null, null),
                linea("4", "24.16", "4", null, null),
                linea("3", "2.10", "4", null, null)
        ), null, null);

        assertEquals(bd("120.76"), r.base());
        assertEquals(bd("5.90"), r.iva());
        assertEquals(bd("126.66"), r.total());
        // orden ascendente por tipo: 4% primero
        assertEquals(bd("4"), r.porTipo().get(0).tipo());
        assertEquals(bd("102.94"), r.porTipo().get(0).base());
        assertEquals(bd("4.12"), r.porTipo().get(0).cuota());
        assertEquals(bd("17.82"), r.porTipo().get(1).base());
        assertEquals(bd("1.78"), r.porTipo().get(1).cuota());
    }

    @Test
    void descuentoDeLineaPorcentaje() {
        // 10 x 10,00 con 10% dto -> base 90,00; IVA 21% -> 18,90
        var r = DesgloseFiscal.calcular(List.of(
                linea("10", "10.00", "21", DesgloseFiscal.PORCENTAJE, "10")
        ), null, null);
        assertEquals(bd("90.00"), r.base());
        assertEquals(bd("18.90"), r.iva());
        assertEquals(bd("108.90"), r.total());
    }

    @Test
    void descuentoDeLineaImporte() {
        // 10 x 10,00 = 100,00 menos 15 € -> base 85,00
        var r = DesgloseFiscal.calcular(List.of(
                linea("10", "10.00", "21", DesgloseFiscal.IMPORTE, "15")
        ), null, null);
        assertEquals(bd("85.00"), r.base());
        assertEquals(bd("17.85"), r.iva());
    }

    @Test
    void descuentoGlobalPorcentajeSeProrrateaYCuadra() {
        // Dos tipos; 10% de descuento global. Debe cuadrar base + Σcuotas = total.
        var r = DesgloseFiscal.calcular(List.of(
                linea("1", "100.00", "21", null, null),
                linea("1", "100.00", "10", null, null)
        ), DesgloseFiscal.PORCENTAJE, bd("10"));

        assertEquals(bd("20.00"), r.descuentoGlobal());   // 10% de 200
        assertEquals(bd("180.00"), r.base());             // 200 - 20
        // Σcuotas = suma de cuotas por tipo
        BigDecimal sumaCuotas = r.porTipo().stream()
                .map(DesgloseFiscal.TipoIva::cuota).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(r.iva(), sumaCuotas);
        assertEquals(r.base().add(r.iva()), r.total());
        // base por tipo tras prorrateo: cada tipo pierde su 10%
        assertEquals(bd("90.00"), r.porTipo().get(0).base()); // 10%
        assertEquals(bd("90.00"), r.porTipo().get(1).base()); // 21%
    }

    @Test
    void descuentoGlobalImporteNoSuperaLaBase() {
        var r = DesgloseFiscal.calcular(List.of(
                linea("1", "50.00", "21", null, null)
        ), DesgloseFiscal.IMPORTE, bd("999"));
        assertEquals(bd("50.00"), r.descuentoGlobal()); // topado a la base
        assertEquals(bd("0.00"), r.base());
        assertEquals(bd("0.00"), r.iva());
    }

    @Test
    void baseLineaNuncaNegativa() {
        // descuento en importe mayor que el bruto -> base 0
        BigDecimal base = DesgloseFiscal.baseLinea(linea("1", "10.00", "21", DesgloseFiscal.IMPORTE, "50"));
        assertTrue(base.signum() == 0);
    }
}
