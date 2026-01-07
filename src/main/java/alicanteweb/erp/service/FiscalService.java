package alicanteweb.erp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Servicio para cálculos fiscales españoles
 */
@Service
public class FiscalService {
    private static final Logger log = LoggerFactory.getLogger(FiscalService.class);

    // Tipos de IVA españoles
    private static final BigDecimal IVA_GENERAL = new BigDecimal("0.21");
    private static final BigDecimal IVA_REDUCIDO = new BigDecimal("0.10");
    private static final BigDecimal IVA_SUPER_REDUCIDO = new BigDecimal("0.04");
    private static final BigDecimal IVA_EXENTO = BigDecimal.ZERO;

    /**
     * Calcula el IVA basado en el tipo
     */
    public BigDecimal calcularIVA(BigDecimal base, String tipoIVA) {
        log.debug("Calculando IVA: {} - Tipo: {}", base, tipoIVA);

        BigDecimal porcentaje = obtenerPorcentajeIVA(tipoIVA);
        BigDecimal iva = base.multiply(porcentaje).setScale(2, RoundingMode.HALF_UP);

        log.debug("IVA calculado: {}", iva);
        return iva;
    }

    /**
     * Obtiene el porcentaje de IVA
     */
    public BigDecimal obtenerPorcentajeIVA(String tipoIVA) {
        if (tipoIVA == null || tipoIVA.isEmpty()) {
            return IVA_GENERAL;
        }

        return switch (tipoIVA.toUpperCase()) {
            case "GENERAL", "21%" -> IVA_GENERAL;
            case "REDUCIDO", "10%" -> IVA_REDUCIDO;
            case "SUPER_REDUCIDO", "4%" -> IVA_SUPER_REDUCIDO;
            case "EXENTO", "0%" -> IVA_EXENTO;
            default -> IVA_GENERAL;
        };
    }

    /**
     * Calcula retención IRPF
     */
    public BigDecimal calcularRetencionIRPF(BigDecimal base, BigDecimal porcentaje) {
        log.debug("Calculando IRPF: {} - Porcentaje: {}", base, porcentaje);
        return base.multiply(porcentaje).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el total final de una factura
     */
    public BigDecimal calcularTotalFactura(BigDecimal base, String tipoIVA, BigDecimal retencion) {
        BigDecimal iva = calcularIVA(base, tipoIVA);
        BigDecimal total = base.add(iva).subtract(retencion).setScale(2, RoundingMode.HALF_UP);

        log.debug("Total factura: Base={}, IVA={}, Retención={}, Total={}",
            base, iva, retencion, total);

        return total;
    }

    /**
     * Valida un NIF/CIF
     */
    public boolean validarNIF(String nif) {
        if (nif == null || nif.isEmpty()) {
            return false;
        }

        // Validación simple: formato básico
        String pattern = "[0-9]{8}[A-Z]|[A-Z][0-9]{7}[0-9A-Z]";
        return nif.matches(pattern);
    }

    /**
     * Valida un CIF
     */
    public boolean validarCIF(String cif) {
        if (cif == null || cif.isEmpty()) {
            return false;
        }

        // Validación simple: formato básico
        String pattern = "[ABCDEFGHJNPQRSUVW][0-9]{7}[0-9A-Z]";
        return cif.matches(pattern);
    }

    /**
     * Genera número de factura único
     */
    public String generarNumeroFactura() {
        long timestamp = System.currentTimeMillis();
        return String.format("FAC-%d", timestamp);
    }

    /**
     * Genera número de albaran único
     */
    public String generarNumeroAlbaran() {
        long timestamp = System.currentTimeMillis();
        return String.format("ALB-%d", timestamp);
    }
}

