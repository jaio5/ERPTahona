package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import alicanteweb.erp.repository.UsuarioRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/web/usuarios")
class UsuarioWebController {
    private final UsuarioService s; private final UsuarioRepository ur;
    public UsuarioWebController(UsuarioService s, UsuarioRepository ur) { this.s = s; this.ur = ur; }
    @GetMapping
    public String lista(HttpSession ses, Model m) {
        if(WebController.requireLogin(ses)) return "redirect:/web/login";
        m.addAttribute("moduloActivo","usuarios"); m.addAttribute("titulo","Usuarios"); m.addAttribute("usuarios",ur.findAll());
        return WebController.layout(m, "usuarios/lista");
    }
    @GetMapping("/nuevo")
    public String nuevo(HttpSession ses, Model m) { if(WebController.requireLogin(ses)) return "redirect:/web/login"; m.addAttribute("moduloActivo","usuarios"); m.addAttribute("titulo","Nuevo usuario"); return WebController.layout(m, "usuarios/formulario"); }
    @PostMapping
    public String guardar(HttpSession ses, @RequestParam String username, @RequestParam String password, @RequestParam(required=false) String nombre, @RequestParam(required=false) String email, @RequestParam(required=false) Long rolId, RedirectAttributes ra) {
        if(WebController.requireLogin(ses)) return "redirect:/web/login";
        try { Usuario u = new Usuario(); u.setUsername(username); u.setPassword(password); u.setNombre(nombre); u.setEmail(email); u.setEnabled(true); s.crearUsuario(u, password); ra.addFlashAttribute("exito","Usuario creado"); } catch(Exception e) { ra.addFlashAttribute("error",e.getMessage()); }
        return "redirect:/web/usuarios";
    }
}

@Controller
@RequestMapping("/web/backups")
class BackupWebController {
    private final BackupService s;
    public BackupWebController(BackupService s) { this.s = s; }
    @GetMapping
    public String lista(HttpSession ses, Model m) {
        if(WebController.requireLogin(ses)) return "redirect:/web/login";
        m.addAttribute("moduloActivo","backups"); m.addAttribute("titulo","Backups");
        try {
            m.addAttribute("backups",s.listarBackups());
        } catch(Exception e) {
            m.addAttribute("backups", Collections.emptyList());
            m.addAttribute("error", e.getMessage());
        }
        return WebController.layout(m, "backups/lista");
    }
    @PostMapping("/crear")
    public String crear(HttpSession ses, RedirectAttributes ra) {
        if(WebController.requireLogin(ses)) return "redirect:/web/login";
        try { s.realizarBackup(); ra.addFlashAttribute("exito","Backup creado"); } catch(Exception e) { ra.addFlashAttribute("error",e.getMessage()); }
        return "redirect:/web/backups";
    }
}

@Controller
@RequestMapping("/web/empresa")
class EmpresaWebController {
    private final EmpresaConfigService s;
    private final VatValidationService vatValidationService;
    public EmpresaWebController(EmpresaConfigService s, VatValidationService vatValidationService) {
        this.s = s;
        this.vatValidationService = vatValidationService;
    }
    @GetMapping
    public String form(HttpSession ses, Model m) {
        if(WebController.requireLogin(ses)) return "redirect:/web/login";
        m.addAttribute("moduloActivo","empresa"); m.addAttribute("titulo","Configuración de empresa"); m.addAttribute("empresa",s.getConfiguracionActiva().orElse(null));
        return WebController.layout(m, "empresa/formulario");
    }
    @PostMapping
    public String guardar(HttpSession ses, @RequestParam String nombreEmpresa, @RequestParam String cif, @RequestParam(required=false) String direccion, @RequestParam(required=false) String codigoPostal, @RequestParam(required=false) String ciudad, @RequestParam(required=false) String provincia, @RequestParam(required=false) String telefono, @RequestParam(required=false) String email, @RequestParam(required=false) String registroSanitario, @RequestParam(required=false) String registroMercantil, @RequestParam(required=false) String web, RedirectAttributes ra) {
        if(WebController.requireLogin(ses)) return "redirect:/web/login";
        try { EmpresaConfig e = s.getConfiguracionActiva().orElse(new EmpresaConfig()); e.setNombreEmpresa(nombreEmpresa); e.setCif(cif); e.setDireccion(direccion); e.setCodigoPostal(codigoPostal); e.setCiudad(ciudad); e.setProvincia(provincia); e.setTelefono(telefono); e.setEmail(email); e.setRegistroSanitario(registroSanitario); e.setRegistroMercantil(registroMercantil); e.setWeb(web); s.save(e); ra.addFlashAttribute("exito","Configuración guardada"); } catch(Exception ex) { ra.addFlashAttribute("error",ex.getMessage()); }
        return "redirect:/web/empresa";
    }

