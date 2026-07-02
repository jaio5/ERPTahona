package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.service.EmpresaConfigService;
import alicanteweb.erp.service.VatValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("@permisos.puede('configuracion', 'ver')")
@RequestMapping("/web/empresa")
public class EmpresaWebController {

    private static final Logger log = LoggerFactory.getLogger(EmpresaWebController.class);

    private final EmpresaConfigService s;
    private final VatValidationService vatValidationService;

    public EmpresaWebController(EmpresaConfigService s, VatValidationService vatValidationService) {
        this.s = s;
        this.vatValidationService = vatValidationService;
    }

    @GetMapping
    public String form(Model m) {
        m.addAttribute("moduloActivo", "empresa");
        m.addAttribute("titulo", "Configuración de empresa");
        m.addAttribute("empresa", s.getConfiguracionActiva().orElse(null));
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.link("Inicio", "/web/dashboard"),
            BreadcrumbBuilder.link("Administración", "#"),
            BreadcrumbBuilder.active("Empresa")));
        return WebController.layout(m, "empresa/formulario");
    }

    @PostMapping
    public String guardar(@RequestParam String nombreEmpresa,
                          @RequestParam String cif,
                          @RequestParam(required = false) String direccion,
                          @RequestParam(required = false) String codigoPostal,
                          @RequestParam(required = false) String ciudad,
                          @RequestParam(required = false) String provincia,
                          @RequestParam(required = false) String telefono,
                          @RequestParam(required = false) String email,
                          @RequestParam(required = false) String registroSanitario,
                          @RequestParam(required = false) String registroMercantil,
                          @RequestParam(required = false) String web,
                          RedirectAttributes ra) {
        try {
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
            s.save(e);
            ra.addFlashAttribute("exito", "Configuración guardada");
        } catch (RuntimeException ex) {
            log.error("Error al guardar configuración de empresa: {}", ex.getMessage(), ex);
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/web/empresa";
    }

    @PostMapping("/validar-vat")
    public String validarVat(Model m,
                             @RequestParam String nombreEmpresa,
                             @RequestParam String cif,
                             @RequestParam(required = false) String direccion,
                             @RequestParam(required = false) String codigoPostal,
                             @RequestParam(required = false) String ciudad,
                             @RequestParam(required = false) String provincia,
                             @RequestParam(required = false) String telefono,
                             @RequestParam(required = false) String email,
                             @RequestParam(required = false) String registroSanitario,
                             @RequestParam(required = false) String registroMercantil,
                             @RequestParam(required = false) String web) {
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
        m.addAttribute("moduloActivo", "empresa");
        m.addAttribute("titulo", "Configuración de empresa");
        m.addAttribute("empresa", e);
        m.addAttribute("vatValidation", vatValidationService.validar(cif));
        return WebController.layout(m, "empresa/formulario");
    }
}
