package alicanteweb.erp.controller.rest;

import alicanteweb.erp.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardRestController {

    private final DashboardService dashboardService;

    public DashboardRestController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/ventas-mensuales")
    public List<Map<String, Object>> ventasMensuales() {
        return dashboardService.ventasMensuales();
    }

    @GetMapping("/produccion-estado")
    public List<Map<String, Object>> produccionEstado() {
        return dashboardService.produccionEstado();
    }
}
