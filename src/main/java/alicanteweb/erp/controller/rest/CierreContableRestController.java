package alicanteweb.erp.controller.rest;

import alicanteweb.erp.service.ContabilidadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/web/contabilidad")
@PreAuthorize("@permisos.puede('contabilidad', 'ver')")
public class CierreContableRestController {

    private final ContabilidadService contabilidadService;

    public CierreContableRestController(ContabilidadService contabilidadService) {
        this.contabilidadService = contabilidadService;
    }

    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> balance(@RequestParam LocalDate fecha) {
        Map<String, Object> result = contabilidadService.calcularBalanceSimple(fecha.withDayOfMonth(1), fecha);
        return ResponseEntity.ok(Map.of(
            "fecha", fecha,
            "totalDebe", result.get("totalDebe"),
            "totalHaber", result.get("totalHaber"),
            "diferencia", result.get("diferencia"),
            "asientos", result.get("asientos")
        ));
    }

    @PostMapping("/cierre")
    @PreAuthorize("@permisos.puede('contabilidad', 'crear')")
    public ResponseEntity<Map<String, Object>> cierreEjercicio(@RequestParam int año) {
        return ResponseEntity.ok(contabilidadService.cerrarEjercicio(año));
    }
}
