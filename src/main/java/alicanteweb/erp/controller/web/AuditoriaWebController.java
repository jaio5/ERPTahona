package alicanteweb.erp.controller.web;

import alicanteweb.erp.service.AuditoriaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@PreAuthorize("@permisos.puede('auditoria', 'ver')")
@RequestMapping("/web/auditoria")
public class AuditoriaWebController {

    private final AuditoriaService s;

    public AuditoriaWebController(AuditoriaService s) {
        this.s = s;
    }

    @GetMapping
    public String lista(Model m) {
        m.addAttribute("moduloActivo", "auditoria");
        m.addAttribute("titulo", "Auditoría");
        m.addAttribute("registros", s.obtenerRecientes());
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.inicio(),
            BreadcrumbBuilder.link("Administración", "#"),
            BreadcrumbBuilder.active("Auditoría")));
        return WebController.layout(m, "auditoria/lista");
    }
}
