package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/web/almacenes")
public class AlmacenWebController {
    private final AlmacenService service;
    public AlmacenWebController(AlmacenService service) { this.service = service; }
    @GetMapping
    public String lista(HttpSession s, Model m, @RequestParam(required=false) String q) {
        if(WebController.requireLogin(s)) return "redirect:/web/login";
        List<Almacen> items = service.findAll();
        if(q != null && !q.isBlank()) { String t = q.toLowerCase(); items = items.stream().filter(a -> (a.getNombre()!=null&&a.getNombre().toLowerCase().contains(t))||(a.getCodigo()!=null&&a.getCodigo().toLowerCase().contains(t))).toList(); }
        m.addAttribute("moduloActivo","almacenes"); m.addAttribute("titulo","Almacenes"); m.addAttribute("almacenes",items);
        return WebController.layout(m, "almacenes/lista");
    }
    @GetMapping("/nuevo")
    public String nuevo(HttpSession s, Model m) { if(WebController.requireLogin(s)) return "redirect:/web/login"; m.addAttribute("moduloActivo","almacenes"); m.addAttribute("titulo","Nuevo almacén"); return WebController.layout(m, "almacenes/formulario"); }
    @PostMapping
    public String guardar(HttpSession s, @RequestParam(required=false) Long id, @RequestParam String codigo, @RequestParam String nombre, @RequestParam(required=false) String descripcion, @RequestParam(required=false) BigDecimal capacidad, @RequestParam(required=false) String localidad, @RequestParam(required=false) String responsable, RedirectAttributes ra) {
        if(WebController.requireLogin(s)) return "redirect:/web/login";
        try { Almacen a = id!=null ? service.findById(id).orElse(new Almacen()) : new Almacen(); a.setCodigo(codigo); a.setNombre(nombre); a.setDescripcion(descripcion); a.setCapacidad(capacidad); a.setLocalidad(localidad); a.setResponsable(responsable); service.save(a); ra.addFlashAttribute("exito","Almacén guardado"); } catch(Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/web/almacenes";
    }
}
