package alicanteweb.erp.controller.web;

import alicanteweb.erp.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/web")
public class WebController {

    private final FacturaService facturaService;
    private final ClienteService clienteService;
    private final ArticuloService articuloService;
    private final OrdenProduccionService ordenProduccionService;
    private final LoteService loteService;
    private final HojaRutaService hojaRutaService;
    private final RecetaService recetaService;

    public WebController(FacturaService facturaService,
                         ClienteService clienteService,
                         ArticuloService articuloService,
                         OrdenProduccionService ordenProduccionService,
                         LoteService loteService,
                         HojaRutaService hojaRutaService,
                         RecetaService recetaService) {
        this.facturaService = facturaService;
        this.clienteService = clienteService;
        this.articuloService = articuloService;
        this.ordenProduccionService = ordenProduccionService;
        this.loteService = loteService;
        this.hojaRutaService = hojaRutaService;
        this.recetaService = recetaService;
    }

    @GetMapping
    public String index() {
        return "redirect:/web/dashboard";
    }

    @GetMapping("/login")
    public String loginPage(Authentication authentication,
                            @RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (authentication != null && authentication.isAuthenticated()) return "redirect:/web/dashboard";
        if (error != null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
        }
        if (logout != null) {
            model.addAttribute("mensaje", "Sesión cerrada correctamente");
        }
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("moduloActivo", "dashboard");
        model.addAttribute("titulo", "Panel");
        model.addAttribute("breadcrumb", BreadcrumbBuilder.of(BreadcrumbBuilder.active("Inicio")));

        model.addAttribute("totalClientes", clienteService.count());
        model.addAttribute("totalArticulos", articuloService.count());
        model.addAttribute("totalFacturas", facturaService.count());
        model.addAttribute("totalRecetas", recetaService.count());

        long ordenesActivas = ordenProduccionService.countByEstado("EN_CURSO")
                + ordenProduccionService.countByEstado("PLANIFICADA");
        model.addAttribute("ordenesActivas", ordenesActivas);

        long lotesCaducar = loteService.countByFechaCaducidadBetween(
                LocalDate.now(), LocalDate.now().plusDays(7));
        model.addAttribute("lotesCaducar", lotesCaducar);

        long rutasActivas = hojaRutaService.countByEstado("EN_CURSO");
        model.addAttribute("rutasActivas", rutasActivas);

        return layout(model, "dashboard");
    }

    @GetMapping("/calendario")
    public String calendario(Model model) {
        model.addAttribute("moduloActivo", "calendario");
        model.addAttribute("titulo", "Calendario");
        return layout(model, "calendario");
    }

    @GetMapping("/planificador")
    public String planificador(Model model) {
        model.addAttribute("moduloActivo", "planificador");
        model.addAttribute("titulo", "Planificador");
        return layout(model, "planificador");
    }

    @GetMapping("/acceso-denegado")
    public String accesoDenegado(Model model) {
        model.addAttribute("titulo", "Acceso denegado");
        return layout(model, "acceso-denegado");
    }

    public static String layout(Model model, String view) {
        model.addAttribute("content", view);
        return "layout";
    }

    public static String redirectWithError(RedirectAttributes ra, String error) {
        ra.addFlashAttribute("error", error);
        return "redirect:../";
    }
}
