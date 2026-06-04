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
@RequestMapping("/web/vehiculos")
class VehiculoWebController {
    private final VehiculoService s;
    public VehiculoWebController(VehiculoService s){this.s=s;}
    @GetMapping
    public String lista(HttpSession ses,Model m,@RequestParam(required=false)String q){if(WebController.requireLogin(ses))return"redirect:/web/login";List<Vehiculo> items=s.findAll();if(q!=null&&!q.isBlank()){String t=q.toLowerCase();items=items.stream().filter(v->(v.getMatricula()!=null&&v.getMatricula().toLowerCase().contains(t))||(v.getMarca()!=null&&v.getMarca().toLowerCase().contains(t))).toList();}m.addAttribute("moduloActivo","vehiculos");m.addAttribute("titulo","Vehículos");m.addAttribute("vehiculos",items);return WebController.layout(m,"vehiculos/lista");}
    @GetMapping("/nuevo")
    public String nuevo(HttpSession ses,Model m){if(WebController.requireLogin(ses))return"redirect:/web/login";m.addAttribute("moduloActivo","vehiculos");m.addAttribute("titulo","Nuevo vehículo");return WebController.layout(m,"vehiculos/formulario");}
    @PostMapping
    public String guardar(HttpSession ses,@RequestParam(required=false)Long id,@RequestParam String matricula,@RequestParam(required=false)String marca,@RequestParam(required=false)String modelo,@RequestParam(required=false)String tipo,@RequestParam(required=false)BigDecimal capacidad,RedirectAttributes ra){if(WebController.requireLogin(ses))return"redirect:/web/login";try{Vehiculo v=id!=null?s.findById(id).orElse(new Vehiculo()):new Vehiculo();v.setMatricula(matricula);v.setMarca(marca);v.setModelo(modelo);v.setTipo(tipo);v.setCapacidadKg(capacidad);s.save(v);ra.addFlashAttribute("exito","Vehículo guardado");}catch(Exception e){ra.addFlashAttribute("error",e.getMessage());}return"redirect:/web/vehiculos";}
}

@Controller
@RequestMapping("/web/rutas")
class RutaWebController {
    private final RutaRepartoService s;
    public RutaWebController(RutaRepartoService s){this.s=s;}
    @GetMapping
    public String lista(HttpSession ses,Model m,@RequestParam(required=false)String q){if(WebController.requireLogin(ses))return"redirect:/web/login";List<RutaReparto> items=s.findAll();if(q!=null&&!q.isBlank()){String t=q.toLowerCase();items=items.stream().filter(r->(r.getNombre()!=null&&r.getNombre().toLowerCase().contains(t))||(r.getCodigo()!=null&&r.getCodigo().toLowerCase().contains(t))).toList();}m.addAttribute("moduloActivo","rutas");m.addAttribute("titulo","Rutas de reparto");m.addAttribute("rutas",items);return WebController.layout(m,"rutas/lista");}
    @GetMapping("/nuevo")
    public String nuevo(HttpSession ses,Model m){if(WebController.requireLogin(ses))return"redirect:/web/login";m.addAttribute("moduloActivo","rutas");m.addAttribute("titulo","Nueva ruta");return WebController.layout(m,"rutas/formulario");}
    @PostMapping
    public String guardar(HttpSession ses,@RequestParam(required=false)Long id,@RequestParam String codigo,@RequestParam String nombre,@RequestParam(required=false)String conductor,@RequestParam(required=false)String descripcion,RedirectAttributes ra){if(WebController.requireLogin(ses))return"redirect:/web/login";try{RutaReparto r=id!=null?s.findById(id).orElse(new RutaReparto()):new RutaReparto();r.setCodigo(codigo);r.setNombre(nombre);r.setConductor(conductor);r.setDescripcion(descripcion);s.save(r);ra.addFlashAttribute("exito","Ruta guardada");}catch(Exception e){ra.addFlashAttribute("error",e.getMessage());}return"redirect:/web/rutas";}
}

@Controller
@RequestMapping("/web/devoluciones")
class DevolucionWebController {
    private final DevolucionService s;
    private final ClienteService cs;
    public DevolucionWebController(DevolucionService s,ClienteService cs){this.s=s;this.cs=cs;}
    @GetMapping
    public String lista(HttpSession ses,Model m,@RequestParam(required=false)String q){if(WebController.requireLogin(ses))return"redirect:/web/login";List<Devolucion> items=s.findAll();if(q!=null&&!q.isBlank()){String t=q.toLowerCase();items=items.stream().filter(d->(d.getNumero()!=null&&d.getNumero().toLowerCase().contains(t))||(d.getMotivo()!=null&&d.getMotivo().toLowerCase().contains(t))).toList();}m.addAttribute("moduloActivo","devoluciones");m.addAttribute("titulo","Devoluciones");m.addAttribute("devoluciones",items);return WebController.layout(m,"devoluciones/lista");}
    @GetMapping("/nuevo")
    public String nuevo(HttpSession ses,Model m){if(WebController.requireLogin(ses))return"redirect:/web/login";m.addAttribute("moduloActivo","devoluciones");m.addAttribute("titulo","Nueva devolución");m.addAttribute("clientes",cs.findAll());return WebController.layout(m,"devoluciones/formulario");}
    @PostMapping
    public String guardar(HttpSession ses,@RequestParam Long clienteId,@RequestParam String numero,@RequestParam(required=false)String motivo,@RequestParam(required=false)String observaciones,RedirectAttributes ra){if(WebController.requireLogin(ses))return"redirect:/web/login";try{Devolucion d=new Devolucion();cs.findById(clienteId).ifPresent(d::setCliente);d.setNumero(numero);d.setFecha(LocalDate.now());d.setMotivo(motivo);d.setObservaciones(observaciones);s.save(d);ra.addFlashAttribute("exito","Devolución guardada");}catch(Exception e){ra.addFlashAttribute("error",e.getMessage());}return"redirect:/web/devoluciones";}
}

