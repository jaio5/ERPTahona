package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.Vehiculo;
import alicanteweb.erp.service.VehiculoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@PreAuthorize("@permisos.puede('reparto', 'ver')")
@RequestMapping("/web/vehiculos")
public class VehiculoWebController {

    private static final Logger log = LoggerFactory.getLogger(VehiculoWebController.class);

    private final VehiculoService vehiculoService;

    public VehiculoWebController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @GetMapping
    public String lista(Model model, @RequestParam(required = false) String q) {
        List<Vehiculo> vehiculos = (q != null && !q.isBlank())
                ? vehiculoService.buscar(q) : vehiculoService.findAll();
        model.addAttribute("moduloActivo", "vehiculos");
        model.addAttribute("titulo", "Vehículos");
        model.addAttribute("vehiculos", vehiculos);
        model.addAttribute("q", q);
        return WebController.layout(model, "vehiculos/lista");
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("moduloActivo", "vehiculos");
        model.addAttribute("titulo", "Nuevo vehículo");
        model.addAttribute("vehiculo", new Vehiculo());
        return WebController.layout(model, "vehiculos/formulario");
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return vehiculoService.findById(id).map(v -> {
            model.addAttribute("moduloActivo", "vehiculos");
            model.addAttribute("titulo", "Editar vehículo " + v.getMatricula());
            model.addAttribute("vehiculo", v);
            return WebController.layout(model, "vehiculos/formulario");
        }).orElseGet(() -> { ra.addFlashAttribute("error", "Registro no encontrado"); return "redirect:/web/vehiculos"; });
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return vehiculoService.findById(id).map(v -> {
            model.addAttribute("moduloActivo", "vehiculos");
            model.addAttribute("titulo", "Vehículo " + v.getMatricula());
            model.addAttribute("vehiculo", v);
            model.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                    BreadcrumbBuilder.link("Inicio", "/web/dashboard"),
                    BreadcrumbBuilder.link("Vehículos", "/web/vehiculos"),
                    BreadcrumbBuilder.active(v.getMatricula())));
            return WebController.layout(model, "vehiculos/ver");
        }).orElseGet(() -> { ra.addFlashAttribute("error", "Registro no encontrado"); return "redirect:/web/vehiculos"; });
    }

    @PostMapping
    public String guardar(@RequestParam(required = false) Long id,
                          @RequestParam String matricula,
                          @RequestParam(required = false) String marca,
                          @RequestParam(required = false) String modelo,
                          @RequestParam(required = false) String tipo,
                          @RequestParam(required = false) BigDecimal capacidad,
                          RedirectAttributes ra) {
        try {
            Vehiculo vehiculo = id != null ? vehiculoService.findById(id).orElseThrow(() -> new IllegalArgumentException("ID de vehículo no válido: " + id)) : new Vehiculo();
            vehiculo.setMatricula(matricula);
            vehiculo.setMarca(marca);
            vehiculo.setModelo(modelo);
            vehiculo.setTipo(tipo);
            vehiculo.setCapacidadKg(capacidad);
            vehiculoService.save(vehiculo);
            ra.addFlashAttribute("exito", "Vehículo guardado correctamente");
        } catch (RuntimeException e) {
            log.error("Error al guardar vehículo: {}", e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/vehiculos";
    }
}
