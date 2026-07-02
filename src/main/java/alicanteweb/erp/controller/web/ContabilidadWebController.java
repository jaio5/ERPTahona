package alicanteweb.erp.controller.web;

import alicanteweb.erp.service.ContabilidadService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@PreAuthorize("@permisos.puede('contabilidad', 'ver')")
@RequestMapping("/web/contabilidad")
public class ContabilidadWebController {

    private final ContabilidadService s;

    public ContabilidadWebController(ContabilidadService s) {
        this.s = s;
    }

    @GetMapping
    public String lista(Model m) {
        m.addAttribute("moduloActivo", "contabilidad");
        m.addAttribute("titulo", "Contabilidad");
        m.addAttribute("asientos", s.obtenerLibroDiario(LocalDate.now().withDayOfMonth(1), LocalDate.now()));
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.link("Inicio", "/web/dashboard"),
            BreadcrumbBuilder.link("Finanzas", "#"),
            BreadcrumbBuilder.active("Contabilidad")));
        return WebController.layout(m, "contabilidad/lista");
    }

    @GetMapping("/balance")
    public String balance(Model m) {
        m.addAttribute("moduloActivo", "contabilidad");
        m.addAttribute("titulo", "Balance y cierre");
        return WebController.layout(m, "contabilidad/balance");
    }
}
