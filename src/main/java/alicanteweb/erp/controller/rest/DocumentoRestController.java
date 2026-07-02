package alicanteweb.erp.controller.rest;

import alicanteweb.erp.controller.dto.AlbaranPendienteDto;
import alicanteweb.erp.controller.dto.PendienteFacturarDto;
import alicanteweb.erp.entities.AlbaranVenta;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.Pedido;
import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.service.AlbaranService;
import alicanteweb.erp.service.ClienteService;
import alicanteweb.erp.service.DocumentoService;
import alicanteweb.erp.service.FacturaService;
import alicanteweb.erp.service.PedidoService;
import alicanteweb.erp.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/web")
public class DocumentoRestController {

    private static final int MAX_ALBARANES_PENDIENTES_POR_CLIENTE = 50;

    private final DocumentoService documentoService;
    private final AlbaranService albaranService;
    private final FacturaService facturaService;
    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;
    private final ClienteService clienteService;

    public DocumentoRestController(DocumentoService documentoService,
                                   AlbaranService albaranService,
                                   FacturaService facturaService,
                                   PedidoService pedidoService,
                                   UsuarioService usuarioService,
                                   ClienteService clienteService) {
        this.documentoService = documentoService;
        this.albaranService = albaranService;
        this.facturaService = facturaService;
        this.pedidoService = pedidoService;
        this.usuarioService = usuarioService;
        this.clienteService = clienteService;
    }

    // =================== DOCUMENTOS CON LÍNEAS ===================

    @PostMapping("/documentos/pedido")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crearPedido(@RequestBody Map<String, Object> datos) {
        Pedido p = documentoService.guardarPedido(null, datos);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", p.getId(), "numero", p.getNumero()));
    }

    @PutMapping("/documentos/pedido/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> actualizarPedido(@PathVariable Long id, @RequestBody Map<String, Object> datos) {
        Pedido p = documentoService.guardarPedido(id, datos);
        return ResponseEntity.ok(Map.of("id", p.getId(), "numero", p.getNumero()));
    }

    @PostMapping("/documentos/albaran")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crearAlbaran(@RequestBody Map<String, Object> datos) {
        AlbaranVenta a = documentoService.guardarAlbaran(null, datos);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", a.getId(), "numero", a.getNumero()));
    }

    @PutMapping("/documentos/albaran/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> actualizarAlbaran(@PathVariable Long id, @RequestBody Map<String, Object> datos) {
        AlbaranVenta a = documentoService.guardarAlbaran(id, datos);
        return ResponseEntity.ok(Map.of("id", a.getId(), "numero", a.getNumero()));
    }

    @PostMapping("/documentos/factura")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crearFactura(@RequestBody Map<String, Object> datos) {
        Factura f = documentoService.guardarFactura(null, datos);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", f.getId(), "numero", f.getNumero()));
    }

    @PutMapping("/documentos/factura/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> actualizarFactura(@PathVariable Long id, @RequestBody Map<String, Object> datos) {
        Factura f = documentoService.guardarFactura(id, datos);
        return ResponseEntity.ok(Map.of("id", f.getId(), "numero", f.getNumero()));
    }

    // =================== ACCIONES FACTURA ===================

