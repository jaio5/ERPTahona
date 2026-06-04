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
@RequestMapping("/web/pedidos-venta")
class PedidoVentaWebController {
    private final PedidoService s; private final ClienteService cs;
    public PedidoVentaWebController(PedidoService s, ClienteService cs) { this.s = s; this.cs = cs; }
    @GetMapping
    public String lista(HttpSession ses, Model m, @RequestParam(required=false) String q) {
        if(WebController.requireLogin(ses)) return "redirect:/web/login";
        List<Pedido> items = s.findAll();
        if(q!=null&&!q.isBlank()){String t=q.toLowerCase();items=items.stream().filter(p->(p.getNumero()!=null&&p.getNumero().toLowerCase().contains(t))||(p.getCliente()!=null&&p.getCliente().getNombre()!=null&&p.getCliente().getNombre().toLowerCase().contains(t))).toList();}
        m.addAttribute("moduloActivo","pedidos-venta"); m.addAttribute("titulo","Pedidos de venta"); m.addAttribute("pedidos",items);
        return WebController.layout(m, "pedidos-venta/lista");
    }
    @GetMapping("/nuevo")
    public String nuevo(HttpSession ses, Model m) { if(WebController.requireLogin(ses)) return "redirect:/web/login"; m.addAttribute("moduloActivo","pedidos-venta"); m.addAttribute("titulo","Nuevo pedido"); m.addAttribute("clientes",cs.findAll()); return WebController.layout(m, "pedidos-venta/formulario"); }
    @PostMapping
    public String guardar(HttpSession ses, @RequestParam Long clienteId, @RequestParam(required=false) String observaciones, RedirectAttributes ra) {
        if(WebController.requireLogin(ses)) return "redirect:/web/login";
        try { Pedido p = new Pedido(); cs.findById(clienteId).ifPresent(p::setCliente); p.setFecha(LocalDate.now()); p.setEstado("PENDIENTE"); p.setObservaciones(observaciones); s.save(p); ra.addFlashAttribute("exito","Pedido guardado"); } catch(Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/web/pedidos-venta";
    }
}

@Controller
@RequestMapping("/web/pedidos-compra")
class PedidoCompraWebController {
    private final PedidoCompraService s; private final ProveedorService ps;
    public PedidoCompraWebController(PedidoCompraService s, ProveedorService ps) { this.s = s; this.ps = ps; }
    @GetMapping
    public String lista(HttpSession ses, Model m, @RequestParam(required=false) String q) {
        if(WebController.requireLogin(ses)) return "redirect:/web/login";
        List<PedidoCompra> items = s.findAll();
        if(q!=null&&!q.isBlank()){String t=q.toLowerCase();items=items.stream().filter(p->(p.getNumero()!=null&&p.getNumero().toLowerCase().contains(t))||(p.getProveedor()!=null&&p.getProveedor().getNombre()!=null&&p.getProveedor().getNombre().toLowerCase().contains(t))).toList();}
        m.addAttribute("moduloActivo","pedidos-compra"); m.addAttribute("titulo","Pedidos de compra"); m.addAttribute("pedidos",items);
        return WebController.layout(m, "pedidos-compra/lista");
    }
    @GetMapping("/nuevo")
    public String nuevo(HttpSession ses, Model m) { if(WebController.requireLogin(ses)) return "redirect:/web/login"; m.addAttribute("moduloActivo","pedidos-compra"); m.addAttribute("titulo","Nuevo pedido compra"); m.addAttribute("proveedores",ps.findAll()); return WebController.layout(m, "pedidos-compra/formulario"); }
    @PostMapping
    public String guardar(HttpSession ses, @RequestParam Long proveedorId, @RequestParam(required=false) String observaciones, RedirectAttributes ra) {
        if(WebController.requireLogin(ses)) return "redirect:/web/login";
        try { PedidoCompra p = new PedidoCompra(); ps.findById(proveedorId).ifPresent(p::setProveedor); p.setFecha(LocalDate.now()); p.setEstado("BORRADOR"); p.setObservaciones(observaciones); s.save(p); ra.addFlashAttribute("exito","Pedido guardado"); } catch(Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/web/pedidos-compra";
    }
}

@Controller
@RequestMapping("/web/presupuestos")
class PresupuestoWebController {
    private final PresupuestoService s; private final ClienteService cs;
    public PresupuestoWebController(PresupuestoService s, ClienteService cs) { this.s = s; this.cs = cs; }
    @GetMapping
    public String lista(HttpSession ses, Model m, @RequestParam(required=false) String q) {
        if(WebController.requireLogin(ses)) return "redirect:/web/login";
        List<Presupuesto> items = s.findAll();
        if(q!=null&&!q.isBlank()){String t=q.toLowerCase();items=items.stream().filter(p->(p.getNumero()!=null&&p.getNumero().toLowerCase().contains(t))||(p.getCliente()!=null&&p.getCliente().getNombre()!=null&&p.getCliente().getNombre().toLowerCase().contains(t))).toList();}
        m.addAttribute("moduloActivo","presupuestos"); m.addAttribute("titulo","Presupuestos"); m.addAttribute("presupuestos",items);
        return WebController.layout(m, "presupuestos/lista");
    }
    @GetMapping("/nuevo")
    public String nuevo(HttpSession ses, Model m) { if(WebController.requireLogin(ses)) return "redirect:/web/login"; m.addAttribute("moduloActivo","presupuestos"); m.addAttribute("titulo","Nuevo presupuesto"); m.addAttribute("clientes",cs.findAll()); return WebController.layout(m, "presupuestos/formulario"); }
    @PostMapping
    public String guardar(HttpSession ses, @RequestParam Long clienteId, @RequestParam(required=false) String observaciones, RedirectAttributes ra) {
        if(WebController.requireLogin(ses)) return "redirect:/web/login";
        try { Presupuesto p = new Presupuesto(); cs.findById(clienteId).ifPresent(p::setCliente); p.setFecha(LocalDate.now()); p.setFechaValidez(LocalDate.now().plusDays(15)); p.setEstado("BORRADOR"); p.setObservaciones(observaciones); s.save(p); ra.addFlashAttribute("exito","Presupuesto guardado"); } catch(Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/web/presupuestos";
    }
}

@Controller
@RequestMapping("/web/facturas-compra")
class FacturaCompraWebController {
    private final FacturaCompraService s; private final ProveedorService ps;
    public FacturaCompraWebController(FacturaCompraService s, ProveedorService ps) { this.s = s; this.ps = ps; }
    @GetMapping
    public String lista(HttpSession ses, Model m, @RequestParam(required=false) String q) {
        if(WebController.requireLogin(ses)) return "redirect:/web/login";
        List<FacturaCompra> items = s.obtenerTodas();
        if(q!=null&&!q.isBlank()){String t=q.toLowerCase();items=items.stream().filter(f->(f.getNumero()!=null&&f.getNumero().toLowerCase().contains(t))||(f.getProveedor()!=null&&f.getProveedor().getNombre()!=null&&f.getProveedor().getNombre().toLowerCase().contains(t))).toList();}
        m.addAttribute("moduloActivo","facturas-compra"); m.addAttribute("titulo","Facturas de compra"); m.addAttribute("facturas",items);
        return WebController.layout(m, "facturas-compra/lista");
    }
    @GetMapping("/nuevo")
    public String nuevo(HttpSession ses, Model m) { if(WebController.requireLogin(ses)) return "redirect:/web/login"; m.addAttribute("moduloActivo","facturas-compra"); m.addAttribute("titulo","Nueva factura compra"); m.addAttribute("proveedores",ps.findAll()); return WebController.layout(m, "facturas-compra/formulario"); }
}
