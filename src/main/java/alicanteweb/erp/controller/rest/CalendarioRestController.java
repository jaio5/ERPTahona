package alicanteweb.erp.controller.rest;

import alicanteweb.erp.entities.OrdenProduccion;
import alicanteweb.erp.entities.Pedido;
import alicanteweb.erp.service.OrdenProduccionService;
import alicanteweb.erp.service.PedidoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/web")
public class CalendarioRestController {

    private final PedidoService pedidoService;
    private final OrdenProduccionService ordenProduccionService;

    public CalendarioRestController(PedidoService pedidoService,
                                     OrdenProduccionService ordenProduccionService) {
        this.pedidoService = pedidoService;
        this.ordenProduccionService = ordenProduccionService;
    }

    @GetMapping("/calendario")
    public ResponseEntity<List<Map<String, Object>>> eventos(
            @RequestParam LocalDate inicio,
            @RequestParam LocalDate fin) {
        List<Map<String, Object>> eventos = new ArrayList<>();

        for (Pedido p : pedidoService.findByFechaBetween(inicio, fin)) {
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("id", p.getId());
            e.put("tipo", "PEDIDO");
            e.put("titulo", (p.getCliente() != null ? p.getCliente().getNombre() : "Sin cliente") + " - " + p.getNumero());
            e.put("fecha", p.getFecha());
            e.put("estado", p.getEstado());
            e.put("url", "/web/pedidos-venta");
            eventos.add(e);
        }

        for (OrdenProduccion o : ordenProduccionService.findByFechaBetween(inicio, fin)) {
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("id", o.getId());
            e.put("tipo", "PRODUCCION");
            e.put("titulo", (o.getReceta() != null ? o.getReceta().getNombre() : "Produccion") + " - " + o.getNumero());
            e.put("fecha", o.getFecha());
            e.put("estado", o.getEstado());
            e.put("url", "/web/ordenes-produccion");
            eventos.add(e);
        }

        return ResponseEntity.ok(eventos);
    }
}
