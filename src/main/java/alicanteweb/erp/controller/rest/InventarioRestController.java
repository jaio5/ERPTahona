package alicanteweb.erp.controller.rest;

import alicanteweb.erp.entities.MovimientoStock;
import alicanteweb.erp.repository.ArticuloRepository;
import alicanteweb.erp.repository.MovimientoStockRepository;
import alicanteweb.erp.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/web/reportes")
public class InventarioRestController {

    private final ArticuloRepository articuloRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final StockService stockService;

    public InventarioRestController(ArticuloRepository articuloRepository,
                                     MovimientoStockRepository movimientoStockRepository,
                                     StockService stockService) {
        this.articuloRepository = articuloRepository;
        this.movimientoStockRepository = movimientoStockRepository;
        this.stockService = stockService;
    }

    @GetMapping("/inventario/valoracion")
    public ResponseEntity<Map<String, Object>> valoracion() {
        List<ArticuloRepository.ValoracionInventario> items = articuloRepository.findValoracionInventario();
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("items", items);
        resp.put("totalValor", articuloRepository.sumValorInventario());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/inventario/movimientos")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> movimientos(
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta) {
        if (desde == null) desde = LocalDate.now().minusDays(30);
        if (hasta == null) hasta = LocalDate.now();
        List<Map<String, Object>> result = movimientoStockRepository.findByFechaBetween(desde, hasta).stream()
                .map(m -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", m.getId());
                    map.put("fecha", m.getFecha());
                    map.put("tipo", m.getTipo());
                    map.put("articuloId", m.getArticulo() != null ? m.getArticulo().getId() : null);
                    map.put("articulo", m.getArticulo() != null ? Map.of("nombre", m.getArticulo().getNombre()) : null);
                    map.put("cantidad", m.getCantidad());
                    map.put("concepto", m.getConcepto());
                    map.put("importe", m.getImporte());
                    return map;
                })
                .toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/inventario/ajustar")
    public ResponseEntity<Void> ajustar(@RequestParam Long articuloId,
                                         @RequestParam BigDecimal cantidadNueva,
                                         @RequestParam(required = false) String motivo) {
        stockService.registrarAjuste(articuloId, cantidadNueva, motivo != null ? motivo : "Ajuste manual");
        return ResponseEntity.ok().build();
    }
}