    @PostMapping("/facturas/{id}/enviar-revision")
    public ResponseEntity<Void> enviarRevision(@PathVariable Long id) {
        Factura f = facturaService.findById(id).orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));
        if (!"BORRADOR".equalsIgnoreCase(f.getEstado())) {
            throw new IllegalStateException("Solo borradores pueden enviarse a revision");
        }
        f.setEstado("REVISION");
        facturaService.save(f);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/facturas/{id}/emitir")
    public ResponseEntity<Void> emitir(@PathVariable Long id) {
        facturaService.aprobarYEmitir(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/facturas/{id}/anular")
    public ResponseEntity<Void> anular(@PathVariable Long id, @RequestParam String motivo) {
        facturaService.anularFactura(id, motivo);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/facturas/{id}/anular-rectificativa")
    public ResponseEntity<Void> anularRectificativa(@PathVariable Long id,
                                                     @RequestParam String motivo,
                                                     @RequestParam String numeroRectificativa) {
        facturaService.anularPorRectificativa(id, numeroRectificativa, motivo);
        return ResponseEntity.ok().build();
    }

    // =================== ACCIONES ALBARÁN ===================

    @PostMapping("/albaranes/{id}/convertir")
    public ResponseEntity<Map<String, Object>> convertirAlbaran(@PathVariable Long id,
                                                                 @RequestBody Map<String, Object> body,
                                                                 Authentication authentication) {
        Usuario u = currentUser(authentication);
        Factura f = albaranService.convertirAFactura(id, u);
        if (body != null && body.get("medioCobro") != null) {
            f.setMedioCobro(String.valueOf(body.get("medioCobro")));
            facturaService.save(f);
        }
        return ResponseEntity.ok(Map.of("id", f.getId(), "numero", f.getNumero(), "mensaje", "Convertido a " + f.getNumero()));
    }

    @PostMapping("/documentos/factura-desde-albaranes")
    public ResponseEntity<Map<String, Object>> facturarDesdeAlbaranes(@RequestBody Map<String, Object> body,
                                                                      Authentication authentication) {
        List<?> rawIds = (List<?>) body.get("albaranIds");
        if (rawIds == null || rawIds.isEmpty()) throw new IllegalArgumentException("No hay albaranes");
        List<Long> ids = rawIds.stream().map(o -> Long.valueOf(String.valueOf(o))).toList();
        Usuario u = currentUser(authentication);
        Factura f = albaranService.convertirVariosAFactura(ids, u);
        if (body.get("medioCobro") != null) {
            f.setMedioCobro(String.valueOf(body.get("medioCobro")));
        }
        if (body.get("fecha") != null) f.setFecha(java.time.LocalDate.parse(String.valueOf(body.get("fecha"))));
        if (body.get("fechaVencimiento") != null) f.setFechaVencimiento(java.time.LocalDate.parse(String.valueOf(body.get("fechaVencimiento"))));
        if (body.get("observaciones") != null) f.setObservaciones(String.valueOf(body.get("observaciones")));
        f = facturaService.save(f);
        boolean emitir = Boolean.TRUE.equals(body.get("emitir"));
        if (emitir) {
            try {
                facturaService.aprobarYEmitir(f.getId());
            } catch (RuntimeException e) {
                throw new IllegalStateException(
                        "Factura " + f.getNumero() + " (id=" + f.getId() + ") creada pero no se pudo emitir: " + e.getMessage(), e);
            }
        }
        return ResponseEntity.ok(Map.of("id", f.getId(), "numero", f.getNumero()));
    }

    // =================== ALBARANES PENDIENTES CLIENTE ===================

    @GetMapping("/clientes/{id}/albaranes-pendientes")
    public ResponseEntity<List<AlbaranPendienteDto>> albaranesPendientes(@PathVariable Long id) {
        clienteService.findById(id).orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        List<AlbaranPendienteDto> list = albaranService.buscarPendientesFacturarPorCliente(id).stream()
                .limit(MAX_ALBARANES_PENDIENTES_POR_CLIENTE)
                .map(AlbaranPendienteDto::from)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/pendientes-facturar")
    public ResponseEntity<List<PendienteFacturarDto>> pendientesFacturar() {
        Map<Long, PendienteFacturarAcumulado> acumulados = new LinkedHashMap<>();
        for (AlbaranVenta albaran : albaranService.buscarPendientesFacturar()) {
            if (albaran.getCliente() == null || albaran.getCliente().getId() == null) {
                continue;
            }
            Long clienteId = albaran.getCliente().getId();
            PendienteFacturarAcumulado acc = acumulados.computeIfAbsent(clienteId,
                    id -> new PendienteFacturarAcumulado(clienteId, albaran.getCliente().getNombre()));
            acc.add(albaran);
        }
        List<PendienteFacturarDto> resultado = new ArrayList<>();
        for (PendienteFacturarAcumulado acc : acumulados.values()) {
            resultado.add(acc.toDto());
        }
        return ResponseEntity.ok(resultado);
    }

    private static class PendienteFacturarAcumulado {
        private final Long clienteId;
        private final String clienteNombre;
        private long totalAlbaranes;
        private BigDecimal importeTotal = BigDecimal.ZERO;
        private final List<Long> albaranIds = new ArrayList<>();

        private PendienteFacturarAcumulado(Long clienteId, String clienteNombre) {
            this.clienteId = clienteId;
            this.clienteNombre = clienteNombre;
        }

        private void add(AlbaranVenta albaran) {
            totalAlbaranes++;
            albaranIds.add(albaran.getId());
            importeTotal = importeTotal.add(albaran.getTotal() != null ? albaran.getTotal() : BigDecimal.ZERO);
        }

        private PendienteFacturarDto toDto() {
            return new PendienteFacturarDto(clienteId, clienteNombre, totalAlbaranes, importeTotal, albaranIds);
        }
    }

    private Usuario currentUser(Authentication authentication) {
        if (authentication != null
                && authentication.getPrincipal() instanceof alicanteweb.erp.config.SecurityConfig.ErpUserPrincipal principal) {
            return usuarioService.buscarPorId(principal.id()).orElse(null);
        }
        return null;
    }

}
