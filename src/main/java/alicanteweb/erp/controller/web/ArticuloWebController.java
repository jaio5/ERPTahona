package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.service.ArticuloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@PreAuthorize("@permisos.puede('articulos', 'ver')")
@RequestMapping("/web/articulos")
public class ArticuloWebController {

    private static final Logger log = LoggerFactory.getLogger(ArticuloWebController.class);

    private final ArticuloService service;

    public ArticuloWebController(ArticuloService service) {
        this.service = service;
    }

    @GetMapping
    public String lista(Model m,
                        @RequestParam(required = false) String q,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "25") int size,
                        @RequestParam(defaultValue = "nombre") String sort,
                        @RequestParam(defaultValue = "asc") String dir) {
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Page<Articulo> pageResult = service.buscarPaginado(q, PageRequest.of(page, size, Sort.by(direction, sort)));
        m.addAttribute("moduloActivo", "articulos");
        m.addAttribute("titulo", "Artículos");
        m.addAttribute("articulos", pageResult.getContent());
        m.addAttribute("page", pageResult);
        m.addAttribute("q", q);
        m.addAttribute("sort", sort);
        m.addAttribute("dir", dir);
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.inicio(),
                BreadcrumbBuilder.active("Artículos")));
        return WebController.layout(m, "articulos/lista");
    }

    @GetMapping("/nuevo")
    public String nuevo(Model m) {
        m.addAttribute("moduloActivo", "articulos");
        m.addAttribute("titulo", "Nuevo artículo");
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.inicio(),
                BreadcrumbBuilder.link("Artículos", "/web/articulos"),
                BreadcrumbBuilder.active("Nuevo artículo")));
        return WebController.layout(m, "articulos/formulario");
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model m, RedirectAttributes ra) {
        return service.findById(id).map(a -> {
            m.addAttribute("moduloActivo", "articulos");
            m.addAttribute("titulo", a.getNombre());
            m.addAttribute("articulo", a);
            m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                    BreadcrumbBuilder.inicio(),
                    BreadcrumbBuilder.link("Artículos", "/web/articulos"),
                    BreadcrumbBuilder.active(a.getNombre())));
            return WebController.layout(m, "articulos/ver");
        }).orElseGet(() -> { ra.addFlashAttribute("error", "Registro no encontrado"); return "redirect:/web/articulos"; });
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model m, RedirectAttributes ra) {
        return service.findById(id).map(a -> {
            m.addAttribute("moduloActivo", "articulos");
            m.addAttribute("titulo", "Editar artículo");
            m.addAttribute("articulo", a);
            m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                    BreadcrumbBuilder.inicio(),
                    BreadcrumbBuilder.link("Artículos", "/web/articulos"),
                    BreadcrumbBuilder.active("Editar")));
            return WebController.layout(m, "articulos/formulario");
        }).orElseGet(() -> { ra.addFlashAttribute("error", "Registro no encontrado"); return "redirect:/web/articulos"; });
    }

    @PostMapping
    public String guardar(@RequestParam(required = false) Long id,
                          @RequestParam String codigo,
                          @RequestParam String nombre,
                          @RequestParam(required = false) String categoria,
                          @RequestParam(required = false) String unidad,
                          @RequestParam(required = false) String pvp,
                          @RequestParam(required = false) String iva,
                          @RequestParam(required = false) String coste,
                          @RequestParam(required = false) String stock,
                          @RequestParam(required = false) String alergenos,
                          RedirectAttributes ra) {
        try {
            Articulo a = id != null ? service.findById(id).orElseThrow(() -> new IllegalArgumentException("ID de artículo no válido: " + id)) : new Articulo();
            a.setCodigo(codigo);
            a.setNombre(nombre);
            a.setCategoria(categoria);
            a.setUnidad(unidad);
            a.setPvp(parseDecimal(pvp, "PVP"));
            a.setIva(parseDecimal(iva, "IVA"));
            a.setCoste(parseDecimal(coste, "coste"));
            a.setStock(parseDecimal(stock, "stock"));
            a.setAlergenos(alergenos);
            service.save(a);
            ra.addFlashAttribute("exito", "Artículo guardado correctamente");
        } catch (RuntimeException e) {
            log.error("Error al guardar artículo: {}", e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/articulos";
    }

    private BigDecimal parseDecimal(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        String normalized = value.trim().replace(" ", "");
        if (normalized.contains(",") && normalized.contains(".")) {
            if (normalized.lastIndexOf(',') > normalized.lastIndexOf('.')) {
                normalized = normalized.replace(".", "").replace(',', '.');
            } else {
                normalized = normalized.replace(",", "");
            }
        } else {
            normalized = normalized.replace(',', '.');
        }
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El valor de " + fieldName + " no es un número válido");
        }
    }
}
