package alicanteweb.erp.controller.web;

import alicanteweb.erp.service.LibroIvaService;
import alicanteweb.erp.util.Descargas;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/**
 * Libros registro de IVA y borradores de modelos 303/111.
 */
@Controller
@PreAuthorize("@permisos.puede('fiscal', 'ver')")
@RequestMapping("/web/fiscal")
public class LibroIvaWebController {

    private final LibroIvaService libroIvaService;

    public LibroIvaWebController(LibroIvaService libroIvaService) {
        this.libroIvaService = libroIvaService;
    }

    @GetMapping("/libros-iva")
    public String librosIva(@RequestParam(required = false) Integer ejercicio,
                            @RequestParam(required = false) Integer trimestre,
                            Model m) {
        int ej = ejercicio != null ? ejercicio : LocalDate.now().getYear();
        int tr = trimestre != null ? trimestre : trimestreActual();
        LocalDate desde = LibroIvaService.inicioTrimestre(ej, tr);
        LocalDate hasta = LibroIvaService.finTrimestre(ej, tr);

        m.addAttribute("moduloActivo", "libros-iva");
        m.addAttribute("titulo", "Libros registro de IVA");
        m.addAttribute("ejercicio", ej);
        m.addAttribute("trimestre", tr);
        m.addAttribute("desde", desde);
        m.addAttribute("hasta", hasta);
        m.addAttribute("emitidas", libroIvaService.libroEmitidas(desde, hasta));
        m.addAttribute("recibidas", libroIvaService.libroRecibidas(desde, hasta));
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.inicio(),
                BreadcrumbBuilder.link("Fiscal", "#"),
                BreadcrumbBuilder.active("Libros de IVA")));
        return WebController.layout(m, "fiscal/libros-iva");
    }

    @GetMapping("/libros-iva/emitidas.csv")
    public ResponseEntity<byte[]> exportEmitidas(@RequestParam int ejercicio, @RequestParam int trimestre) {
        return Descargas.csv(libroIvaService.exportarCsv(libroIvaService.libroEmitidas(
                        LibroIvaService.inicioTrimestre(ejercicio, trimestre),
                        LibroIvaService.finTrimestre(ejercicio, trimestre))),
                "libro-emitidas-" + ejercicio + "T" + trimestre + ".csv");
    }

    @GetMapping("/libros-iva/recibidas.csv")
    public ResponseEntity<byte[]> exportRecibidas(@RequestParam int ejercicio, @RequestParam int trimestre) {
        return Descargas.csv(libroIvaService.exportarCsv(libroIvaService.libroRecibidas(
                        LibroIvaService.inicioTrimestre(ejercicio, trimestre),
                        LibroIvaService.finTrimestre(ejercicio, trimestre))),
                "libro-recibidas-" + ejercicio + "T" + trimestre + ".csv");
    }

    @GetMapping("/modelo303")
    public String modelo303(@RequestParam(required = false) Integer ejercicio,
                            @RequestParam(required = false) Integer trimestre,
                            Model m) {
        int ej = ejercicio != null ? ejercicio : LocalDate.now().getYear();
        int tr = trimestre != null ? trimestre : trimestreActual();
        m.addAttribute("moduloActivo", "modelo303");
        m.addAttribute("titulo", "Modelo 303 (borrador)");
        m.addAttribute("borrador", libroIvaService.modelo303(ej, tr));
        m.addAttribute("borrador111", libroIvaService.modelo111(ej, tr));
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.inicio(),
                BreadcrumbBuilder.link("Fiscal", "#"),
                BreadcrumbBuilder.active("Modelo 303")));
        return WebController.layout(m, "fiscal/modelo303");
    }

    private static int trimestreActual() {
        return (LocalDate.now().getMonthValue() - 1) / 3 + 1;
    }
}
