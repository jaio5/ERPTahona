package alicanteweb.erp.controller.web;

import alicanteweb.erp.service.ContabilidadService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@PreAuthorize("@permisos.puede('contabilidad', 'ver')")
@RequestMapping("/web/contabilidad")
public class ContabilidadWebController {

    private final ContabilidadService s;

    public ContabilidadWebController(ContabilidadService s) {
        this.s = s;
    }

    @GetMapping
    public String lista(Model m) {
        m.addAttribute("moduloActivo", "contabilidad");
        m.addAttribute("titulo", "Contabilidad");
        m.addAttribute("asientos", s.obtenerLibroDiario(LocalDate.now().withDayOfMonth(1), LocalDate.now()));
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.inicio(),
            BreadcrumbBuilder.link("Finanzas", "#"),
            BreadcrumbBuilder.active("Contabilidad")));
        return WebController.layout(m, "contabilidad/lista");
    }

    @GetMapping("/balance")
    public String balance(Model m) {
        m.addAttribute("moduloActivo", "contabilidad");
        m.addAttribute("titulo", "Balance y cierre");
        return WebController.layout(m, "contabilidad/balance");
    }

    @GetMapping("/mayor")
    public String mayor(@RequestParam(required = false) String cuenta,
                        @RequestParam(required = false) LocalDate desde,
                        @RequestParam(required = false) LocalDate hasta,
                        Model m) {
        LocalDate d = desde != null ? desde : LocalDate.now().withDayOfYear(1);
        LocalDate h = hasta != null ? hasta : LocalDate.now();
        m.addAttribute("moduloActivo", "contabilidad");
        m.addAttribute("titulo", "Libro mayor");
        m.addAttribute("cuenta", cuenta);
        m.addAttribute("desde", d);
        m.addAttribute("hasta", h);
        m.addAttribute("movimientos", cuenta != null && !cuenta.isBlank()
                ? s.obtenerLibroMayor(cuenta, d, h) : java.util.List.of());
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.inicio(),
                BreadcrumbBuilder.link("Finanzas", "#"),
                BreadcrumbBuilder.active("Libro mayor")));
        return WebController.layout(m, "contabilidad/mayor");
    }

    @GetMapping("/balance-pgc")
    public String balancePgc(@RequestParam(required = false) LocalDate fecha, Model m) {
        LocalDate f = fecha != null ? fecha : LocalDate.now();
        m.addAttribute("moduloActivo", "contabilidad");
        m.addAttribute("titulo", "Balance de situación y PyG");
        m.addAttribute("balance", s.obtenerBalancePgc(f));
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.inicio(),
                BreadcrumbBuilder.link("Finanzas", "#"),
                BreadcrumbBuilder.active("Balance PGC")));
        return WebController.layout(m, "contabilidad/balance-pgc");
    }

    @PostMapping("/apertura")
    @PreAuthorize("@permisos.puede('contabilidad', 'crear')")
    public String generarApertura(@RequestParam int ejercicio,
                                  org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        try {
            var asiento = s.generarAsientoApertura(ejercicio);
            ra.addFlashAttribute("exito", "Asiento de apertura " + asiento.getNumero() + " generado");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/contabilidad/balance-pgc";
    }
}
