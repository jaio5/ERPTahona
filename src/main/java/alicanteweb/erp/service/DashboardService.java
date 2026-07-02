package alicanteweb.erp.service;

import alicanteweb.erp.repository.FacturaRepository;
import alicanteweb.erp.repository.OrdenProduccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final int MESES_HISTORICO = 5;
    private static final String[] NOMBRES_MESES = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

    private final FacturaRepository facturaRepository;
    private final OrdenProduccionRepository ordenRepository;

    public DashboardService(FacturaRepository facturaRepository, OrdenProduccionRepository ordenRepository) {
        this.facturaRepository = facturaRepository;
        this.ordenRepository = ordenRepository;
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
