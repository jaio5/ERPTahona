package alicanteweb.erp.controller.web;

import alicanteweb.erp.util.Flash;
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
            BreadcrumbBuilder.inicio(),
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
        }).orElseGet(() -> { Flash.error(ra, "Registro no encontrado"); return "redirect:/web/horneadas"; });
    }

    /**
     * Alta desde el formulario. Cantidad y costes llegan como texto para admitir la coma decimal
     * ('2,5'); un valor no numérico vuelve al formulario con un mensaje en vez de un 400.
     */
    @PostMapping
    public String guardarFormulario(@RequestParam(required = false) Long id,
                                    @RequestParam(required = false) String tipo,
                                    @RequestParam(required = false) Integer tempIni,
                                    @RequestParam(required = false) Integer tempFin,
                                    @RequestParam(required = false) String cantidad,
                                    @RequestParam(required = false) String resultado,
                                    @RequestParam(required = false) String observaciones,
                                    @RequestParam(required = false) String costeManoObra,
                                    @RequestParam(required = false) String costeEnergia,
                                    @RequestParam(required = false) String costeMateriales,
                                    RedirectAttributes ra) {
        BigDecimal cantidadDecimal;
        BigDecimal manoObra;
        BigDecimal energia;
        BigDecimal materiales;
        try {
            cantidadDecimal = DocumentoParserUtil.parseDecimalCampo(cantidad, "Cantidad no válida");
            manoObra = DocumentoParserUtil.parseDecimalCampo(costeManoObra, "Importe no válido");
            energia = DocumentoParserUtil.parseDecimalCampo(costeEnergia, "Importe no válido");
            materiales = DocumentoParserUtil.parseDecimalCampo(costeMateriales, "Importe no válido");
        } catch (IllegalArgumentException e) {
            Flash.error(ra, e.getMessage());
            return "redirect:/web/horneadas/nuevo";
        }
        return guardar(id, tipo, tempIni, tempFin, cantidadDecimal, resultado, observaciones,
                manoObra, energia, materiales, ra);
    }

    /** Guardado con los importes ya parseados (lo usa {@link #guardarFormulario}). */
    public String guardar(Long id, String tipo, Integer tempIni, Integer tempFin, BigDecimal cantidad,
                          String resultado, String observaciones, BigDecimal costeManoObra,
                          BigDecimal costeEnergia, BigDecimal costeMateriales,
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
            Flash.exito(ra, "Horneada registrada correctamente");
        } catch (RuntimeException e) {
            log.error("Error al registrar horneada: {}", e.getMessage(), e);
            Flash.error(ra, e.getMessage());
        }
        return "redirect:/web/horneadas";
    }
}
