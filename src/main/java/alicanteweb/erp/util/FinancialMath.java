package alicanteweb.erp.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class FinancialMath {

    public static final BigDecimal CIEN = new BigDecimal("100");
    public static final int SCALE = 2;
    public static final RoundingMode ROUND = RoundingMode.HALF_UP;

    private FinancialMath() {}

    /** base * porcentaje / 100, escala 2. Devuelve ZERO si el porcentaje es nulo o cero. */
    public static BigDecimal porcentaje(BigDecimal base, BigDecimal porcentaje) {
        if (base == null || porcentaje == null || porcentaje.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return base.multiply(porcentaje).divide(CIEN, SCALE, ROUND);
    }

    /** Subtotal con descuento aplicado (sin IVA). */
    public static BigDecimal subtotalConDescuento(BigDecimal cantidad, BigDecimal precio, BigDecimal descuento) {
        BigDecimal subtotal = (cantidad != null ? cantidad : BigDecimal.ZERO)
                .multiply(precio != null ? precio : BigDecimal.ZERO);
        if (descuento != null && descuento.compareTo(BigDecimal.ZERO) > 0) {
            subtotal = subtotal.subtract(porcentaje(subtotal, descuento));
        }
        return subtotal.setScale(SCALE, ROUND);
    }
}