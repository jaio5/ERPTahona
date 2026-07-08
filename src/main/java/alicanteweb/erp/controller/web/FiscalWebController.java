package alicanteweb.erp.controller.web;

import alicanteweb.erp.util.Flash;
import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.SifModalidad;
import alicanteweb.erp.service.DeclaracionResponsableService;
import alicanteweb.erp.service.EmpresaConfigService;
import alicanteweb.erp.service.FiscalComplianceService;
import alicanteweb.erp.service.Modelo347Service;
import alicanteweb.erp.service.VerifactuEvidenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import alicanteweb.erp.util.Descargas;
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
    private final FiscalComplianceService fiscalComplianceService;
    private final DeclaracionResponsableService declaracionResponsableService;

    public FiscalWebController(Modelo347Service modelo347Service,
                               EmpresaConfigService empresaConfigService,
                               VerifactuEvidenceService evidenceService,
                               FiscalComplianceService fiscalComplianceService,
                               DeclaracionResponsableService declaracionResponsableService) {
        this.modelo347Service = modelo347Service;
        this.empresaConfigService = empresaConfigService;
        this.evidenceService = evidenceService;
        this.fiscalComplianceService = fiscalComplianceService;
        this.declaracionResponsableService = declaracionResponsableService;
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
                BreadcrumbBuilder.inicio(),
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
        return Descargas.adjunto(body, "modelo347-" + ejercicio + ".txt", MediaType.TEXT_PLAIN);
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
                BreadcrumbBuilder.inicio(),
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
                    BreadcrumbBuilder.inicio(),
                    BreadcrumbBuilder.link("VERI*FACTU", "/web/verifactu"),
                    BreadcrumbBuilder.active("Evidencia #" + id)));
            return WebController.layout(model, "fiscal/verifactu-ver");
        }).orElseGet(() -> { Flash.error(ra, "Registro no encontrado"); return "redirect:/web/verifactu"; });
    }

    @PostMapping("/web/verifactu/{id}/reenviar")
    @PreAuthorize("@permisos.puede('verifactu', 'enviar')")
    public String reenviar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            evidenceService.reenviarEvidencia(id);
            Flash.exito(ra, "Reenvío procesado");
        } catch (RuntimeException e) {
            log.error("Error reenviando evidencia {}", id, e);
            Flash.error(ra, e.getMessage());
        }
        return "redirect:/web/verifactu/" + id;
    }

    @PostMapping("/web/verifactu/{id}/verificar")
    public String verificar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            evidenceService.verificarEstadoAEAT(id);
            Flash.exito(ra, "Estado verificado");
        } catch (RuntimeException e) {
            log.error("Error verificando evidencia {}", id, e);
            Flash.error(ra, e.getMessage());
        }
        return "redirect:/web/verifactu/" + id;
    }

    // =========================== CUMPLIMIENTO FISCAL ===========================

    @GetMapping("/web/fiscal/cumplimiento")
    public String cumplimiento(Model model) {
        EmpresaConfig config = empresaConfigService.getConfiguracionActiva().orElse(null);
        model.addAttribute("moduloActivo", "cumplimiento");
        model.addAttribute("titulo", "Cumplimiento fiscal");
        model.addAttribute("report", fiscalComplianceService.diagnosticar());
        model.addAttribute("config", config);
        model.addAttribute("modalidades", SifModalidad.values());
        model.addAttribute("vigente", config != null && empresaConfigService.isFuncionamientoVerifactuVigente(config));
        model.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.inicio(),
                BreadcrumbBuilder.active("Cumplimiento fiscal")));
        return WebController.layout(model, "fiscal/cumplimiento");
    }

    @PostMapping("/web/fiscal/cumplimiento/sif")
    @PreAuthorize("@permisos.puede('fiscal', 'editar')")
    public String guardarDatosSif(@RequestParam(required = false) String verifactuNifEmisor,
                                  @RequestParam(required = false) String verifactuNombreSistema,
                                  @RequestParam(required = false) String verifactuVersionSistema,
                                  @RequestParam(required = false) String verifactuIdDispositivo,
                                  @RequestParam(required = false) String sifModalidad,
                                  @RequestParam(required = false) String productorSoftware,
                                  @RequestParam(required = false) String nifProductorSoftware,
                                  RedirectAttributes ra) {
        try {
            EmpresaConfig config = empresaConfigService.getConfiguracionActivaOrThrow();
            config.setVerifactuNifEmisor(limpiar(verifactuNifEmisor));
            config.setVerifactuNombreSistema(limpiar(verifactuNombreSistema));
            config.setVerifactuVersionSistema(limpiar(verifactuVersionSistema));
            config.setVerifactuIdDispositivo(limpiar(verifactuIdDispositivo));
            config.setProductorSoftware(limpiar(productorSoftware));
            config.setNifProductorSoftware(limpiar(nifProductorSoftware));
            if (sifModalidad != null && !sifModalidad.isBlank()) {
                config.setSifModalidad(SifModalidad.valueOf(sifModalidad));
            }
            empresaConfigService.save(config);
            Flash.exito(ra, "Datos del sistema de facturación guardados");
        } catch (RuntimeException e) {
            log.error("Error guardando datos SIF: {}", e.getMessage(), e);
            Flash.error(ra, e.getMessage());
        }
        return "redirect:/web/fiscal/cumplimiento";
    }

    @PostMapping("/web/fiscal/cumplimiento/declaracion")
    @PreAuthorize("@permisos.puede('fiscal', 'editar')")
    public String emitirDeclaracionResponsable(RedirectAttributes ra) {
        try {
            var pdf = declaracionResponsableService.generarDeclaracionResponsable();
            Flash.exito(ra, "Declaración responsable emitida: " + pdf.getName());
        } catch (RuntimeException e) {
            log.error("Error generando declaración responsable: {}", e.getMessage(), e);
            Flash.error(ra, e.getMessage());
        }
        return "redirect:/web/fiscal/cumplimiento";
    }

    @GetMapping("/web/fiscal/cumplimiento/declaracion/descargar")
    @PreAuthorize("@permisos.puede('fiscal', 'exportar')")
    public Object descargarDeclaracionResponsable(RedirectAttributes ra) {
        // El enlace de descarga solo se muestra cuando la declaración está emitida;
        // ante acceso directo o un enlace obsoleto respondemos con un aviso amable
        // (redirección a la pantalla) en vez de un error 500.
        EmpresaConfig config = empresaConfigService.getConfiguracionActiva().orElse(null);
        String ruta = config != null ? config.getDeclaracionResponsableRuta() : null;
        if (ruta == null || ruta.isBlank()) {
            Flash.error(ra, "Aún no se ha emitido la declaración responsable del sistema.");
            return "redirect:/web/fiscal/cumplimiento";
        }
        java.io.File pdf = new java.io.File(ruta);
        if (!pdf.exists()) {
            Flash.error(ra,
                    "No se encuentra el fichero de la declaración responsable. Vuelve a emitirla.");
            return "redirect:/web/fiscal/cumplimiento";
        }
        return Descargas.pdf(pdf);
    }

    @PostMapping("/web/fiscal/cumplimiento/iniciar")
    @PreAuthorize("@permisos.puede('verifactu', 'enviar')")
    public String iniciarVerifactu(RedirectAttributes ra) {
        try {
            EmpresaConfig config = empresaConfigService.getConfiguracionActivaOrThrow();
            empresaConfigService.iniciarFuncionamientoVerifactu(config.getVerifactuNifEmisor());
            Flash.exito(ra, "Funcionamiento VERI*FACTU iniciado");
        } catch (RuntimeException e) {
            log.error("Error iniciando VERI*FACTU: {}", e.getMessage(), e);
            Flash.error(ra, e.getMessage());
        }
        return "redirect:/web/fiscal/cumplimiento";
    }

    @PostMapping("/web/fiscal/cumplimiento/renuncia")
    @PreAuthorize("@permisos.puede('verifactu', 'enviar')")
    public String programarRenuncia(RedirectAttributes ra) {
        try {
            empresaConfigService.programarRenunciaVerifactuFinDeAnio();
            Flash.exito(ra, "Renuncia programada para el 31 de diciembre");
        } catch (RuntimeException e) {
            log.error("Error programando renuncia VERI*FACTU: {}", e.getMessage(), e);
            Flash.error(ra, e.getMessage());
        }
        return "redirect:/web/fiscal/cumplimiento";
    }

    private String limpiar(String valor) {
        return valor != null && !valor.isBlank() ? valor.trim() : null;
    }
}
