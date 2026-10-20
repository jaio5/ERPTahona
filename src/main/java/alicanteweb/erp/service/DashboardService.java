package alicanteweb.erp.service;

import alicanteweb.erp.repository.AlbaranVentaRepository;
import alicanteweb.erp.repository.FacturaRepository;
import alicanteweb.erp.repository.OrdenProduccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final int MESES_HISTORICO = 5;
    private static final String[] NOMBRES_MESES = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

    private final FacturaRepository facturaRepository;
    private final OrdenProduccionRepository ordenRepository;
    private final AlbaranVentaRepository albaranRepository;

    public DashboardService(FacturaRepository facturaRepository, OrdenProduccionRepository ordenRepository,
                            AlbaranVentaRepository albaranRepository) {
        this.facturaRepository = facturaRepository;
        this.ordenRepository = ordenRepository;
        this.albaranRepository = albaranRepository;
    }

    /**
     * Resumen operativo de un día para el panel "Hoy". Las facturas en BORRADOR o
     * ANULADA no cuentan; el mostrador es la serie del TPV y lo "facturado" el resto.
     */
    @Transactional(readOnly = true)
    public ResumenDia resumenDia(LocalDate dia) {
        Object[] mostrador = filaUnica(facturaRepository.resumenDiaDeSerie(dia, TpvService.SERIE_TPV));
        Object[] ordinarias = filaUnica(facturaRepository.resumenDiaExcluyendoSerie(dia, TpvService.SERIE_TPV));
        Object[] vencidas = filaUnica(facturaRepository.resumenVencidas(dia));
        long albaranesSinEntregar = albaranRepository.countSinEntregarByFecha(dia);
        return new ResumenDia(dia,
                aLong(mostrador[0]), aImporte(mostrador[1]),
                aLong(ordinarias[0]), aImporte(ordinarias[1]),
                albaranesSinEntregar,
                aLong(vencidas[0]), aImporte(vencidas[1]));
    }

    /** Cifras del día que pinta el panel. Importes con 2 decimales, nunca null. */
    public record ResumenDia(LocalDate dia,
                             long ticketsMostrador, BigDecimal importeMostrador,
                             long facturasEmitidas, BigDecimal importeFacturado,
                             long albaranesSinEntregar,
                             long facturasVencidas, BigDecimal importeVencido) {
    }

    private static Object[] filaUnica(List<Object[]> filas) {
        if (filas == null || filas.isEmpty() || filas.get(0) == null) return new Object[]{0L, BigDecimal.ZERO};
        return filas.get(0);
    }

    private static long aLong(Object valor) {
        return valor instanceof Number n ? n.longValue() : 0L;
    }

    private static BigDecimal aImporte(Object valor) {
        BigDecimal importe;
        if (valor instanceof BigDecimal bd) importe = bd;
        else if (valor instanceof Number n) importe = new BigDecimal(n.toString());
        else importe = BigDecimal.ZERO;
        return importe.setScale(2, RoundingMode.HALF_UP);
    }

    public List<Map<String, Object>> ventasMensuales() {
        LocalDate desde = LocalDate.now().minusMonths(MESES_HISTORICO).withDayOfMonth(1);
        List<Object[]> rows = facturaRepository.findVentasMensuales(desde);
        Map<String, Number> totalesPorMes = acumularVentasPorMes(rows);

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = MESES_HISTORICO; i >= 0; i--) {
            LocalDate mes = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            String key = mes.getYear() + "-" + mes.getMonthValue();
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("mes", NOMBRES_MESES[mes.getMonthValue() - 1]);
            entry.put("total", totalesPorMes.getOrDefault(key, 0));
            result.add(entry);
        }
        return result;
    }

    public List<Map<String, Object>> produccionEstado() {
        List<Object[]> rows = ordenRepository.countByEstado();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("estado", row[0] != null ? row[0].toString() : "DESCONOCIDO");
            entry.put("cantidad", ((Number) row[1]).longValue());
            result.add(entry);
        }
        return result;
    }

    private Map<String, Number> acumularVentasPorMes(List<Object[]> rows) {
        Map<String, Number> totalesPorMes = new LinkedHashMap<>();
        for (Object[] row : rows) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            totalesPorMes.put(year + "-" + month, (Number) row[2]);
        }
        return totalesPorMes;
    }
}
