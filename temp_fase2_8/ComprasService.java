package alicanteweb.erp.service;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.entities.PedidoCompra;
import alicanteweb.erp.entities.Proveedor;
import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.ArticuloRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de gestión de compras
 * FASE 8: Compras
 */
@Service
@Slf4j
public class ComprasService {

    private final ArticuloRepository articuloRepository;
    private final AuditoriaService auditoriaService;
    private final AlertasService alertasService;

    public ComprasService(ArticuloRepository articuloRepository,
                         AuditoriaService auditoriaService,
                         AlertasService alertasService) {
        this.articuloRepository = articuloRepository;
        this.auditoriaService = auditoriaService;
        this.alertasService = alertasService;
    }

    /**
     * Crea un pedido de compra
     */
    @Transactional
    public PedidoCompra crearPedidoCompra(PedidoCompra pedido, Usuario usuario) {
        log.info("Creando pedido de compra para proveedor: {}",
                pedido.getProveedor().getNombre());

        pedido.setUsuarioCreador(usuario);
        pedido.setEstado("BORRADOR");
        pedido.setFecha(LocalDate.now());

        // pedidoCompraRepository.save(pedido);

        auditoriaService.registrarCreacion(usuario, "PedidoCompra",
                pedido.getId() != null ? pedido.getId().toString() : "nuevo",
                "Pedido creado para: " + pedido.getProveedor().getNombre());

        return pedido;
    }

    /**
     * Confirma recepción de mercancía
     */
    @Transactional
    public void confirmarRecepcion(PedidoCompra pedido, Usuario usuario) {
        log.info("Confirmando recepción de pedido: {}", pedido.getNumero());

        pedido.setEstado("RECIBIDO");
        pedido.setRecibido(true);
        pedido.setFechaRecepcion(LocalDate.now());

        // Actualizar stock de artículos
        // TODO: Implementar actualización de stock desde líneas de pedido

        auditoriaService.registrarAccion(usuario, "RECEPCION_COMPRA", "PedidoCompra",
                pedido.getId().toString(), "Mercancía recibida");
    }

    /**
     * Detecta artículos con stock bajo
     */
    public List<Articulo> detectarStockBajo() {
        // Simulación - en producción: consultar artículos con stock < stock mínimo
        log.info("Detectando artículos con stock bajo");

        List<Articulo> articulosBajoStock = List.of();

        if (!articulosBajoStock.isEmpty()) {
            alertasService.alertaModificacionMasiva(null, "Stock", articulosBajoStock.size());
        }

        return articulosBajoStock;
    }

    /**
     * Genera sugerencia de pedido automático
     */
    public Map<String, Object> generarSugerenciaPedido(Proveedor proveedor) {
        log.info("Generando sugerencia de pedido para: {}", proveedor.getNombre());

        Map<String, Object> sugerencia = new HashMap<>();

        // En producción: analizar consumo histórico y stock actual
        sugerencia.put("proveedor", proveedor.getNombre());
        sugerencia.put("articulos_sugeridos", List.of());
        sugerencia.put("total_estimado", BigDecimal.ZERO);
        sugerencia.put("fecha_sugerida", LocalDate.now().plusDays(7));

        return sugerencia;
    }

    /**
     * Análisis de compras
     */
    public Map<String, Object> analizarCompras(LocalDate inicio, LocalDate fin) {
        Map<String, Object> analisis = new HashMap<>();

        analisis.put("periodo_inicio", inicio);
        analisis.put("periodo_fin", fin);
        analisis.put("total_comprado", BigDecimal.ZERO);
        analisis.put("numero_pedidos", 0);
        analisis.put("proveedores_utilizados", 0);
        analisis.put("ticket_medio_compra", BigDecimal.ZERO);
        analisis.put("top_proveedores", Map.of());
        analisis.put("top_articulos_comprados", Map.of());

        return analisis;
    }

    /**
     * Compara precios de proveedores
     */
    public Map<String, Object> compararProveedores(Long articuloId) {
        Map<String, Object> comparativa = new HashMap<>();

        // En producción: consultar últimas compras del artículo a diferentes proveedores
        comparativa.put("articulo_id", articuloId);
        comparativa.put("comparativa_precios", List.of());
        comparativa.put("proveedor_mas_economico", null);
        comparativa.put("proveedor_mas_rapido", null);
        comparativa.put("proveedor_mas_fiable", null);

        return comparativa;
    }
}

