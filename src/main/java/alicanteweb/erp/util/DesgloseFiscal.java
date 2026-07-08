package alicanteweb.erp.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Cálculo fiscal de un documento (factura/albarán): base e IVA desglosados por tipo,
 * aplicando el descuento de cada línea y, sobre el conjunto, un descuento global
 * <b>prorrateado proporcionalmente entre los tipos de IVA</b> (imprescindible para que el
 * desglose que se remite a la AEAT y el que se imprime cuadren al céntimo).
 *
 * <p>Es la <b>fuente única de verdad</b>: la usan tanto {@code DocumentoService} (para persistir
 * los totales) como {@code ImpresionService} (para renderizar el PDF), de modo que ambos nunca
 * puedan divergir. Es puro (sin dependencias de Spring/JPA) y por tanto fácilmente testeable.
 */
public final class DesgloseFiscal {

    public static final String PORCENTAJE = "PORCENTAJE";
    public static final String IMPORTE = "IMPORTE";

    private DesgloseFiscal() {}

    /** Una línea de entrada para el cálculo. */
    public record Linea(BigDecimal cantidad, BigDecimal precio, BigDecimal iva,
                        String descuentoTipo, BigDecimal descuentoValor) {}

    /** Base y cuota resultantes para un tipo de IVA. */
    public record TipoIva(BigDecimal tipo, BigDecimal base, BigDecimal cuota) {}

    /** Resultado global del documento. */
    public record Resultado(List<TipoIva> porTipo, BigDecimal base, BigDecimal iva,
                            BigDecimal descuentoGlobal, BigDecimal total) {}

    /** Base neta de una línea: cantidad·precio menos su descuento (nunca negativa). */
    public static BigDecimal baseLinea(Linea l) {
        BigDecimal bruto = nz(l.cantidad()).multiply(nz(l.precio()));
        BigDecimal desc = importeDescuento(bruto, l.descuentoTipo(), l.descuentoValor());
        return bruto.subtract(desc).max(BigDecimal.ZERO).setScale(FinancialMath.SCALE, FinancialMath.ROUND);
    }

    /**
     * Calcula el desglose completo. El descuento global se aplica sobre la suma de las bases de
     * línea (ya netas de su propio descuento) y se reparte entre tipos de IVA en proporción a su
     * base; la última porción absorbe el redondeo para que la suma sea exacta.
     */
    public static Resultado calcular(List<Linea> lineas, String descGlobalTipo, BigDecimal descGlobalValor) {
        // 1) Base por tipo de IVA tras el descuento de cada línea (orden ascendente por tipo).
        Map<BigDecimal, BigDecimal> basePorTipo = new TreeMap<>();
        if (lineas != null) {
            for (Linea l : lineas) {
                basePorTipo.merge(nz(l.iva()), baseLinea(l), BigDecimal::add);
            }
        }
        BigDecimal baseBruta = basePorTipo.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal descGlobal = importeDescuento(baseBruta, descGlobalTipo, descGlobalValor);

        // 2) Prorratear el descuento global y calcular cuota por tipo.
        List<TipoIva> porTipo = new ArrayList<>();
        BigDecimal baseTotal = BigDecimal.ZERO;
        BigDecimal ivaTotal = BigDecimal.ZERO;
        BigDecimal descRepartido = BigDecimal.ZERO;
        int idx = 0;
        int n = basePorTipo.size();
        for (Map.Entry<BigDecimal, BigDecimal> e : basePorTipo.entrySet()) {
            idx++;
            BigDecimal tipo = e.getKey();
            BigDecimal baseRate = e.getValue();
            BigDecimal descRate;
            if (idx == n) {
                descRate = descGlobal.subtract(descRepartido); // el último absorbe el redondeo
            } else if (baseBruta.signum() > 0) {
                descRate = descGlobal.multiply(baseRate)
                        .divide(baseBruta, FinancialMath.SCALE, FinancialMath.ROUND);
            } else {
                descRate = BigDecimal.ZERO;
            }
            descRepartido = descRepartido.add(descRate);
            BigDecimal baseNeta = baseRate.subtract(descRate).setScale(FinancialMath.SCALE, FinancialMath.ROUND);
            BigDecimal cuota = baseNeta.multiply(tipo)
                    .divide(FinancialMath.CIEN, FinancialMath.SCALE, FinancialMath.ROUND);
            porTipo.add(new TipoIva(tipo, baseNeta, cuota));
            baseTotal = baseTotal.add(baseNeta);
            ivaTotal = ivaTotal.add(cuota);
        }
        BigDecimal total = baseTotal.add(ivaTotal);
        return new Resultado(porTipo, baseTotal, ivaTotal, descGlobal, total);
    }

    /** Importe de un descuento sobre una base (según tipo PORCENTAJE|IMPORTE). Nunca supera la base. */
    public static BigDecimal importeDescuento(BigDecimal base, String tipo, BigDecimal valor) {
        if (base == null || base.signum() <= 0 || valor == null || valor.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        if (IMPORTE.equalsIgnoreCase(tipo)) {
            return valor.min(base).setScale(FinancialMath.SCALE, FinancialMath.ROUND);
        }
        // Por defecto y para 'PORCENTAJE': base * valor / 100
        return FinancialMath.porcentaje(base, valor);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
