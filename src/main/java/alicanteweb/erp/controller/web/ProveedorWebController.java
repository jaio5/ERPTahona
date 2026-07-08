package alicanteweb.erp.controller.web;

import alicanteweb.erp.util.Flash;
import alicanteweb.erp.entities.Proveedor;
import alicanteweb.erp.service.ProveedorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@PreAuthorize("@permisos.puede('proveedores', 'ver')")
@RequestMapping("/web/proveedores")
public class ProveedorWebController {

    private static final Logger log = LoggerFactory.getLogger(ProveedorWebController.class);

    private final ProveedorService service;

    public ProveedorWebController(ProveedorService service) {
        this.service = service;
    }

    @GetMapping
    public String lista(Model m, @RequestParam(required = false) String q,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "15") int size) {
        Page<Proveedor> pageResult = service.buscarPaginado(q, PageRequest.of(page, size));
        m.addAttribute("moduloActivo", "proveedores");
        m.addAttribute("titulo", "Proveedores");
        m.addAttribute("proveedores", pageResult.getContent());
        m.addAttribute("page", pageResult);
        m.addAttribute("q", q);
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.inicio(),
            BreadcrumbBuilder.active("Proveedores")));
        return WebController.layout(m, "proveedores/lista");
    }

    @GetMapping("/nuevo")
    public String nuevo(Model m) {
        m.addAttribute("moduloActivo", "proveedores");
        m.addAttribute("titulo", "Nuevo proveedor");
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.inicio(),
            BreadcrumbBuilder.link("Proveedores", "/web/proveedores"),
            BreadcrumbBuilder.active("Nuevo proveedor")));
        return WebController.layout(m, "proveedores/formulario");
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model m, RedirectAttributes ra) {
        return service.findById(id).map(p -> {
            m.addAttribute("moduloActivo", "proveedores");
            m.addAttribute("titulo", "Proveedor " + p.getNombre());
            m.addAttribute("proveedor", p);
            m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.inicio(),
                BreadcrumbBuilder.link("Proveedores", "/web/proveedores"),
                BreadcrumbBuilder.active(p.getNombre())));
            return WebController.layout(m, "proveedores/ver");
        }).orElseGet(() -> { Flash.error(ra, "Registro no encontrado"); return "redirect:/web/proveedores"; });
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model m, RedirectAttributes ra) {
        return service.findById(id).map(p -> {
            m.addAttribute("moduloActivo", "proveedores");
            m.addAttribute("titulo", "Editar proveedor");
            m.addAttribute("proveedor", p);
            m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.inicio(),
                BreadcrumbBuilder.link("Proveedores", "/web/proveedores"),
                BreadcrumbBuilder.active("Editar")));
            return WebController.layout(m, "proveedores/formulario");
        }).orElseGet(() -> { Flash.error(ra, "Registro no encontrado"); return "redirect:/web/proveedores"; });
    }

    @PostMapping
    public String guardar(@RequestParam(required = false) Long id,
                          @RequestParam String codigo,
                          @RequestParam String nombre,
                          @RequestParam(required = false) String cif,
                          @RequestParam(required = false) String telefono,
                          @RequestParam(required = false) String email,
                          RedirectAttributes ra) {
        try {
            Proveedor p = id != null ? service.findById(id).orElseThrow(() -> new IllegalArgumentException("ID de proveedor no válido: " + id)) : new Proveedor();
            p.setCodigo(codigo);
            p.setNombre(nombre);
            p.setCif(cif);
            p.setTelefono(telefono);
            p.setEmail(email);
            service.save(p);
            Flash.exito(ra, "Proveedor guardado correctamente");
        } catch (RuntimeException e) {
            log.error("Error al guardar proveedor: {}", e.getMessage(), e);
            Flash.error(ra, e.getMessage());
        }
        return "redirect:/web/proveedores";
    }
}
