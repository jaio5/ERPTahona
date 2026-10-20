package alicanteweb.erp.controller.web;

import alicanteweb.erp.util.Flash;
import alicanteweb.erp.entities.Merma;
import alicanteweb.erp.service.ArticuloService;
import alicanteweb.erp.service.MermaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@PreAuthorize("@permisos.puede('almacen', 'ver')")
@RequestMapping("/web/mermas")
public class MermaWebController {

    private static final Logger log = LoggerFactory.getLogger(MermaWebController.class);

    private final MermaService mermaService;
    private final ArticuloService articuloService;

    public MermaWebController(MermaService mermaService, ArticuloService articuloService) {
        this.mermaService = mermaService;
        this.articuloService = articuloService;
    }

    @GetMapping
    public String lista(Model model,
                        @RequestParam(required = false) String tipo) {
        List<Merma> mermas = (tipo != null && !tipo.isBlank())
                ? mermaService.findByTipo(tipo) : mermaService.findAll();
        model.addAttribute("moduloActivo", "mermas");
        model.addAttribute("titulo", "Mermas");
        model.addAttribute("mermas", mermas);
        model.addAttribute("tipo", tipo);
        return WebController.layout(model, "mermas/lista");
    }

    @GetMapping("/nueva")
    public String formulario(Model model) {
        model.addAttribute("moduloActivo", "mermas");
        model.addAttribute("titulo", "Registrar merma");
        model.addAttribute("articulos", articuloService.findAll());
        return WebController.layout(model, "mermas/formulario");
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return mermaService.findDetailById(id).map(merma -> {
            model.addAttribute("moduloActivo", "mermas");
            model.addAttribute("titulo", "Detalle de merma");
            model.addAttribute("merma", merma);
            model.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                    BreadcrumbBuilder.inicio(),
                    BreadcrumbBuilder.link("Mermas", "/web/mermas"),
                    BreadcrumbBuilder.active("Merma #" + id)));
            return WebController.layout(model, "mermas/ver");
        }).orElseGet(() -> { Flash.error(ra, "Registro no encontrado"); return "redirect:/web/mermas"; });
    }

    /**
     * Alta/edición desde el formulario. La cantidad llega como texto para admitir la coma decimal
     * ('2,5'); un valor no numérico vuelve al formulario con un mensaje en vez de un 400.
     */
    @PostMapping
    public String guardarFormulario(@RequestParam(required = false) Long id,
                                    @RequestParam Long articuloId,
                                    @RequestParam String cantidad,
                                    @RequestParam(required = false) String motivo,
                                    @RequestParam(required = false) String tipo,
                                    @RequestParam(required = false) String responsable,
                                    @RequestParam(required = false) String observaciones,
                                    @RequestParam(required = false) String fecha,
                                    RedirectAttributes ra) {
        BigDecimal cantidadDecimal;
        try {
            cantidadDecimal = DocumentoParserUtil.parseDecimalCampo(cantidad, "Cantidad no válida");
            if (cantidadDecimal == null) throw new IllegalArgumentException("La cantidad es obligatoria");
        } catch (IllegalArgumentException e) {
            Flash.error(ra, e.getMessage());
            return "redirect:/web/mermas/nueva";
        }
        return guardar(id, articuloId, cantidadDecimal, motivo, tipo, responsable, observaciones, fecha, ra);
    }

    /** Guardado con la cantidad ya parseada (lo usa {@link #guardarFormulario}). */
    public String guardar(Long id, Long articuloId, BigDecimal cantidad, String motivo, String tipo,
                          String responsable, String observaciones, String fecha,
                          RedirectAttributes ra) {
        try {
            Merma merma = id != null ? mermaService.findById(id).orElseThrow(() -> new IllegalArgumentException("ID de merma no válido: " + id)) : new Merma();
            articuloService.findById(articuloId).ifPresent(merma::setArticulo);
            merma.setCantidad(cantidad);
            merma.setMotivo(motivo);
            merma.setTipo(tipo);
            merma.setResponsable(responsable);
            merma.setObservaciones(observaciones);
            if (fecha != null && !fecha.isBlank()) merma.setFecha(LocalDate.parse(fecha));
            mermaService.save(merma);
            Flash.exito(ra, "Merma registrada correctamente");
        } catch (RuntimeException e) {
            log.error("Error al guardar merma: {}", e.getMessage(), e);
            Flash.error(ra, e.getMessage());
        }
        return "redirect:/web/mermas";
    }
}
