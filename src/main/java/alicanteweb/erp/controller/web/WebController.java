package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/web")
public class WebController {

    private final AutenticacionService autenticacionService;
    private final FacturaService facturaService;
    private final ClienteService clienteService;
    private final ArticuloService articuloService;
    private final OrdenProduccionService ordenProduccionService;
    private final LoteService loteService;
    private final HojaRutaService hojaRutaService;
    private final RecetaService recetaService;

    public WebController(AutenticacionService autenticacionService,
                         FacturaService facturaService,
                         ClienteService clienteService,
                         ArticuloService articuloService,
                         OrdenProduccionService ordenProduccionService,
                         LoteService loteService,
                         HojaRutaService hojaRutaService,
                         RecetaService recetaService) {
        this.autenticacionService = autenticacionService;
        this.facturaService = facturaService;
        this.clienteService = clienteService;
        this.articuloService = articuloService;
        this.ordenProduccionService = ordenProduccionService;
        this.loteService = loteService;
        this.hojaRutaService = hojaRutaService;
        this.recetaService = recetaService;
    }

    @GetMapping
    public String index(HttpSession session) {
        return requireLogin(session) ? "redirect:/web/dashboard" : "redirect:/web/login";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("usuarioId") != null) return "redirect:/web/dashboard";
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                         HttpSession session, Model model) {
        Usuario usuario = autenticacionService.login(username, password);
        if (usuario == null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
            return "login";
        }
        session.setAttribute("usuarioId", usuario.getId());
        session.setAttribute("usuarioNombre", usuario.getNombre() != null ? usuario.getNombre() : usuario.getUsername());
        return "redirect:/web/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        autenticacionService.logout();
        session.invalidate();
        return "redirect:/web/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (requireLogin(session)) return "redirect:/web/login";
        model.addAttribute("moduloActivo", "dashboard");
        model.addAttribute("titulo", "Panel");

        model.addAttribute("totalClientes", clienteService.findAll().size());
        model.addAttribute("totalArticulos", articuloService.findAll().size());
        model.addAttribute("totalFacturas", facturaService.findAll().size());
        model.addAttribute("totalRecetas", recetaService.findAll().size());

        long ordenesActivas = ordenProduccionService.findByEstado("EN_CURSO").size()
                + ordenProduccionService.findByEstado("PLANIFICADA").size();
        model.addAttribute("ordenesActivas", ordenesActivas);

        long lotesCaducar = loteService.findByFechaCaducidadBetween(
                LocalDate.now(), LocalDate.now().plusDays(7)).size();
        model.addAttribute("lotesCaducar", lotesCaducar);

        long rutasActivas = hojaRutaService.findByEstado("EN_CURSO").size();
        model.addAttribute("rutasActivas", rutasActivas);

        return layout(model, "dashboard");
    }

    public static boolean requireLogin(HttpSession session) {
        return session == null || session.getAttribute("usuarioId") == null;
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
