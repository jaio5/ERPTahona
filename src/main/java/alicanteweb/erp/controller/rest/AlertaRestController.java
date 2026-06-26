package alicanteweb.erp.controller.rest;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/web")
public class AlertaRestController {

    private final ArticuloService articuloService;
    private final PedidoService pedidoService;
    private final OrdenProduccionService ordenProduccionService;
    private final LoteService loteService;
    private final FacturaService facturaService;

    public AlertaRestController(ArticuloService articuloService,
                                 PedidoService pedidoService,
                                 OrdenProduccionService ordenProduccionService,
                                 LoteService loteService,
                                 FacturaService facturaService) {
        this.articuloService = articuloService;
        this.pedidoService = pedidoService;
        this.ordenProduccionService = ordenProduccionService;
        this.loteService = loteService;
        this.facturaService = facturaService;
    }

    @GetMapping("/alertas")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> alertas() {
        List<Map<String, Object>> alertas = new ArrayList<>();
        LocalDate hoy = LocalDate.now();

        // Stock bajo
        for (Articulo art : articuloService.findConStockBajo()) {
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("id", "STOCK_BAJO_" + art.getId());
            a.put("tipo", "STOCK_BAJO");
            a.put("severidad", "warning");
            a.put("titulo", "Stock bajo: " + art.getNombre());
            a.put("descripcion", "Stock actual: " + art.getStock() + ", mínimo: " + art.getStockMinimo());
            alertas.add(a);
        }

        // Lotes por caducar (próximos 3 días)
        List<Lote> lotes = loteService.findByFechaCaducidadBetween(hoy, hoy.plusDays(3));
        for (Lote l : lotes) {
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("id", "LOTE_CADUCA_" + l.getId());
            a.put("tipo", "LOTE_CADUCA");
            a.put("severidad", l.getFechaCaducidad().equals(hoy) ? "danger" : "warning");
            a.put("titulo", "Lote caduca: " + (l.getArticulo() != null ? l.getArticulo().getNombre() : "N/A"));
            a.put("descripcion", "Lote " + l.getCodigo() + " caduca el " + l.getFechaCaducidad());
            alertas.add(a);
        }

        // Pedidos pendientes no servidos
        long pendientes = pedidoService.countByEstado("PENDIENTE");
        if (pendientes > 0) {
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("id", "PEDIDOS_PENDIENTES");
            a.put("tipo", "PEDIDOS_PENDIENTES");
            a.put("severidad", "info");
            a.put("titulo", pendientes + " pedido(s) pendiente(s)");
            a.put("descripcion", "Hay pedidos sin servir");
            alertas.add(a);
        }

        // Órdenes de producción planificadas
        long planificadas = ordenProduccionService.countByEstado("PLANIFICADA");
        if (planificadas > 0) {
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("id", "ORDENES_PLANIFICADAS");
            a.put("tipo", "ORDENES_PLANIFICADAS");
            a.put("severidad", "info");
            a.put("titulo", planificadas + " orden(es) de producción planificada(s)");
            a.put("descripcion", "Órdenes listas para iniciar");
            alertas.add(a);
        }

        // Facturas vencidas
        for (Factura f : facturaService.findVencidas(hoy)) {
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("id", "FACTURA_VENCIDA_" + f.getId());
            a.put("tipo", "FACTURA_VENCIDA");
            a.put("severidad", "danger");
            a.put("titulo", "Factura vencida: " + f.getNumero());
            a.put("descripcion", "Vencimiento: " + f.getFechaVencimiento() + ", Total: " + f.getTotal());
            alertas.add(a);
        }

        return ResponseEntity.ok(alertas);
    }
}