    @PostMapping("/validar-vat")
    public String validarVat(HttpSession ses, Model m, @RequestParam String nombreEmpresa, @RequestParam String cif, @RequestParam(required=false) String direccion, @RequestParam(required=false) String codigoPostal, @RequestParam(required=false) String ciudad, @RequestParam(required=false) String provincia, @RequestParam(required=false) String telefono, @RequestParam(required=false) String email, @RequestParam(required=false) String registroSanitario, @RequestParam(required=false) String registroMercantil, @RequestParam(required=false) String web) {
        if(WebController.requireLogin(ses)) return "redirect:/web/login";
        EmpresaConfig e = s.getConfiguracionActiva().orElse(new EmpresaConfig());
        e.setNombreEmpresa(nombreEmpresa);
        e.setCif(cif);
        e.setDireccion(direccion);
        e.setCodigoPostal(codigoPostal);
        e.setCiudad(ciudad);
        e.setProvincia(provincia);
        e.setTelefono(telefono);
        e.setEmail(email);
        e.setRegistroSanitario(registroSanitario);
        e.setRegistroMercantil(registroMercantil);
        e.setWeb(web);
        m.addAttribute("moduloActivo","empresa");
        m.addAttribute("titulo","Configuracion de empresa");
        m.addAttribute("empresa", e);
        m.addAttribute("vatValidation", vatValidationService.validar(cif));
        return WebController.layout(m, "empresa/formulario");
    }
}

@Controller
@RequestMapping("/web/contabilidad")
class ContabilidadWebController {
    private final ContabilidadService s;
    public ContabilidadWebController(ContabilidadService s) { this.s = s; }
    @GetMapping
    public String lista(HttpSession ses, Model m) {
        if(WebController.requireLogin(ses)) return "redirect:/web/login";
        m.addAttribute("moduloActivo","contabilidad"); m.addAttribute("titulo","Contabilidad");
        m.addAttribute("asientos",s.obtenerLibroDiario(LocalDate.now().withDayOfMonth(1), LocalDate.now()));
        return WebController.layout(m, "contabilidad/lista");
    }
}

@Controller
@RequestMapping("/web/tesoreria")
class TesoreriaWebController {
    private final MovimientoCajaService cajaS; private final MovimientoBancoService bancoS;
    public TesoreriaWebController(MovimientoCajaService cajaS, MovimientoBancoService bancoS) { this.cajaS = cajaS; this.bancoS = bancoS; }
    @GetMapping
    public String caja(HttpSession ses, Model m) {
        if(WebController.requireLogin(ses)) return "redirect:/web/login";
        m.addAttribute("moduloActivo","tesoreria"); m.addAttribute("titulo","Tesorería");
        m.addAttribute("caja",cajaS.findAll()); m.addAttribute("banco",bancoS.findAll());
        return WebController.layout(m, "tesoreria/lista");
    }
}

@Controller
@RequestMapping("/web/auditoria")
class AuditoriaWebController {
    private final AuditoriaService s;
    public AuditoriaWebController(AuditoriaService s) { this.s = s; }
    @GetMapping
    public String lista(HttpSession ses, Model m) {
        if(WebController.requireLogin(ses)) return "redirect:/web/login";
        m.addAttribute("moduloActivo","auditoria"); m.addAttribute("titulo","Auditoría");
        m.addAttribute("registros",s.obtenerRecientes());
        return WebController.layout(m, "auditoria/lista");
    }
}
