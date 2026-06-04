package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/web/hojas-ruta")
class HojaRutaWebController {
    private final HojaRutaService service;
    public HojaRutaWebController(HojaRutaService service) { this.service = service; }
    @GetMapping
    public String lista(HttpSession s, Model m, @RequestParam(required=false) String estado) {
        if(WebController.requireLogin(s)) return "redirect:/web/login";
        List<HojaRuta> items = estado!=null&&!estado.isBlank() ? service.findByEstado(estado) : service.findAll();
        m.addAttribute("moduloActivo","hojas-ruta"); m.addAttribute("titulo","Hojas de ruta"); m.addAttribute("hojas",items);
        return WebController.layout(m, "hojas-ruta/lista");
    }
    @GetMapping("/{id}")
    public String ver(HttpSession s, @PathVariable Long id, Model m) {
        if(WebController.requireLogin(s)) return "redirect:/web/login";
        return service.findById(id).map(h -> {
            m.addAttribute("moduloActivo","hojas-ruta"); m.addAttribute("titulo","Hoja "+h.getFecha());
            m.addAttribute("hoja",h); m.addAttribute("entregas", service.getEntregas(id));
            return WebController.layout(m, "hojas-ruta/ver");
        }).orElse("redirect:/web/hojas-ruta");
    }
}
