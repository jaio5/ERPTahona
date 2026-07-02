package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.Horneada;
import alicanteweb.erp.service.HorneadaService;
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
@PreAuthorize("@permisos.puede('produccion', 'ver')")
@RequestMapping("/web/horneadas")
public class HorneadaWebController {

    private static final Logger log = LoggerFactory.getLogger(HorneadaWebController.class);

    private final HorneadaService horneadaService;

    public HorneadaWebController(HorneadaService horneadaService) {
        this.horneadaService = horneadaService;
    }

    @GetMapping
    public String lista(Model model, @RequestParam(required = false) String q) {
        List<Horneada> horneadas = (q != null && !q.isBlank())
                ? horneadaService.buscar(q) : horneadaService.findAll();
        model.addAttribute("moduloActivo", "horneadas");
        model.addAttribute("titulo", "Horneadas");
        model.addAttribute("horneadas", horneadas);
        model.addAttribute("q", q);
        model.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.link("Inicio", "/web/dashboard"),
            BreadcrumbBuilder.link("Producción", "#"),
            BreadcrumbBuilder.active("Horneadas")));
        return WebController.layout(model, "horneadas/lista");
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("moduloActivo", "horneadas");
        model.addAttribute("titulo", "Registrar horneada");
        model.addAttribute("horneada", new Horneada());
        return WebController.layout(model, "horneadas/formulario");
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return horneadaService.findDetailById(id).map(h -> {
            model.addAttribute("moduloActivo", "horneadas");
            model.addAttribute("titulo", "Horneada del " + h.getFecha());
            model.addAttribute("horneada", h);
            return WebController.layout(model, "horneadas/ver");
        }).orElseGet(() -> { ra.addFlashAttribute("error", "Registro no encontrado"); return "redirect:/web/horneadas"; });
    }

    @PostMapping
    public String guardar(@RequestParam(required = false) Long id,
                          @RequestParam(required = false) String tipo,
                          @RequestParam(required = false) Integer tempIni,
                          @RequestParam(required = false) Integer tempFin,
                          @RequestParam(required = false) BigDecimal cantidad,
                          @RequestParam(required = false) String resultado,
                          @RequestParam(required = false) String observaciones,
                          @RequestParam(required = false) BigDecimal costeManoObra,
                          @RequestParam(required = false) BigDecimal costeEnergia,
                          @RequestParam(required = false) BigDecimal costeMateriales,
                          RedirectAttributes ra) {
        try {
            Horneada horneada = id != null ? horneadaService.findById(id).orElseThrow(() -> new IllegalArgumentException("ID de horneada no válido: " + id)) : new Horneada();
            horneada.setFecha(LocalDate.now());
            horneada.setTipoHorneada(tipo);
            horneada.setTemperaturaInicial(tempIni);
            horneada.setTemperaturaFinal(tempFin);
            horneada.setCantidadProducida(cantidad);
            horneada.setResultado(resultado);
            horneada.setObservaciones(observaciones);
            horneada.setCosteManoObra(costeManoObra);
            horneada.setCosteEnergia(costeEnergia);
            horneada.setCosteMateriales(costeMateriales);
            horneadaService.save(horneada);
            ra.addFlashAttribute("exito", "Horneada registrada correctamente");
        } catch (RuntimeException e) {
            log.error("Error al registrar horneada: {}", e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/horneadas";
    }
}
