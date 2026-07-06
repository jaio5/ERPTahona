package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.Lote;
import alicanteweb.erp.service.ArticuloService;
import alicanteweb.erp.service.LoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
@PreAuthorize("@permisos.puede('almacen', 'ver')")
@RequestMapping("/web/lotes")
public class LoteWebController {

    private static final Logger log = LoggerFactory.getLogger(LoteWebController.class);

    private final LoteService service;
    private final ArticuloService articuloService;

    public LoteWebController(LoteService service, ArticuloService articuloService) {
        this.service = service;
        this.articuloService = articuloService;
    }

    @GetMapping
    public String lista(Model m, @RequestParam(required = false) String q,
                        @RequestParam(required = false) String estado,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "15") int size) {
        Page<Lote> pageResult = service.findPage(q, estado, PageRequest.of(page, size));
        m.addAttribute("moduloActivo", "lotes");
        m.addAttribute("titulo", "Lotes / Trazabilidad");
        m.addAttribute("lotes", pageResult.getContent());
        m.addAttribute("page", pageResult);
        m.addAttribute("q", q);
        m.addAttribute("estado", estado);
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.inicio(),
            BreadcrumbBuilder.active("Lotes")));
        return WebController.layout(m, "lotes/lista");
    }

    @GetMapping("/nuevo")
    public String nuevo(Model m) {
        m.addAttribute("moduloActivo", "lotes");
        m.addAttribute("titulo", "Nuevo lote");
        m.addAttribute("articulos", articuloService.findAll());
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.inicio(),
            BreadcrumbBuilder.link("Lotes", "/web/lotes"),
            BreadcrumbBuilder.active("Nuevo lote")));
        return WebController.layout(m, "lotes/formulario");
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model m, RedirectAttributes ra) {
        return service.findDetailById(id).map(l -> {
            m.addAttribute("moduloActivo", "lotes");
            m.addAttribute("titulo", "Editar lote " + l.getCodigo());
            m.addAttribute("lote", l);
            m.addAttribute("articulos", articuloService.findAll());
            m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.inicio(),
                BreadcrumbBuilder.link("Lotes", "/web/lotes"),
                BreadcrumbBuilder.active("Editar " + l.getCodigo())));
            return WebController.layout(m, "lotes/formulario");
        }).orElseGet(() -> { ra.addFlashAttribute("error", "Registro no encontrado"); return "redirect:/web/lotes"; });
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model m, RedirectAttributes ra) {
        return service.findDetailById(id).map(l -> {
            m.addAttribute("moduloActivo", "lotes");
            m.addAttribute("titulo", "Lote " + l.getCodigo());
            m.addAttribute("lote", l);
            m.addAttribute("insumos", service.findInsumosDeProductoDetail(id));
            m.addAttribute("usos", service.findProductosQueUsaronInsumoDetail(id));
            m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                    BreadcrumbBuilder.inicio(),
                    BreadcrumbBuilder.link("Lotes", "/web/lotes"),
                    BreadcrumbBuilder.active(l.getCodigo())));
            return WebController.layout(m, "lotes/ver");
        }).orElseGet(() -> { ra.addFlashAttribute("error", "Registro no encontrado"); return "redirect:/web/lotes"; });
    }

    @PostMapping
    public String guardar(@RequestParam(required = false) Long id,
                          @RequestParam String codigo,
                          @RequestParam(required = false) Long articuloId,
                          @RequestParam String fechaProd,
                          @RequestParam String fechaCad,
                          @RequestParam(required = false) BigDecimal cantidad,
                          @RequestParam(required = false) String origen,
                          @RequestParam(required = false) String registroSanitario,
                          RedirectAttributes ra) {
        try {
            Lote l = id != null ? service.findById(id).orElseThrow(() -> new IllegalArgumentException("ID de lote no válido: " + id)) : new Lote();
            l.setCodigo(codigo);
            if (articuloId != null) articuloService.findById(articuloId).ifPresent(l::setArticulo);
            try {
                l.setFechaProduccion(LocalDate.parse(fechaProd));
                l.setFechaCaducidad(LocalDate.parse(fechaCad));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Formato de fecha incorrecto. Use AAAA-MM-DD");
            }
            l.setCantidadInicial(cantidad);
            l.setOrigen(origen);
            l.setNumeroRegistroSanitario(registroSanitario);
            service.save(l);
            ra.addFlashAttribute("exito", "Lote guardado correctamente");
        } catch (RuntimeException e) {
            log.error("Error al guardar lote: {}", e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/lotes";
    }
}
