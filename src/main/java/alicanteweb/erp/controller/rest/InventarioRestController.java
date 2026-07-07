package alicanteweb.erp.controller.rest;

import alicanteweb.erp.service.ArticuloService;
import alicanteweb.erp.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/web/reportes")
@PreAuthorize("@permisos.puede('reportes', 'ver')")
public class InventarioRestController {

    private static final int DIAS_DEFECTO_MOVIMIENTOS = 30;

    private final ArticuloService articuloService;
    private final StockService stockService;

    public InventarioRestController(ArticuloService articuloService, StockService stockService) {
        this.articuloService = articuloService;
        this.stockService = stockService;
    }

    @GetMapping("/inventario/valoracion")
    public ResponseEntity<Map<String, Object>> valoracion() {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("items", articuloService.findValoracionInventario());
        resp.put("totalValor", articuloService.sumValorInventario());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/inventario/movimientos")
    public ResponseEntity<List<Map<String, Object>>> movimientos(
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta) {
        if (desde == null) desde = LocalDate.now().minusDays(DIAS_DEFECTO_MOVIMIENTOS);
        if (hasta == null) hasta = LocalDate.now();
        List<Map<String, Object>> result = stockService.findMovimientosByFecha(desde, hasta);
        return ResponseEntity.ok(result);
    }

    // Escritura de stock: no basta el reportes/ver de la clase; exige el mismo
    // permiso que el resto de operaciones de almacén (lotes, mermas).
    @PostMapping("/inventario/ajustar")
    @PreAuthorize("@permisos.puede('almacen', 'editar')")
    public ResponseEntity<Void> ajustar(@RequestParam Long articuloId,
                                         @RequestParam BigDecimal cantidadNueva,
                                         @RequestParam(required = false) String motivo) {
        stockService.registrarAjuste(articuloId, cantidadNueva, motivo != null ? motivo : "Ajuste manual");
        return ResponseEntity.ok().build();
    }
}
