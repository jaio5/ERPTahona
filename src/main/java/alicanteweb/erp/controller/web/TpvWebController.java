package alicanteweb.erp.controller.web;

import alicanteweb.erp.service.TpvService;
import alicanteweb.erp.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * TPV de venta mostrador: parrilla táctil de artículos y emisión inmediata
 * de factura simplificada con su ticket.
 */
@Controller
@PreAuthorize("@permisos.puede('ventas', 'ver')")
@RequestMapping("/web/tpv")
public class TpvWebController extends BaseWebController {

    private static final Logger log = LoggerFactory.getLogger(TpvWebController.class);

    private final TpvService tpvService;

    public TpvWebController(TpvService tpvService, UsuarioService usuarioService) {
        super(usuarioService);
        this.tpvService = tpvService;
    }

    @GetMapping
    public String tpv(Model m) {
        m.addAttribute("moduloActivo", "tpv");
        m.addAttribute("titulo", "TPV · Venta mostrador");
        m.addAttribute("articulos", tpvService.articulosVendibles());
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.inicio(),
                BreadcrumbBuilder.link("Ventas", "#"),
                BreadcrumbBuilder.active("TPV")));
        return WebController.layout(m, "tpv/index");
    }

    @PostMapping("/vender")
    @PreAuthorize("@permisos.puede('ventas', 'crear')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> vender(@RequestBody VentaTpvRequest request,
                                                      HttpSession session) {
        try {
            TpvService.ResultadoVenta venta = tpvService.vender(
                    request.lineas(), request.formaPago(), usuarioActual(session));
            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "facturaId", venta.facturaId(),
                    "numero", venta.numero(),
                    "total", venta.total(),
                    "pdfUrl", "/web/facturas/" + venta.facturaId() + "/pdf"));
        } catch (RuntimeException e) {
            log.error("Error en venta TPV: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", e.getMessage()));
        }
    }

    public record VentaTpvRequest(List<TpvService.LineaTpv> lineas, String formaPago) {
    }
}
