package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.RutaReparto;
import alicanteweb.erp.service.RutaRepartoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@PreAuthorize("@permisos.puede('reparto', 'ver')")
@RequestMapping("/web/rutas")
public class RutaRepartoWebController {

    private static final Logger log = LoggerFactory.getLogger(RutaRepartoWebController.class);

    private final RutaRepartoService rutaRepartoService;

    public RutaRepartoWebController(RutaRepartoService rutaRepartoService) {
        this.rutaRepartoService = rutaRepartoService;
    }

    @GetMapping
    public String lista(Model model, @RequestParam(required = false) String q) {
        List<RutaReparto> rutas = (q != null && !q.isBlank())
                ? rutaRepartoService.searchByNombre(q) : rutaRepartoService.findAll();
        model.addAttribute("moduloActivo", "rutas");
        model.addAttribute("titulo", "Rutas de reparto");
        model.addAttribute("rutas", rutas);
        model.addAttribute("q", q);
        return WebController.layout(model, "rutas/lista");
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("moduloActivo", "rutas");
        model.addAttribute("titulo", "Nueva ruta");
        model.addAttribute("ruta", new RutaReparto());
        return WebController.layout(model, "rutas/formulario");
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return rutaRepartoService.findById(id).map(r -> {
            model.addAttribute("moduloActivo", "rutas");
            model.addAttribute("titulo", "Editar ruta " + r.getNombre());
            model.addAttribute("ruta", r);
            return WebController.layout(model, "rutas/formulario");
        }).orElseGet(() -> { ra.addFlashAttribute("error", "Registro no encontrado"); return "redirect:/web/rutas"; });
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return rutaRepartoService.findDetailById(id).map(r -> {
            model.addAttribute("moduloActivo", "rutas");
            model.addAttribute("titulo", "Ruta " + r.getNombre());
            model.addAttribute("ruta", r);
            model.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                    BreadcrumbBuilder.inicio(),
                    BreadcrumbBuilder.link("Rutas", "/web/rutas"),
                    BreadcrumbBuilder.active(r.getNombre())));
            return WebController.layout(model, "rutas/ver");
        }).orElseGet(() -> { ra.addFlashAttribute("error", "Registro no encontrado"); return "redirect:/web/rutas"; });
    }

    @PostMapping
    public String guardar(@RequestParam(required = false) Long id,
                          @RequestParam String codigo,
                          @RequestParam String nombre,
                          @RequestParam(required = false) String conductor,
                          @RequestParam(required = false) String descripcion,
                          RedirectAttributes ra) {
        try {
            RutaReparto ruta = id != null ? rutaRepartoService.findById(id).orElseThrow(() -> new IllegalArgumentException("ID de ruta no válido: " + id)) : new RutaReparto();
            ruta.setCodigo(codigo);
            ruta.setNombre(nombre);
            ruta.setConductor(conductor);
            ruta.setDescripcion(descripcion);
            rutaRepartoService.save(ruta);
            ra.addFlashAttribute("exito", "Ruta guardada correctamente");
        } catch (RuntimeException e) {
            log.error("Error al guardar ruta: {}", e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/rutas";
    }
}
