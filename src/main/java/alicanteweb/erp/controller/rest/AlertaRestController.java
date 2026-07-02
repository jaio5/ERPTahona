package alicanteweb.erp.controller.rest;

import alicanteweb.erp.service.AlertaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/web")
public class AlertaRestController {

    private final AlertaService alertaService;

    public AlertaRestController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping("/alertas")
    public ResponseEntity<List<Map<String, Object>>> alertas() {
        return ResponseEntity.ok(alertaService.obtenerAlertas());
    }
}
