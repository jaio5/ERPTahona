package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.TipoImpositivo;
import alicanteweb.erp.service.TipoImpositivoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/** CRUD del catálogo de tipos de IVA (Ajustes). */
@Controller
@PreAuthorize("@permisos.puede('configuracion', 'ver')")
@RequestMapping("/web/tipos-iva")
public class TipoImpositivoWebController {

    private static final Logger log = LoggerFactory.getLogger(TipoImpositivoWebController.class);

    private final TipoImpositivoService service;

    public TipoImpositivoWebController(TipoImpositivoService service) {
        this.service = service;
    }

    @GetMapping
    public String lista(Model m) {
        m.addAttribute("moduloActivo", "tiposIva");
        m.addAttribute("titulo", "Tipos de IVA");
        m.addAttribute("tipos", service.findAll());
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.inicio(),
            BreadcrumbBuilder.link("Administración", "#"),
            BreadcrumbBuilder.active("Tipos de IVA")));
        return WebController.layout(m, "tipos-iva/lista");
    }

    @GetMapping("/nuevo")
    public String nuevo(Model m) {
        return formulario(m, new TipoImpositivo(), "Nuevo tipo de IVA");
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model m, RedirectAttributes ra) {
        return service.findById(id)
            .map(t -> formulario(m, t, "Editar tipo de IVA"))
            .orElseGet(() -> {
                ra.addFlashAttribute("error", "Tipo de IVA no encontrado");
                return "redirect:/web/tipos-iva";
            });
    }

    private String formulario(Model m, TipoImpositivo tipo, String titulo) {
        m.addAttribute("moduloActivo", "tiposIva");
        m.addAttribute("titulo", titulo);
        m.addAttribute("tipo", tipo);
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.inicio(),
            BreadcrumbBuilder.link("Tipos de IVA", "/web/tipos-iva"),
            BreadcrumbBuilder.active(titulo)));
        return WebController.layout(m, "tipos-iva/formulario");
    }

    @PostMapping
    public String guardar(@RequestParam(required = false) Long id,
                          @RequestParam String nombre,
                          @RequestParam String porcentaje,
                          @RequestParam(required = false) String recargoEquivalencia,
                          @RequestParam(required = false, defaultValue = "0") Integer orden,
                          @RequestParam(required = false, defaultValue = "false") boolean activo,
                          @RequestParam(required = false, defaultValue = "false") boolean esDefecto,
                          RedirectAttributes ra) {
        try {
            TipoImpositivo t = id != null
                ? service.findById(id).orElseThrow(() -> new IllegalArgumentException("Tipo de IVA no válido: " + id))
                : new TipoImpositivo();
            t.setNombre(nombre);
            t.setPorcentaje(parseDecimal(porcentaje));
            t.setRecargoEquivalencia(recargoEquivalencia == null || recargoEquivalencia.isBlank()
                ? null : parseDecimal(recargoEquivalencia));
            t.setOrden(orden != null ? orden : 0);
            t.setActivo(activo);
            t.setEsDefecto(esDefecto);
            service.save(t);
            ra.addFlashAttribute("exito", "Tipo de IVA guardado");
        } catch (RuntimeException e) {
            log.error("Error al guardar tipo de IVA: {}", e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
            if (id != null) return "redirect:/web/tipos-iva/" + id + "/editar";
            return "redirect:/web/tipos-iva/nuevo";
        }
        return "redirect:/web/tipos-iva";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.deleteById(id);
            ra.addFlashAttribute("exito", "Tipo de IVA eliminado");
        } catch (RuntimeException e) {
            log.error("Error al eliminar tipo de IVA {}: {}", id, e.getMessage(), e);
            ra.addFlashAttribute("error", "No se pudo eliminar el tipo de IVA");
        }
        return "redirect:/web/tipos-iva";
    }

    private static BigDecimal parseDecimal(String v) {
        if (v == null || v.isBlank()) return BigDecimal.ZERO;
        return new BigDecimal(v.trim().replace(",", "."));
    }
}
