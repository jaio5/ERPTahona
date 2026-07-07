package alicanteweb.erp.controller.rest;

import alicanteweb.erp.controller.dto.ArticuloDto;
import alicanteweb.erp.controller.dto.ClienteDto;
import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.service.AlbaranService;
import alicanteweb.erp.service.ArticuloService;
import alicanteweb.erp.service.ClienteService;
import alicanteweb.erp.service.FacturaService;
import alicanteweb.erp.service.PedidoCompraService;
import alicanteweb.erp.service.PedidoService;
import alicanteweb.erp.service.ProveedorService;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/web")
public class WebApiController {

    private final ClienteService clienteService;
    private final ArticuloService articuloService;
    private final ProveedorService proveedorService;
    private final FacturaService facturaService;
    private final PedidoService pedidoService;
    private final PedidoCompraService pedidoCompraService;
    private final AlbaranService albaranService;

    public WebApiController(ClienteService clienteService,
                            ArticuloService articuloService,
                            ProveedorService proveedorService,
                            FacturaService facturaService,
                            PedidoService pedidoService,
                            PedidoCompraService pedidoCompraService,
                            AlbaranService albaranService) {
        this.clienteService = clienteService;
        this.articuloService = articuloService;
        this.proveedorService = proveedorService;
        this.facturaService = facturaService;
        this.pedidoService = pedidoService;
        this.pedidoCompraService = pedidoCompraService;
        this.albaranService = albaranService;
    }

    @GetMapping("/resumen")
    @PreAuthorize("@permisos.puede('dashboard', 'ver')")
    public WebResumen resumen() {
        return new WebResumen(
                clienteService.count(),
                articuloService.count(),
                proveedorService.count(),
                facturaService.count(),
                pedidoService.count(),
                pedidoCompraService.count()
        );
    }

    @GetMapping("/clientes")
    @PreAuthorize("@permisos.puede('clientes', 'ver')")
    public List<ClienteDto> clientes(@RequestParam(required = false) String q,
                                     @RequestParam(defaultValue = "100") int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 500));
        return clienteService.buscarParaApi(q, PageRequest.of(0, safeLimit)).stream()
                .map(ClienteDto::from)
                .toList();
    }

    @GetMapping("/clientes/{id}")
    @PreAuthorize("@permisos.puede('clientes', 'ver')")
    public ResponseEntity<ClienteDto> cliente(@PathVariable Long id) {
        return clienteService.findById(id)
                .map(ClienteDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/clientes")
    @PreAuthorize("@permisos.puede('clientes', 'crear')")
    public ClienteDto crearCliente(@RequestBody ClienteDto dto) {
        return ClienteDto.from(clienteService.save(dto.toEntity(new Cliente())));
    }

    @PutMapping("/clientes/{id}")
    @PreAuthorize("@permisos.puede('clientes', 'editar')")
    public ResponseEntity<ClienteDto> actualizarCliente(@PathVariable Long id, @RequestBody ClienteDto dto) {
        return clienteService.findById(id)
                .map(cliente -> {
                    dto.applyTo(cliente);
                    return ResponseEntity.ok(ClienteDto.from(clienteService.save(cliente)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/clientes/{id}/baja")
    @PreAuthorize("@permisos.puede('clientes', 'editar')")
    public ResponseEntity<Void> bajaCliente(@PathVariable Long id) {
        clienteService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/clientes/{id}/activar")
    @PreAuthorize("@permisos.puede('clientes', 'editar')")
    public ResponseEntity<Void> activarCliente(@PathVariable Long id) {
        clienteService.activar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/articulos")
    @PreAuthorize("@permisos.puede('articulos', 'ver')")
    public List<ArticuloDto> articulos(@RequestParam(required = false) String q,
                                       @RequestParam(required = false) Boolean activo,
                                       @RequestParam(defaultValue = "500") int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 1000));
        return articuloService.buscarParaApi(q, activo, PageRequest.of(0, safeLimit)).stream()
                .map(ArticuloDto::from)
                .toList();
    }

    @GetMapping("/articulos/{id}")
    @PreAuthorize("@permisos.puede('articulos', 'ver')")
    public ResponseEntity<ArticuloDto> articulo(@PathVariable Long id) {
        return articuloService.findById(id)
                .map(ArticuloDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/articulos")
    @PreAuthorize("@permisos.puede('articulos', 'crear')")
    public ArticuloDto crearArticulo(@RequestBody ArticuloDto dto) {
        return ArticuloDto.from(articuloService.save(dto.toEntity(new Articulo())));
    }

    @PutMapping("/articulos/{id}")
    @PreAuthorize("@permisos.puede('articulos', 'editar')")
    public ResponseEntity<ArticuloDto> actualizarArticulo(@PathVariable Long id, @RequestBody ArticuloDto dto) {
        return articuloService.findById(id)
                .map(articulo -> {
                    dto.applyTo(articulo);
                    return ResponseEntity.ok(ArticuloDto.from(articuloService.save(articulo)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/articulos/{id}/baja")
    @PreAuthorize("@permisos.puede('articulos', 'editar')")
    public ResponseEntity<Void> bajaArticulo(@PathVariable Long id) {
        articuloService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/articulos/{id}/activar")
    @PreAuthorize("@permisos.puede('articulos', 'editar')")
    public ResponseEntity<Void> activarArticulo(@PathVariable Long id) {
        articuloService.activar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/clientes/{clienteId}/albaranes-recientes")
    @PreAuthorize("@permisos.puede('ventas', 'ver')")
    public List<AlbaranResumen> albaranesRecientes(@PathVariable Long clienteId) {
        return albaranService.obtenerRecientesPorCliente(clienteId).stream()
                .map(a -> new AlbaranResumen(
                        a.getId(),
                        a.getNumero(),
                        a.getFecha() != null ? a.getFecha().toString() : null,
                        a.getTotal(),
                        a.getAlbaranVentaLineas().stream()
                                .map(l -> new LineaResumen(
                                        l.getArticulo() != null ? l.getArticulo().getId() : null,
                                        l.getCantidad(),
                                        l.getPrecio(),
                                        l.getIva()))
                                .toList()))
                .toList();
    }

    public record AlbaranResumen(Long id,
                                 String numero,
                                 String fecha,
                                 java.math.BigDecimal total,
                                 List<LineaResumen> lineas) {}

    public record LineaResumen(Long articuloId,
                               java.math.BigDecimal cantidad,
                               java.math.BigDecimal precio,
                               java.math.BigDecimal iva) {}

    public record WebResumen(long clientes,
                             long articulos,
                             long proveedores,
                             long facturas,
                             long pedidosVenta,
                             long pedidosCompra) {
    }
}
