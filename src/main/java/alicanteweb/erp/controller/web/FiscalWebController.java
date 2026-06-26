package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.service.EmpresaConfigService;
import alicanteweb.erp.service.Modelo347Service;
import alicanteweb.erp.service.VerifactuEvidenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import org.springframework.security.access.prepost.PreAuthorize;
@Controller
@PreAuthorize("@permisos.puede('fiscal', 'ver')")
public class FiscalWebController {
    private static final Logger log = LoggerFactory.getLogger(FiscalWebController.class);

    private final Modelo347Service modelo347Service;
    private final EmpresaConfigService empresaConfigService;
    private final VerifactuEvidenceService evidenceService;

    public FiscalWebController(Modelo347Service modelo347Service,
                               EmpresaConfigService empresaConfigService,
                               VerifactuEvidenceService evidenceService) {
        this.modelo347Service = modelo347Service;
        this.empresaConfigService = empresaConfigService;
        this.evidenceService = evidenceService;
    }

    @GetMapping("/web/modelo347")
    public String modelo347(@RequestParam(required = false) Integer ejercicio,
                            Model model) {
        if (ejercicio == null) ejercicio = java.time.LocalDate.now().getYear() - 1;
        var resultado = modelo347Service.generarModelo347(ejercicio);
        model.addAttribute("moduloActivo", "modelo347");
        model.addAttribute("titulo", "Modelo 347");
        model.addAttribute("resultado", resultado);
        model.addAttribute("errores", modelo347Service.validarModelo347(resultado));
        model.addAttribute("ejercicio", ejercicio);
        model.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.link("Inicio", "/web/dashboard"),
                BreadcrumbBuilder.active("Modelo 347")));
        return WebController.layout(model, "fiscal/modelo347");
    }

    @GetMapping("/web/modelo347/descargar")
    @PreAuthorize("@permisos.puede('fiscal', 'exportar')")
    public ResponseEntity<byte[]> descargarModelo347(@RequestParam int ejercicio) {
        var resultado = modelo347Service.generarModelo347(ejercicio);
        var errores = modelo347Service.validarModelo347(resultado);
        if (!errores.isEmpty()) {
            throw new IllegalStateException("El Modelo 347 contiene errores: " + String.join("; ", errores));
        }
        EmpresaConfig empresa = empresaConfigService.getConfiguracionActivaOrThrow();
        var declarante = new Modelo347Service.DatosDeclarante(
                empresa.getCif(), empresa.getNombreEmpresa(), empresa.getTelefono(),
                empresa.getNombreEmpresa(), ejercicio);
        byte[] body = modelo347Service.generarFicheroBOE(resultado, declarante)
                .getBytes(StandardCharsets.ISO_8859_1);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=modelo347-" + ejercicio + ".txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(body);
    }

    @GetMapping("/web/verifactu")
    public String verifactu(@RequestParam(required = false) String estado,
                            @RequestParam(required = false) String q,
                            @RequestParam(defaultValue = "0") int page,
                            Model model) {
        var evidencias = evidenceService.findPage(estado, q,
                PageRequest.of(Math.max(0, page), 30,
                        Sort.by(Sort.Direction.DESC, "fechaGeneracionRegistro", "id")));
        model.addAttribute("moduloActivo", "verifactu");
        model.addAttribute("titulo", "Evidencias VERI*FACTU");
        model.addAttribute("evidencias", evidencias);
        model.addAttribute("estado", estado);
        model.addAttribute("q", q);
        model.addAttribute("pendientes", evidenceService.countByEstado("PENDIENTE"));
        model.addAttribute("erroresCount", evidenceService.countByEstado("ERROR"));
        model.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.link("Inicio", "/web/dashboard"),
                BreadcrumbBuilder.active("VERI*FACTU")));
        return WebController.layout(model, "fiscal/verifactu-lista");
    }

    @GetMapping("/web/verifactu/{id}")
    public String verEvidencia(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return evidenceService.findById(id).map(e -> {
            model.addAttribute("moduloActivo", "verifactu");
            model.addAttribute("titulo", "Evidencia " + (e.getNumero() != null ? e.getNumero() : e.getId()));
            model.addAttribute("evidencia", e);
            model.addAttribute("cadenaValida", e.getSerie() != null && evidenceService.validarCadenaIntegridad(e.getSerie()));
            model.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                    BreadcrumbBuilder.link("Inicio", "/web/dashboard"),
                    BreadcrumbBuilder.link("VERI*FACTU", "/web/verifactu"),
                    BreadcrumbBuilder.active("Evidencia #" + id)));
            return WebController.layout(model, "fiscal/verifactu-ver");
        }).orElseGet(() -> { ra.addFlashAttribute("error", "Registro no encontrado"); return "redirect:/web/verifactu"; });
    }

    @PostMapping("/web/verifactu/{id}/reenviar")
    @PreAuthorize("@permisos.puede('verifactu', 'enviar')")
    public String reenviar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            evidenceService.reenviarEvidencia(id);
            ra.addFlashAttribute("exito", "Reenvío procesado");
        } catch (RuntimeException e) {
            log.error("Error reenviando evidencia {}", id, e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/verifactu/" + id;
    }

    @PostMapping("/web/verifactu/{id}/verificar")
    public String verificar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            evidenceService.verificarEstadoAEAT(id);
            ra.addFlashAttribute("exito", "Estado verificado");
        } catch (RuntimeException e) {
            log.error("Error verificando evidencia {}", id, e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/verifactu/" + id;
    }
}
