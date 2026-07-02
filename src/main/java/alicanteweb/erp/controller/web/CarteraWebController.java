package alicanteweb.erp.controller.web;

import alicanteweb.erp.service.CarteraService;
import alicanteweb.erp.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Cartera de cobros y pagos: pendientes con antigüedad (aging) y registro
 * de cobros/pagos parciales.
 */
@Controller
@PreAuthorize("@permisos.puede('tesoreria', 'ver')")
@RequestMapping("/web/cartera")
public class CarteraWebController extends BaseWebController {

    private static final Logger log = LoggerFactory.getLogger(CarteraWebController.class);

    private final CarteraService carteraService;

    public CarteraWebController(CarteraService carteraService, UsuarioService usuarioService) {
        super(usuarioService);
        this.carteraService = carteraService;
    }

    @GetMapping
    public String cartera(Model m) {
        List<CarteraService.CarteraItem> cobros = carteraService.cobrosPendientes();
        List<CarteraService.CarteraItem> pagos = carteraService.pagosPendientes();
        m.addAttribute("moduloActivo", "cartera");
        m.addAttribute("titulo", "Cartera de cobros y pagos");
        m.addAttribute("cobros", cobros);
        m.addAttribute("pagos", pagos);
        m.addAttribute("agingCobros", carteraService.aging(cobros));
        m.addAttribute("agingPagos", carteraService.aging(pagos));
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.link("Inicio", "/web/dashboard"),
                BreadcrumbBuilder.link("Finanzas", "#"),
                BreadcrumbBuilder.active("Cartera")));
        return WebController.layout(m, "cartera/index");
    }

    @PostMapping("/cobros")
    @PreAuthorize("@permisos.puede('tesoreria', 'crear')")
    public String registrarCobro(@RequestParam Long facturaId,
                                 @RequestParam BigDecimal importe,
                                 @RequestParam(required = false) LocalDate fecha,
                                 @RequestParam(required = false) String formaPago,
                                 @RequestParam(required = false) String referencia,
                                 @RequestParam(required = false) String observaciones,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        try {
            carteraService.registrarCobro(facturaId, fecha, importe, formaPago, referencia,
                    observaciones, usuarioActual(session));
            ra.addFlashAttribute("exito", "Cobro registrado");
        } catch (RuntimeException e) {
            log.error("Error al registrar cobro de factura {}: {}", facturaId, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/cartera";
    }

    @PostMapping("/pagos")
    @PreAuthorize("@permisos.puede('tesoreria', 'crear')")
    public String registrarPago(@RequestParam Long facturaCompraId,
                                @RequestParam BigDecimal importe,
                                @RequestParam(required = false) LocalDate fecha,
                                @RequestParam(required = false) String formaPago,
                                @RequestParam(required = false) String referencia,
                                @RequestParam(required = false) String observaciones,
                                HttpSession session,
                                RedirectAttributes ra) {
        try {
            carteraService.registrarPago(facturaCompraId, fecha, importe, formaPago, referencia,
                    observaciones, usuarioActual(session));
            ra.addFlashAttribute("exito", "Pago registrado");
        } catch (RuntimeException e) {
            log.error("Error al registrar pago de factura de compra {}: {}", facturaCompraId, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/cartera";
    }
}
