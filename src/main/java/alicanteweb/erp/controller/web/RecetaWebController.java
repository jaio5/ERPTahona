package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/web/recetas")
class RecetaWebController {
    private final RecetaService service;
    public RecetaWebController(RecetaService service) { this.service = service; }
    @GetMapping
    public String lista(HttpSession s, Model m, @RequestParam(required=false) String q) {
        if(WebController.requireLogin(s)) return "redirect:/web/login";
        List<Receta> items = service.findAll();
        if(q != null && !q.isBlank()) { String t = q.toLowerCase(); items = items.stream().filter(r -> (r.getNombre()!=null&&r.getNombre().toLowerCase().contains(t))||(r.getCodigo()!=null&&r.getCodigo().toLowerCase().contains(t))).toList(); }
        m.addAttribute("moduloActivo","recetas"); m.addAttribute("titulo","Recetas"); m.addAttribute("recetas",items);
        return WebController.layout(m, "recetas/lista");
    }
    @GetMapping("/nuevo")
    public String nuevo(HttpSession s, Model m) { if(WebController.requireLogin(s)) return "redirect:/web/login"; m.addAttribute("moduloActivo","recetas"); m.addAttribute("titulo","Nueva receta"); return WebController.layout(m, "recetas/formulario"); }
    @PostMapping
    public String guardar(HttpSession s, @RequestParam(required=false) Long id, @RequestParam String codigo, @RequestParam String nombre, @RequestParam(required=false) String descripcion, @RequestParam(required=false) Integer tiempoPrep, @RequestParam(required=false) Integer tiempoHorneado, @RequestParam(required=false) Integer temperatura, @RequestParam(required=false) String alergenos, RedirectAttributes ra) {
        if(WebController.requireLogin(s)) return "redirect:/web/login";
        try { Receta r = id!=null ? service.findById(id).orElse(new Receta()) : new Receta(); r.setCodigo(codigo); r.setNombre(nombre); r.setDescripcion(descripcion); r.setTiempoPreparacion(tiempoPrep); r.setTiempoHorneado(tiempoHorneado); r.setTemperaturaHorneado(temperatura); r.setAlergenos(alergenos); service.save(r); ra.addFlashAttribute("exito","Receta guardada"); } catch(Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/web/recetas";
    }
}
