package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/web/ordenes-produccion")
class OrdenProduccionWebController {
    private final OrdenProduccionService service;
    public OrdenProduccionWebController(OrdenProduccionService service) { this.service = service; }
    @GetMapping
    public String lista(HttpSession s, Model m, @RequestParam(required=false) String estado) {
        if(WebController.requireLogin(s)) return "redirect:/web/login";
        List<OrdenProduccion> items = estado!=null&&!estado.isBlank() ? service.findByEstado(estado) : service.findAll();
        m.addAttribute("moduloActivo","ordenes-produccion"); m.addAttribute("titulo","Órdenes de producción"); m.addAttribute("ordenes",items);
        return WebController.layout(m, "ordenes-produccion/lista");
    }
    @GetMapping("/nuevo")
    public String nuevo(HttpSession s, Model m) { if(WebController.requireLogin(s)) return "redirect:/web/login"; m.addAttribute("moduloActivo","ordenes-produccion"); m.addAttribute("titulo","Nueva orden"); return WebController.layout(m, "ordenes-produccion/formulario"); }
    @PostMapping
    public String guardar(HttpSession s, @RequestParam(required=false) Long id, @RequestParam(required=false) BigDecimal cantidad, @RequestParam(required=false) String observaciones, RedirectAttributes ra) {
        if(WebController.requireLogin(s)) return "redirect:/web/login";
        try { OrdenProduccion o = id!=null ? service.findById(id).orElse(new OrdenProduccion()) : new OrdenProduccion(); o.setCantidadPlanificada(cantidad); o.setFecha(LocalDate.now()); o.setObservaciones(observaciones); service.save(o); ra.addFlashAttribute("exito","Orden guardada"); } catch(Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/web/ordenes-produccion";
    }
}
