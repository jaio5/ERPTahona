package alicanteweb.erp.controller.rest;

import alicanteweb.erp.entities.MovimientoBanco;
import alicanteweb.erp.service.MovimientoBancoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/web/extractos")
public class ExtractoBancarioRestController {

    private static final int MESES_DEFECTO_EXTRACTO = 1;

    private final MovimientoBancoService movimientoBancoService;

    public ExtractoBancarioRestController(MovimientoBancoService movimientoBancoService) {
        this.movimientoBancoService = movimientoBancoService;
    }

    @PostMapping("/importar")
    public ResponseEntity<Map<String, Object>> importar(@RequestParam Long bancoId,
                                                         @RequestParam("archivo") MultipartFile archivo) {
        try {
            MovimientoBancoService.ResultadoImportacionCSV resultado = movimientoBancoService.importarDesdeCSV(bancoId, archivo);
            return ResponseEntity.ok(Map.of("importados", resultado.importados(), "errores", resultado.errores()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("importados", 0, "errores", List.of(e.getMessage())));
        }
    }

    @PostMapping("/importar-n43")
    public ResponseEntity<Map<String, Object>> importarNorma43(@RequestParam Long bancoId,
                                                               @RequestParam("archivo") MultipartFile archivo) {
        try {
            MovimientoBancoService.ResultadoImportacionCSV resultado = movimientoBancoService.importarNorma43(bancoId, archivo);
            return ResponseEntity.ok(Map.of("importados", resultado.importados(), "errores", resultado.errores()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("importados", 0, "errores", List.of(e.getMessage())));
        }
    }

    @PostMapping("/{id}/conciliar")
    public ResponseEntity<Void> conciliar(@PathVariable Long id) {
        movimientoBancoService.conciliar(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listar(@RequestParam(required = false) LocalDate desde,
                                                             @RequestParam(required = false) LocalDate hasta) {
        if (desde == null) desde = LocalDate.now().minusMonths(MESES_DEFECTO_EXTRACTO);
        if (hasta == null) hasta = LocalDate.now();
        List<Map<String, Object>> result = movimientoBancoService.findByFechas(desde, hasta).stream()
                .map(this::toMap)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/no-conciliados")
    public ResponseEntity<List<Map<String, Object>>> noConciliados() {
        List<Map<String, Object>> result = movimientoBancoService.findNoConciliados().stream()
                .map(this::toMap)
                .toList();
        return ResponseEntity.ok(result);
    }

    private Map<String, Object> toMap(MovimientoBanco m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.getId());
        map.put("fecha", m.getFecha());
        map.put("tipo", m.getTipo());
        map.put("concepto", m.getConcepto());
        map.put("importe", m.getImporte());
        map.put("saldoResultante", m.getSaldoResultante());
        map.put("conciliado", Boolean.TRUE.equals(m.getConciliado()));
        return map;
    }
}
