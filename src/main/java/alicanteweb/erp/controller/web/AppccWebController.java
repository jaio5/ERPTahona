package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.AppccControl;
import alicanteweb.erp.service.AppccControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@PreAuthorize("@permisos.puede('produccion', 'ver')")
@RequestMapping("/web/appcc")
public class AppccWebController {

    private static final Logger log = LoggerFactory.getLogger(AppccWebController.class);

    private final AppccControlService appccControlService;

    public AppccWebController(AppccControlService appccControlService) {
        this.appccControlService = appccControlService;
    }

    @GetMapping
    public String lista(Model model, @RequestParam(required = false) String q) {
        List<AppccControl> controles = (q != null && !q.isBlank())
                ? appccControlService.buscar(q) : appccControlService.findAll();
        model.addAttribute("moduloActivo", "appcc");
        model.addAttribute("titulo", "Controles APPCC");
        model.addAttribute("controles", controles);
        model.addAttribute("q", q);
        return WebController.layout(model, "appcc/lista");
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("moduloActivo", "appcc");
        model.addAttribute("titulo", "Nuevo control APPCC");
        model.addAttribute("control", new AppccControl());
        return WebController.layout(model, "appcc/formulario");
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return appccControlService.findById(id).map(c -> {
            model.addAttribute("moduloActivo", "appcc");
            model.addAttribute("titulo", "Control APPCC");
            model.addAttribute("control", c);
            return WebController.layout(model, "appcc/ver");
        }).orElseGet(() -> { ra.addFlashAttribute("error", "Registro no encontrado"); return "redirect:/web/appcc"; });
    }

    @PostMapping
    public String guardar(@RequestParam(required = false) Long id,
                          @RequestParam String puntoCritico,
                          @RequestParam(required = false) BigDecimal temperatura,
                          @RequestParam(required = false, name = "limiteCritico") BigDecimal limite,
                          @RequestParam(required = false) String resultado,
                          @RequestParam(required = false, name = "accionCorrectiva") String accion,
                          @RequestParam(required = false) String responsable,
                          RedirectAttributes ra) {
        try {
            AppccControl control = id != null ? appccControlService.findById(id).orElseThrow(() -> new IllegalArgumentException("ID de control APPCC no válido: " + id)) : new AppccControl();
            control.setFecha(LocalDate.now());
            control.setHora(LocalDateTime.now());
            control.setPuntoCritico(puntoCritico);
            control.setTemperatura(temperatura);
            control.setLimiteCritico(limite);
            control.setResultado(resultado);
            control.setAccionCorrectiva(accion);
            control.setResponsable(responsable);
            appccControlService.save(control);
            ra.addFlashAttribute("exito", "Control APPCC guardado correctamente");
        } catch (RuntimeException e) {
            log.error("Error al guardar control APPCC: {}", e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/appcc";
    }
}
