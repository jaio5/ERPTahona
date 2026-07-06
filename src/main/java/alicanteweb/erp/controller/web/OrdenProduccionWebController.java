package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@PreAuthorize("@permisos.puede('produccion', 'ver')")
@RequestMapping("/web/ordenes-produccion")
public class OrdenProduccionWebController {

    private static final Logger log = LoggerFactory.getLogger(OrdenProduccionWebController.class);

    private final OrdenProduccionService service;
    private final RecetaService recetaService;
    private final ArticuloService articuloService;
    private final HorneadaService horneadaService;

    public OrdenProduccionWebController(OrdenProduccionService service,
                                        RecetaService recetaService,
                                        ArticuloService articuloService,
                                        HorneadaService horneadaService) {
        this.service = service;
        this.recetaService = recetaService;
        this.articuloService = articuloService;
        this.horneadaService = horneadaService;
    }

    @GetMapping
    public String lista(Model m,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(required = false) String estado) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fecha"));
        Page<OrdenProduccion> pageResult = service.findPage(estado, null, pageable);
        m.addAttribute("moduloActivo", "ordenes-produccion");
        m.addAttribute("titulo", "Órdenes de producción");
        m.addAttribute("page", pageResult);
        m.addAttribute("ordenes", pageResult.getContent());
        m.addAttribute("estado", estado);
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.inicio(),
            BreadcrumbBuilder.active("Órdenes de producción")));
        return WebController.layout(m, "ordenes-produccion/lista");
    }

    @GetMapping("/nuevo")
    public String nuevo(Model m) {
        m.addAttribute("moduloActivo", "ordenes-produccion");
        m.addAttribute("titulo", "Nueva orden de producción");
        m.addAttribute("recetas", recetaService.findByActivo(true));
        m.addAttribute("articulos", articuloService.findAll());
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.inicio(),
            BreadcrumbBuilder.link("Órdenes de producción", "/web/ordenes-produccion"),
            BreadcrumbBuilder.active("Nueva orden")));
        return WebController.layout(m, "ordenes-produccion/formulario");
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model m, RedirectAttributes ra) {
        return service.findDetailById(id).map(o -> {
            m.addAttribute("moduloActivo", "ordenes-produccion");
            m.addAttribute("titulo", "Editar orden " + o.getNumero());
            m.addAttribute("orden", o);
            m.addAttribute("recetas", recetaService.findByActivo(true));
            m.addAttribute("articulos", articuloService.findAll());
            m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.inicio(),
                BreadcrumbBuilder.link("Órdenes de producción", "/web/ordenes-produccion"),
                BreadcrumbBuilder.active("Editar " + o.getNumero())));
            return WebController.layout(m, "ordenes-produccion/formulario");
        }).orElseGet(() -> { ra.addFlashAttribute("error", "Registro no encontrado"); return "redirect:/web/ordenes-produccion"; });
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model m, RedirectAttributes ra) {
        return service.findDetailById(id).map(o -> {
            m.addAttribute("moduloActivo", "ordenes-produccion");
            m.addAttribute("titulo", "Orden " + o.getNumero());
            m.addAttribute("orden", o);
            m.addAttribute("horneadas", horneadaService.findByOrdenProduccionId(id));
            m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                    BreadcrumbBuilder.inicio(),
                    BreadcrumbBuilder.link("Órdenes de producción", "/web/ordenes-produccion"),
                    BreadcrumbBuilder.active(o.getNumero())));
            return WebController.layout(m, "ordenes-produccion/ver");
        }).orElseGet(() -> { ra.addFlashAttribute("error", "Registro no encontrado"); return "redirect:/web/ordenes-produccion"; });
    }

    @PostMapping
    public String guardar(@RequestParam(required = false) Long id,
                          @RequestParam(required = false) Long recetaId,
                          @RequestParam(required = false) Long articuloId,
                          @RequestParam(required = false) BigDecimal cantidad,
                          @RequestParam(required = false) String fechaStr,
                          @RequestParam(required = false) String observaciones,
                          RedirectAttributes ra) {
        try {
            OrdenProduccion o = id != null
                    ? service.findById(id).orElseThrow(() -> new IllegalArgumentException("ID de orden de producción no válido: " + id))
                    : new OrdenProduccion();
            if (recetaId != null) {
                recetaService.findById(recetaId).ifPresent(o::setReceta);
            }
            if (articuloId != null) {
                articuloService.findById(articuloId).ifPresent(o::setArticulo);
            }
            o.setCantidadPlanificada(cantidad);
            try {
                o.setFecha(fechaStr != null && !fechaStr.isBlank() ? LocalDate.parse(fechaStr) : LocalDate.now());
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Formato de fecha incorrecto. Use AAAA-MM-DD");
            }
            o.setObservaciones(observaciones);
            if (o.getEstado() == null || o.getEstado().isBlank()) {
                o.setEstado("PLANIFICADA");
            }
            service.save(o);
            ra.addFlashAttribute("exito", "Orden guardada correctamente");
        } catch (RuntimeException e) {
            log.error("Error al guardar orden de producción: {}", e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/ordenes-produccion";
    }

    @PostMapping("/{id}/iniciar")
    @PreAuthorize("@permisos.puede('produccion', 'editar')")
    public String iniciar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.iniciarProduccion(id);
            ra.addFlashAttribute("exito", "Producción iniciada");
        } catch (RuntimeException e) {
            log.error("Error al iniciar orden {}: {}", id, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/ordenes-produccion/" + id;
    }

    @PostMapping("/{id}/finalizar")
    @PreAuthorize("@permisos.puede('produccion', 'editar')")
    public String finalizar(@PathVariable Long id,
                            @RequestParam(required = false) BigDecimal cantidadProducida,
                            @RequestParam(required = false) BigDecimal merma,
                            RedirectAttributes ra) {
        try {
            service.finalizarProduccion(id,
                    cantidadProducida != null ? cantidadProducida : BigDecimal.ZERO,
                    merma != null ? merma : BigDecimal.ZERO);
            ra.addFlashAttribute("exito", "Producción finalizada");
        } catch (RuntimeException e) {
            log.error("Error al finalizar orden {}: {}", id, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/ordenes-produccion/" + id;
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("@permisos.puede('produccion', 'editar')")
    public String cancelar(@PathVariable Long id,
                           @RequestParam(required = false, defaultValue = "Cancelada manualmente") String motivo,
                           RedirectAttributes ra) {
        try {
            service.cancelarProduccion(id, motivo);
            ra.addFlashAttribute("exito", "Orden cancelada");
        } catch (RuntimeException e) {
            log.error("Error al cancelar orden {}: {}", id, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/ordenes-produccion/" + id;
    }
}
