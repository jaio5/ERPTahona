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
@RequestMapping("/web/proveedores")
class ProveedorWebController {
    private final ProveedorService service;
    public ProveedorWebController(ProveedorService service) { this.service = service; }
    @GetMapping
    public String lista(HttpSession s, Model m, @RequestParam(required = false) String q) {
        if(WebController.requireLogin(s))return"redirect:/web/login";
        List<Proveedor> items = service.findAll();
        if(q!=null&&!q.isBlank()){String t=q.toLowerCase();items=items.stream().filter(p->(p.getNombre()!=null&&p.getNombre().toLowerCase().contains(t))||(p.getCodigo()!=null&&p.getCodigo().toLowerCase().contains(t))).toList();}
        m.addAttribute("moduloActivo","proveedores");m.addAttribute("titulo","Proveedores");m.addAttribute("proveedores",items);
        return WebController.layout(m,"proveedores/lista");
    }
    @GetMapping("/nuevo")
    public String nuevo(HttpSession s,Model m){if(WebController.requireLogin(s))return"redirect:/web/login";m.addAttribute("moduloActivo","proveedores");m.addAttribute("titulo","Nuevo proveedor");return WebController.layout(m,"proveedores/formulario");}
    @PostMapping
    public String guardar(HttpSession s,@RequestParam(required=false)Long id,@RequestParam String codigo,@RequestParam String nombre,@RequestParam(required=false)String cif,@RequestParam(required=false)String telefono,@RequestParam(required=false)String email,RedirectAttributes ra){if(WebController.requireLogin(s))return"redirect:/web/login";try{Proveedor p=id!=null?service.findById(id).orElse(new Proveedor()):new Proveedor();p.setCodigo(codigo);p.setNombre(nombre);p.setCif(cif);p.setTelefono(telefono);p.setEmail(email);service.save(p);ra.addFlashAttribute("exito","Proveedor guardado");}catch(Exception e){ra.addFlashAttribute("error",e.getMessage());}return"redirect:/web/proveedores";}
}