@Controller
@RequestMapping("/web/horneadas")
class HorneadaWebController {
    private final HorneadaService s;
    public HorneadaWebController(HorneadaService s){this.s=s;}
    @GetMapping
    public String lista(HttpSession ses,Model m,@RequestParam(required=false)String q){if(WebController.requireLogin(ses))return"redirect:/web/login";List<Horneada> items=s.findAll();if(q!=null&&!q.isBlank()){String t=q.toLowerCase();items=items.stream().filter(h->(h.getTipoHorneada()!=null&&h.getTipoHorneada().toLowerCase().contains(t))||(h.getResultado()!=null&&h.getResultado().toLowerCase().contains(t))).toList();}m.addAttribute("moduloActivo","horneadas");m.addAttribute("titulo","Horneadas");m.addAttribute("horneadas",items);return WebController.layout(m,"horneadas/lista");}
    @GetMapping("/nuevo")
    public String nuevo(HttpSession ses,Model m){if(WebController.requireLogin(ses))return"redirect:/web/login";m.addAttribute("moduloActivo","horneadas");m.addAttribute("titulo","Registrar horneada");return WebController.layout(m,"horneadas/formulario");}
    @PostMapping
    public String guardar(HttpSession ses,@RequestParam(required=false)Long id,@RequestParam(required=false)String tipo,@RequestParam(required=false)Integer tempIni,@RequestParam(required=false)Integer tempFin,@RequestParam(required=false)BigDecimal cantidad,@RequestParam(required=false)String resultado,@RequestParam(required=false)String observaciones,RedirectAttributes ra){if(WebController.requireLogin(ses))return"redirect:/web/login";try{Horneada h=id!=null?s.findById(id).orElse(new Horneada()):new Horneada();h.setFecha(LocalDate.now());h.setTipoHorneada(tipo);h.setTemperaturaInicial(tempIni);h.setTemperaturaFinal(tempFin);h.setCantidadProducida(cantidad);h.setResultado(resultado);h.setObservaciones(observaciones);s.save(h);ra.addFlashAttribute("exito","Horneada registrada");}catch(Exception e){ra.addFlashAttribute("error",e.getMessage());}return"redirect:/web/horneadas";}
}

@Controller
@RequestMapping("/web/appcc")
class AppccWebController {
    private final AppccControlService s;
    public AppccWebController(AppccControlService s){this.s=s;}
    @GetMapping
    public String lista(HttpSession ses,Model m,@RequestParam(required=false)String q){if(WebController.requireLogin(ses))return"redirect:/web/login";List<AppccControl> items=s.findAll();if(q!=null&&!q.isBlank()){String t=q.toLowerCase();items=items.stream().filter(c->(c.getPuntoCritico()!=null&&c.getPuntoCritico().toLowerCase().contains(t))||(c.getResponsable()!=null&&c.getResponsable().toLowerCase().contains(t))).toList();}m.addAttribute("moduloActivo","appcc");m.addAttribute("titulo","Controles APPCC");m.addAttribute("controles",items);return WebController.layout(m,"appcc/lista");}
    @GetMapping("/nuevo")
    public String nuevo(HttpSession ses,Model m){if(WebController.requireLogin(ses))return"redirect:/web/login";m.addAttribute("moduloActivo","appcc");m.addAttribute("titulo","Nuevo control APPCC");return WebController.layout(m,"appcc/formulario");}
    @PostMapping
    public String guardar(HttpSession ses,@RequestParam(required=false)Long id,@RequestParam String puntoCritico,@RequestParam(required=false)BigDecimal temperatura,@RequestParam(required=false)BigDecimal limite,@RequestParam(required=false)String resultado,@RequestParam(required=false)String accion,@RequestParam(required=false)String responsable,RedirectAttributes ra){if(WebController.requireLogin(ses))return"redirect:/web/login";try{AppccControl c=id!=null?s.findById(id).orElse(new AppccControl()):new AppccControl();c.setFecha(LocalDate.now());c.setPuntoCritico(puntoCritico);c.setTemperatura(temperatura);c.setLimiteCritico(limite);c.setResultado(resultado);c.setAccionCorrectiva(accion);c.setResponsable(responsable);s.save(c);ra.addFlashAttribute("exito","Control APPCC guardado");}catch(Exception e){ra.addFlashAttribute("error",e.getMessage());}return"redirect:/web/appcc";}
}
