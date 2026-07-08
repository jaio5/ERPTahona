package alicanteweb.erp.controller.web;

import alicanteweb.erp.util.Flash;
import alicanteweb.erp.entities.Receta;
import alicanteweb.erp.service.RecetaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@PreAuthorize("@permisos.puede('produccion', 'ver')")
@RequestMapping("/web/recetas")
public class RecetaWebController {

    private static final Logger log = LoggerFactory.getLogger(RecetaWebController.class);

    private final RecetaService service;

    public RecetaWebController(RecetaService service) {
        this.service = service;
    }

    @GetMapping
    public String lista(Model m, @RequestParam(required = false) String q) {
        List<Receta> items = (q != null && !q.isBlank())
                ? service.searchByNombre(q) : service.findAll();
        m.addAttribute("moduloActivo", "recetas");
        m.addAttribute("titulo", "Recetas");
        m.addAttribute("recetas", items);
        m.addAttribute("q", q);
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.inicio(),
            BreadcrumbBuilder.active("Recetas")));
        return WebController.layout(m, "recetas/lista");
    }

    @GetMapping("/nuevo")
    public String nuevo(Model m) {
        m.addAttribute("moduloActivo", "recetas");
        m.addAttribute("titulo", "Nueva receta");
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.inicio(),
            BreadcrumbBuilder.link("Recetas", "/web/recetas"),
            BreadcrumbBuilder.active("Nueva receta")));
        return WebController.layout(m, "recetas/formulario");
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model m, RedirectAttributes ra) {
        return service.findDetailById(id).map(r -> {
            m.addAttribute("moduloActivo", "recetas");
            m.addAttribute("titulo", "Editar receta");
            m.addAttribute("receta", r);
            m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.inicio(),
                BreadcrumbBuilder.link("Recetas", "/web/recetas"),
                BreadcrumbBuilder.active("Editar " + r.getNombre())));
            return WebController.layout(m, "recetas/formulario");
        }).orElseGet(() -> { Flash.error(ra, "Registro no encontrado"); return "redirect:/web/recetas"; });
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model m, RedirectAttributes ra) {
        return service.findById(id).map(r -> {
            m.addAttribute("moduloActivo", "recetas");
            m.addAttribute("titulo", "Receta " + r.getNombre());
            m.addAttribute("receta", r);
            m.addAttribute("ingredientes", service.getIngredientes(id));
            m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                    BreadcrumbBuilder.inicio(),
                    BreadcrumbBuilder.link("Recetas", "/web/recetas"),
                    BreadcrumbBuilder.active(r.getNombre())));
            return WebController.layout(m, "recetas/ver");
        }).orElseGet(() -> { Flash.error(ra, "Registro no encontrado"); return "redirect:/web/recetas"; });
    }

    @PostMapping
    public String guardar(@RequestParam(required = false) Long id,
                          @RequestParam String codigo,
                          @RequestParam String nombre,
                          @RequestParam(required = false) String descripcion,
                          @RequestParam(required = false) Integer tiempoPrep,
                          @RequestParam(required = false) Integer tiempoHorneado,
                          @RequestParam(required = false) Integer temperatura,
                          @RequestParam(required = false) String alergenos,
                          RedirectAttributes ra) {
        try {
            Receta r = id != null ? service.findById(id).orElseThrow(() -> new IllegalArgumentException("ID de receta no válido: " + id)) : new Receta();
            r.setCodigo(codigo);
            r.setNombre(nombre);
            r.setDescripcion(descripcion);
            r.setTiempoPreparacion(tiempoPrep);
            r.setTiempoHorneado(tiempoHorneado);
            r.setTemperaturaHorneado(temperatura);
            r.setAlergenos(alergenos);
            service.save(r);
            Flash.exito(ra, "Receta guardada correctamente");
        } catch (RuntimeException e) {
            log.error("Error al guardar receta: {}", e.getMessage(), e);
            Flash.error(ra, e.getMessage());
        }
        return "redirect:/web/recetas";
    }
}
