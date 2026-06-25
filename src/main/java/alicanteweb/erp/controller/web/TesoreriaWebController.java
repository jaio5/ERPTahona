package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.MovimientoBanco;
import alicanteweb.erp.service.MovimientoBancoService;
import alicanteweb.erp.service.MovimientoCajaService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@PreAuthorize("@permisos.puede('tesoreria', 'ver')")
@RequestMapping("/web/tesoreria")
public class TesoreriaWebController {

    private final MovimientoCajaService cajaS;
    private final MovimientoBancoService bancoS;

    public TesoreriaWebController(MovimientoCajaService cajaS, MovimientoBancoService bancoS) {
        this.cajaS = cajaS;
        this.bancoS = bancoS;
    }

    @GetMapping
    public String caja(Model m) {
        m.addAttribute("moduloActivo", "tesoreria");
        m.addAttribute("titulo", "Tesorería");
        m.addAttribute("caja", cajaS.findAll());
        m.addAttribute("banco", bancoS.findAll());
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.link("Inicio", "/web/dashboard"),
            BreadcrumbBuilder.link("Finanzas", "#"),
            BreadcrumbBuilder.active("Tesorería")));
        return WebController.layout(m, "tesoreria/lista");
    }

    @PostMapping("/caja")
    @PreAuthorize("@permisos.puede('tesoreria', 'crear')")
    public String registrarMovimientoCaja(@RequestParam String tipo,
                                          @RequestParam String concepto,
                                          @RequestParam BigDecimal importe,
                                          @RequestParam(required = false) String categoria,
                                          RedirectAttributes ra) {
        try {
            if ("INGRESO".equals(tipo)) {
                cajaS.registrarIngreso(concepto, importe, categoria);
            } else {
                cajaS.registrarGasto(concepto, importe, categoria);
            }
            ra.addFlashAttribute("exito", "Movimiento de caja registrado");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/tesoreria";
    }

    @GetMapping("/extractos")
    public String extractos(Model m) {
        m.addAttribute("moduloActivo", "tesoreria");
        m.addAttribute("titulo", "Extractos");
        return WebController.layout(m, "tesoreria/extractos");
    }

    @GetMapping("/conciliacion")
    public String conciliacion(Model m) {
        m.addAttribute("moduloActivo", "extractos");
        m.addAttribute("titulo", "Conciliación bancaria");
        m.addAttribute("pendientes", bancoS.findNoConciliados());
        return WebController.layout(m, "tesoreria/conciliacion");
    }

    @GetMapping("/conciliacion/{id}/candidatos")
    @ResponseBody
    public ResponseEntity<List<MovimientoBancoService.CandidatoConciliacion>> candidatos(@PathVariable Long id) {
        return bancoS.findById(id)
                .map(mov -> ResponseEntity.ok(bancoS.buscarCandidatos(mov)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/conciliacion/{id}/aprobar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> aprobar(
            @PathVariable Long id,
            @RequestParam(required = false) Long facturaId,
            @RequestParam(required = false) Long facturaCompraId) {
        try {
            MovimientoBanco resultado;
            if (facturaId != null) {
                resultado = bancoS.conciliarConFactura(id, facturaId);
            } else if (facturaCompraId != null) {
                resultado = bancoS.conciliarConFacturaCompra(id, facturaCompraId);
            } else {
                resultado = bancoS.conciliar(id);
            }
            return ResponseEntity.ok(Map.of("ok", true, "id", resultado.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", e.getMessage()));
        }
    }
}
