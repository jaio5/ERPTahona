package alicanteweb.erp.controller.web;

import alicanteweb.erp.util.Flash;
import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.service.EmpresaConfigService;
import alicanteweb.erp.service.VatValidationService;
import lombok.Getter;
import lombok.Setter;
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
            BreadcrumbBuilder.inicio(),
            BreadcrumbBuilder.link("Administración", "#"),
            BreadcrumbBuilder.active("Empresa")));
        return WebController.layout(m, "empresa/formulario");
    }

    @PreAuthorize("@permisos.puede('configuracion', 'editar')")
    @PostMapping
    public String guardar(@ModelAttribute EmpresaForm form, RedirectAttributes ra) {
        try {
            EmpresaConfig e = s.getConfiguracionActiva().orElse(new EmpresaConfig());
            form.applyTo(e);
            s.save(e);
            Flash.exito(ra, "Configuración guardada");
        } catch (RuntimeException ex) {
            log.error("Error al guardar configuración de empresa: {}", ex.getMessage(), ex);
            Flash.error(ra, ex.getMessage());
        }
        return "redirect:/web/empresa";
    }

    @PostMapping("/validar-vat")
    public String validarVat(@ModelAttribute EmpresaForm form, Model m) {
        EmpresaConfig e = s.getConfiguracionActiva().orElse(new EmpresaConfig());
        form.applyTo(e);
        m.addAttribute("moduloActivo", "empresa");
        m.addAttribute("titulo", "Configuración de empresa");
        m.addAttribute("empresa", e);
        m.addAttribute("vatValidation", vatValidationService.validar(form.getCif()));
        return WebController.layout(m, "empresa/formulario");
    }

    /**
     * Datos del formulario de empresa. Sus nombres de campo coinciden con los {@code name} de los
     * inputs de la plantilla, de modo que Spring los enlaza por @ModelAttribute. Centraliza el
     * volcado a la entidad ({@link #applyTo}) para no duplicarlo entre guardar y validar-vat.
     */
    @Getter
    @Setter
    public static class EmpresaForm {
        private String nombreEmpresa;
        private String cif;
        private String nombreComercial;
        private String direccion;
        private String codigoPostal;
        private String ciudad;
        private String provincia;
        private String telefono;
        private String whatsapp;
        private String email;
        private String registroSanitario;
        private String registroMercantil;
        private String web;
        private String iban;
        private String sepaCreditorId;
        private boolean albaranMedioFolio;

        void applyTo(EmpresaConfig e) {
            e.setNombreEmpresa(nombreEmpresa);
            e.setCif(cif);
            e.setNombreComercial(nombreComercial);
            e.setDireccion(direccion);
            e.setCodigoPostal(codigoPostal);
            e.setCiudad(ciudad);
            e.setProvincia(provincia);
            e.setTelefono(telefono);
            e.setWhatsapp(whatsapp);
            e.setEmail(email);
            e.setRegistroSanitario(registroSanitario);
            e.setRegistroMercantil(registroMercantil);
            e.setWeb(web);
            e.setIban(iban != null && !iban.isBlank() ? iban.replaceAll("\\s+", "").toUpperCase() : null);
            e.setSepaCreditorId(sepaCreditorId != null && !sepaCreditorId.isBlank() ? sepaCreditorId.trim() : null);
            e.setAlbaranMedioFolio(albaranMedioFolio);
        }
    }
}
