package alicanteweb.erp.controller.rest;

import alicanteweb.erp.entities.MovimientoBanco;
import alicanteweb.erp.repository.MovimientoBancoRepository;
import alicanteweb.erp.entities.Banco;
import alicanteweb.erp.repository.BancoRepository;
import alicanteweb.erp.service.MovimientoBancoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/web/extractos")
public class ExtractoBancarioRestController {

    private static final Logger log = LoggerFactory.getLogger(ExtractoBancarioRestController.class);

    private final MovimientoBancoService movimientoBancoService;
    private final MovimientoBancoRepository repository;
    private final BancoRepository bancoRepository;

    public ExtractoBancarioRestController(MovimientoBancoService movimientoBancoService,
                                            MovimientoBancoRepository repository,
                                            BancoRepository bancoRepository) {
        this.movimientoBancoService = movimientoBancoService;
        this.repository = repository;
        this.bancoRepository = bancoRepository;
    }

    @PostMapping("/importar")
    public ResponseEntity<Map<String, Object>> importar(@RequestParam Long bancoId,
                                                         @RequestParam("archivo") MultipartFile archivo) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> errores = new ArrayList<>();
        int importados = 0;
        Banco banco = bancoRepository.findById(bancoId).orElse(null);
        if (banco == null) {
            result.put("importados", 0);
            result.put("errores", List.of("Banco no encontrado con id: " + bancoId));
            return ResponseEntity.badRequest().body(result);
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8));
            String linea;
            boolean primera = true;
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            while ((linea = reader.readLine()) != null) {
                if (primera) { primera = false; continue; }
                String[] cols = linea.split(";|,");
                if (cols.length < 3) continue;
                try {
                    MovimientoBanco mb = new MovimientoBanco();
                    mb.setBanco(banco);
                    // CSV esperado: fecha;concepto;importe
                    mb.setFecha(LocalDate.parse(cols[0].trim(), fmt));
                    mb.setConcepto(cols.length > 1 ? cols[1].trim() : "");
                    double importe = Double.parseDouble(cols[2].trim().replace(",", "."));
                    mb.setImporte(BigDecimal.valueOf(Math.abs(importe)));
                    mb.setTipo(importe >= 0 ? "INGRESO" : "GASTO");
                    mb.setConciliado(false);
                    movimientoBancoService.save(mb);
                    importados++;
                } catch (RuntimeException e) {
                    log.warn("Error en línea {} del CSV: {}", importados + 2, e.getMessage());
                    errores.add("Línea " + (importados + 2) + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Error leyendo archivo CSV de extracto bancario: {}", e.getMessage(), e);
            errores.add("Error leyendo archivo: " + e.getMessage());
        }
        result.put("importados", importados);
        result.put("errores", errores);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/conciliar")
    public ResponseEntity<Void> conciliar(@PathVariable Long id) {
        movimientoBancoService.conciliar(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> listar(@RequestParam(required = false) LocalDate desde,
                                                             @RequestParam(required = false) LocalDate hasta) {
        if (desde == null) desde = LocalDate.now().minusMonths(1);
        if (hasta == null) hasta = LocalDate.now();
        List<Map<String, Object>> result = movimientoBancoService.findByFechas(desde, hasta).stream()
                .map(this::toMap)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/no-conciliados")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> noConciliados() {
        List<Map<String, Object>> result = repository.findByConciliado(false).stream()
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
