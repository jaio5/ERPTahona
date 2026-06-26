package alicanteweb.erp.controller.rest;

import alicanteweb.erp.repository.FacturaRepository;
import alicanteweb.erp.repository.OrdenProduccionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardRestController {

    private static final String[] MESES = {"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"};

    private final FacturaRepository facturaRepository;
    private final OrdenProduccionRepository ordenRepository;

    public DashboardRestController(FacturaRepository facturaRepository,
                                   OrdenProduccionRepository ordenRepository) {
        this.facturaRepository = facturaRepository;
        this.ordenRepository = ordenRepository;
    }

    @GetMapping("/ventas-mensuales")
    public List<Map<String, Object>> ventasMensuales() {
        LocalDate desde = LocalDate.now().minusMonths(5).withDayOfMonth(1);
        List<Object[]> rows = facturaRepository.findVentasMensuales(desde);

        Map<String, Number> totalesPorMes = new LinkedHashMap<>();
        for (Object[] row : rows) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            Number total = (Number) row[2];
            totalesPorMes.put(year + "-" + month, total);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate mes = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            String key = mes.getYear() + "-" + mes.getMonthValue();
            String label = MESES[mes.getMonthValue() - 1];
            Number total = totalesPorMes.getOrDefault(key, 0);
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("mes", label);
            entry.put("total", total);
            result.add(entry);
        }
        return result;
    }

    @GetMapping("/produccion-estado")
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
}
