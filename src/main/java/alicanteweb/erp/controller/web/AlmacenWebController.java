package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.Almacen;
import alicanteweb.erp.service.AlmacenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@PreAuthorize("@permisos.puede('almacen', 'ver')")
@RequestMapping("/web/almacenes")
public class AlmacenWebController {

    private static final Logger log = LoggerFactory.getLogger(AlmacenWebController.class);

    private final AlmacenService service;

    public AlmacenWebController(AlmacenService service) {
        this.service = service;
    }

    @GetMapping
    public String lista(Model m, @RequestParam(required = false) String q) {
        List<Almacen> items = (q != null && !q.isBlank())
                ? service.buscar(q) : service.findAll();
        m.addAttribute("moduloActivo", "almacenes");
        m.addAttribute("titulo", "Almacenes");
        m.addAttribute("almacenes", items);
        m.addAttribute("q", q);
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.inicio(),
            BreadcrumbBuilder.active("Almacenes")));
        return WebController.layout(m, "almacenes/lista");
    }

    @GetMapping("/nuevo")
    public String nuevo(Model m) {
        m.addAttribute("moduloActivo", "almacenes");
        m.addAttribute("titulo", "Nuevo almacén");
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.inicio(),
            BreadcrumbBuilder.link("Almacenes", "/web/almacenes"),
            BreadcrumbBuilder.active("Nuevo almacén")));
        return WebController.layout(m, "almacenes/formulario");
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model m, RedirectAttributes ra) {
        return service.findById(id).map(a -> {
            m.addAttribute("moduloActivo", "almacenes");
            m.addAttribute("titulo", "Editar almacén");
            m.addAttribute("almacen", a);
            m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.inicio(),
                BreadcrumbBuilder.link("Almacenes", "/web/almacenes"),
                BreadcrumbBuilder.active("Editar " + a.getNombre())));
            return WebController.layout(m, "almacenes/formulario");
        }).orElseGet(() -> { ra.addFlashAttribute("error", "Registro no encontrado"); return "redirect:/web/almacenes"; });
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model m, RedirectAttributes ra) {
        return service.findById(id).map(a -> {
            m.addAttribute("moduloActivo", "almacenes");
            m.addAttribute("titulo", "Almacén " + a.getNombre());
            m.addAttribute("almacen", a);
            m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                    BreadcrumbBuilder.inicio(),
                    BreadcrumbBuilder.link("Almacenes", "/web/almacenes"),
                    BreadcrumbBuilder.active(a.getNombre())));
            return WebController.layout(m, "almacenes/ver");
        }).orElseGet(() -> { ra.addFlashAttribute("error", "Registro no encontrado"); return "redirect:/web/almacenes"; });
    }

    @PostMapping
    public String guardar(@RequestParam(required = false) Long id,
                          @RequestParam String codigo,
                          @RequestParam String nombre,
                          @RequestParam(required = false) String descripcion,
                          @RequestParam(required = false) BigDecimal capacidad,
                          @RequestParam(required = false) String localidad,
                          @RequestParam(required = false) String responsable,
                          RedirectAttributes ra) {
        try {
            Almacen a = id != null ? service.findById(id).orElseThrow(() -> new IllegalArgumentException("ID de almacén no válido: " + id)) : new Almacen();
            a.setCodigo(codigo);
            a.setNombre(nombre);
            a.setDescripcion(descripcion);
            a.setCapacidad(capacidad);
            a.setLocalidad(localidad);
            a.setResponsable(responsable);
            service.save(a);
            ra.addFlashAttribute("exito", "Almacén guardado correctamente");
        } catch (RuntimeException e) {
            log.error("Error al guardar almacén: {}", e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/almacenes";
    }
}
