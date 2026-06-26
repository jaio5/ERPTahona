package alicanteweb.erp.controller.rest;

import alicanteweb.erp.entities.AsientoContable;
import alicanteweb.erp.repository.AsientoContableRepository;
import alicanteweb.erp.service.AsientoContableService;
import alicanteweb.erp.service.ContabilidadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/web/contabilidad")
public class CierreContableRestController {

    private final ContabilidadService contabilidadService;
    private final AsientoContableService asientoContableService;
    private final AsientoContableRepository repository;

    public CierreContableRestController(ContabilidadService contabilidadService,
                                         AsientoContableService asientoContableService,
                                         AsientoContableRepository repository) {
        this.contabilidadService = contabilidadService;
        this.asientoContableService = asientoContableService;
        this.repository = repository;
    }

    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> balance(@RequestParam LocalDate fecha) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<AsientoContable> asientos = repository.findByFechaBetween(fecha.withDayOfMonth(1), fecha);
        BigDecimal totalDebe = BigDecimal.ZERO;
        BigDecimal totalHaber = BigDecimal.ZERO;
        for (AsientoContable a : asientos) {
            totalDebe = totalDebe.add(a.getDebe() != null ? a.getDebe() : BigDecimal.ZERO);
            totalHaber = totalHaber.add(a.getHaber() != null ? a.getHaber() : BigDecimal.ZERO);
        }
        result.put("fecha", fecha);
        result.put("totalDebe", totalDebe);
        result.put("totalHaber", totalHaber);
        result.put("diferencia", totalDebe.subtract(totalHaber));
        result.put("asientos", asientos.size());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/cierre")
    public ResponseEntity<Map<String, Object>> cierreEjercicio(@RequestParam int año) {
        Map<String, Object> result = new LinkedHashMap<>();
        LocalDate inicio = LocalDate.of(año, 1, 1);
        LocalDate fin = LocalDate.of(año, 12, 31);

        List<AsientoContable> asientos = repository.findByFechaBetween(inicio, fin);
        BigDecimal totalDebe = asientos.stream()
                .map(a -> a.getDebe() != null ? a.getDebe() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalHaber = asientos.stream()
                .map(a -> a.getHaber() != null ? a.getHaber() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Crear asiento de cierre
        AsientoContable cierre = new AsientoContable();
        cierre.setFecha(fin);
        cierre.setConcepto("Cierre ejercicio " + año);
        cierre.setTipo("CIERRE");
        cierre.setDebe(totalHaber);
        cierre.setHaber(totalDebe);
        cierre.setAsientoCierre(true);
        asientoContableService.save(cierre);

        result.put("mensaje", "Cierre del ejercicio " + año + " completado");
        result.put("totalDebe", totalDebe);
        result.put("totalHaber", totalHaber);
        result.put("asientoCierreId", cierre.getId());
        return ResponseEntity.ok(result);
    }
}
