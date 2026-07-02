package alicanteweb.erp.service;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.Lote;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class AlertaService {

    private static final int DIAS_AVISO_CADUCIDAD_LOTE = 3;

    private final ArticuloService articuloService;
    private final PedidoService pedidoService;
    private final OrdenProduccionService ordenProduccionService;
    private final LoteService loteService;
    private final FacturaService facturaService;

    public AlertaService(ArticuloService articuloService,
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

    @Transactional(readOnly = true)
    public List<Map<String, Object>> obtenerAlertas() {
        List<Map<String, Object>> alertas = new ArrayList<>();
        LocalDate hoy = LocalDate.now();

        agregarAlertasStockBajo(alertas);
        agregarAlertasLotesCaducando(alertas, hoy);
        agregarAlertaPedidosPendientes(alertas);
        agregarAlertaOrdenesPlanificadas(alertas);
        agregarAlertasFacturasVencidas(alertas, hoy);

        return alertas;
    }

    private void agregarAlertasStockBajo(List<Map<String, Object>> alertas) {
        for (Articulo art : articuloService.findConStockBajo()) {
            alertas.add(crearAlerta(
                    "STOCK_BAJO_" + art.getId(),
                    "STOCK_BAJO",
                    "warning",
                    "Stock bajo: " + art.getNombre(),
                    "Stock actual: " + art.getStock() + ", mínimo: " + art.getStockMinimo()
            ));
        }
    }

    private void agregarAlertasLotesCaducando(List<Map<String, Object>> alertas, LocalDate hoy) {
        List<Lote> lotes = loteService.findByFechaCaducidadBetween(hoy, hoy.plusDays(DIAS_AVISO_CADUCIDAD_LOTE));
        for (Lote l : lotes) {
            String severidad = l.getFechaCaducidad().equals(hoy) ? "danger" : "warning";
            alertas.add(crearAlerta(
                    "LOTE_CADUCA_" + l.getId(),
                    "LOTE_CADUCA",
                    severidad,
                    "Lote caduca: " + (l.getArticulo() != null ? l.getArticulo().getNombre() : "N/A"),
                    "Lote " + l.getCodigo() + " caduca el " + l.getFechaCaducidad()
            ));
        }
    }

    private void agregarAlertaPedidosPendientes(List<Map<String, Object>> alertas) {
        long pendientes = pedidoService.countByEstado("PENDIENTE");
        if (pendientes > 0) {
            alertas.add(crearAlerta(
                    "PEDIDOS_PENDIENTES",
                    "PEDIDOS_PENDIENTES",
                    "info",
                    pendientes + " pedido(s) pendiente(s)",
                    "Hay pedidos sin servir"
            ));
        }
    }

    private void agregarAlertaOrdenesPlanificadas(List<Map<String, Object>> alertas) {
        long planificadas = ordenProduccionService.countByEstado("PLANIFICADA");
        if (planificadas > 0) {
            alertas.add(crearAlerta(
                    "ORDENES_PLANIFICADAS",
                    "ORDENES_PLANIFICADAS",
                    "info",
                    planificadas + " orden(es) de producción planificada(s)",
                    "Órdenes listas para iniciar"
            ));
        }
    }

    private void agregarAlertasFacturasVencidas(List<Map<String, Object>> alertas, LocalDate hoy) {
        for (Factura f : facturaService.findVencidas(hoy)) {
            alertas.add(crearAlerta(
                    "FACTURA_VENCIDA_" + f.getId(),
                    "FACTURA_VENCIDA",
                    "danger",
                    "Factura vencida: " + f.getNumero(),
                    "Vencimiento: " + f.getFechaVencimiento() + ", Total: " + f.getTotal()
            ));
        }
    }

    private Map<String, Object> crearAlerta(String id, String tipo, String severidad, String titulo, String descripcion) {
        Map<String, Object> alerta = new LinkedHashMap<>();
        alerta.put("id", id);
        alerta.put("tipo", tipo);
        alerta.put("severidad", severidad);
        alerta.put("titulo", titulo);
        alerta.put("descripcion", descripcion);
        return alerta;
    }
}
