package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@PreAuthorize("@permisos.puede('reparto', 'ver')")
@RequestMapping("/web/hojas-ruta")
public class HojaRutaWebController {
    private static final Logger log = LoggerFactory.getLogger(HojaRutaWebController.class);
    private final HojaRutaService service;
    private final RutaRepartoService rutaService;
    public HojaRutaWebController(HojaRutaService service, RutaRepartoService rutaService) {
        this.service = service;
        this.rutaService = rutaService;
    }
    @GetMapping
    public String lista(Model m, @RequestParam(required=false) String estado) {
        List<HojaRuta> items = estado!=null&&!estado.isBlank() ? service.findByEstado(estado) : service.findAll();
        m.addAttribute("moduloActivo","hojas-ruta"); m.addAttribute("titulo","Hojas de ruta"); m.addAttribute("hojas",items);
        return WebController.layout(m, "hojas-ruta/lista");
    }
    @GetMapping("/nuevo")
    public String nuevo(Model m) {
        m.addAttribute("moduloActivo", "hojas-ruta");
        m.addAttribute("titulo", "Nueva hoja de ruta");
        m.addAttribute("rutas", rutaService.findAll());
        return WebController.layout(m, "hojas-ruta/formulario");
    }

    @PostMapping
    public String guardar(@RequestParam Long rutaId, @RequestParam String fecha,
                          RedirectAttributes ra) {
        try {
            HojaRuta h = service.generarDesdeRuta(rutaId, LocalDate.parse(fecha));
            ra.addFlashAttribute("exito", "Hoja de ruta generada");
            return "redirect:/web/hojas-ruta/" + h.getId();
        } catch (RuntimeException e) {
            log.error("Error al generar hoja de ruta: {}", e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/web/hojas-ruta";
        }
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model m, RedirectAttributes ra) {
        return service.findDetailById(id).map(h -> {
            m.addAttribute("moduloActivo","hojas-ruta"); m.addAttribute("titulo","Hoja "+h.getFecha());
            m.addAttribute("hoja",h); m.addAttribute("entregas", service.getEntregasDetalle(id));
            return WebController.layout(m, "hojas-ruta/ver");
        }).orElseGet(() -> { ra.addFlashAttribute("error", "Registro no encontrado"); return "redirect:/web/hojas-ruta"; });
    }
}
