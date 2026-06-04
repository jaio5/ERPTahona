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
@RequestMapping("/web/articulos")
class ArticuloWebController {
    private final ArticuloService service;
    public ArticuloWebController(ArticuloService service) { this.service = service; }
    @GetMapping
    public String lista(HttpSession s, Model m, @RequestParam(required = false) String q) {
        if(WebController.requireLogin(s))return"redirect:/web/login";
        List<Articulo> items = service.findAll();
        if(q!=null&&!q.isBlank()){String t=q.toLowerCase();items=items.stream().filter(a->(a.getNombre()!=null&&a.getNombre().toLowerCase().contains(t))||(a.getCodigo()!=null&&a.getCodigo().toLowerCase().contains(t))).toList();}
        m.addAttribute("moduloActivo","articulos");m.addAttribute("titulo","Artículos");m.addAttribute("articulos",items);
        return WebController.layout(m,"articulos/lista");
    }
    @GetMapping("/nuevo")
    public String nuevo(HttpSession s,Model m){if(WebController.requireLogin(s))return"redirect:/web/login";m.addAttribute("moduloActivo","articulos");m.addAttribute("titulo","Nuevo artículo");return WebController.layout(m,"articulos/formulario");}
    @GetMapping("/{id}")
    public String ver(HttpSession s,@PathVariable Long id,Model m){if(WebController.requireLogin(s))return"redirect:/web/login";return service.findById(id).map(a->{m.addAttribute("moduloActivo","articulos");m.addAttribute("titulo",a.getNombre());m.addAttribute("articulo",a);return WebController.layout(m,"articulos/ver");}).orElse("redirect:/web/articulos");}
    @PostMapping
    public String guardar(HttpSession s,@RequestParam(required=false)Long id,@RequestParam String codigo,@RequestParam String nombre,@RequestParam(required=false)String categoria,@RequestParam(required=false)BigDecimal pvp,@RequestParam(required=false)BigDecimal iva,@RequestParam(required=false)BigDecimal stock,@RequestParam(required=false)String alergenos,RedirectAttributes ra){if(WebController.requireLogin(s))return"redirect:/web/login";try{Articulo a=id!=null?service.findById(id).orElse(new Articulo()):new Articulo();a.setCodigo(codigo);a.setNombre(nombre);a.setCategoria(categoria);a.setPvp(pvp);a.setIva(iva);a.setStock(stock);a.setAlergenos(alergenos);service.save(a);ra.addFlashAttribute("exito","Artículo guardado");}catch(Exception e){ra.addFlashAttribute("error",e.getMessage());}return"redirect:/web/articulos";}
}
