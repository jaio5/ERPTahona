package alicanteweb.erp.controller.rest;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/reparto")
public class RepartoRestController {

    private final VehiculoService vehiculoService;
    private final RutaRepartoService rutaService;
    private final HojaRutaService hojaRutaService;

    public RepartoRestController(VehiculoService vehiculoService,
                                  RutaRepartoService rutaService,
                                  HojaRutaService hojaRutaService) {
        this.vehiculoService = vehiculoService;
        this.rutaService = rutaService;
        this.hojaRutaService = hojaRutaService;
    }

    @GetMapping("/vehiculos")
    public List<Vehiculo> listVehiculos(@RequestParam(defaultValue = "true") boolean activo) {
        return vehiculoService.findByActivo(activo);
    }

    @GetMapping("/vehiculos/{id}")
    public ResponseEntity<Vehiculo> getVehiculo(@PathVariable Long id) {
        return vehiculoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/vehiculos")
    public Vehiculo createVehiculo(@RequestBody Vehiculo vehiculo) {
        return vehiculoService.save(vehiculo);
    }

    @GetMapping("/rutas")
    public List<RutaReparto> listRutas(@RequestParam(defaultValue = "true") boolean activo) {
        return rutaService.findByActivo(activo);
    }

    @GetMapping("/rutas/{id}")
    public ResponseEntity<RutaReparto> getRuta(@PathVariable Long id) {
        return rutaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/rutas/{id}/paradas")
    public ResponseEntity<List<RutaParada>> getParadas(@PathVariable Long id) {
        return ResponseEntity.ok(rutaService.getParadas(id));
    }

    @PostMapping("/rutas")
    public RutaReparto createRuta(@RequestBody RutaReparto ruta) {
        return rutaService.save(ruta);
    }

    @GetMapping("/hojas")
    public List<HojaRuta> listHojas(
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long rutaId) {
        if (fecha != null) return hojaRutaService.findByFecha(java.time.LocalDate.parse(fecha));
        if (estado != null) return hojaRutaService.findByEstado(estado);
        if (rutaId != null) return hojaRutaService.findByRutaId(rutaId);
        return hojaRutaService.findAll();
    }

    @GetMapping("/hojas/{id}")
    public ResponseEntity<HojaRuta> getHoja(@PathVariable Long id) {
        return hojaRutaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/hojas/{id}/entregas")
    public ResponseEntity<List<HojaRutaEntrega>> getEntregas(@PathVariable Long id) {
        return ResponseEntity.ok(hojaRutaService.getEntregas(id));
    }

    @PostMapping("/hojas/generar")
    public ResponseEntity<HojaRuta> generarHoja(@RequestParam Long rutaId,
                                                 @RequestParam String fecha) {
        return ResponseEntity.ok(hojaRutaService.generarDesdeRuta(rutaId, java.time.LocalDate.parse(fecha)));
    }

    @PostMapping("/hojas/{id}/iniciar")
    public ResponseEntity<HojaRuta> iniciarRuta(@PathVariable Long id) {
        return ResponseEntity.ok(hojaRutaService.iniciarRuta(id));
    }

    @PostMapping("/hojas/{id}/finalizar")
    public ResponseEntity<HojaRuta> finalizarRuta(@PathVariable Long id,
                                                    @RequestParam BigDecimal kmFin,
                                                    @RequestParam(required = false) String incidencias) {
        return ResponseEntity.ok(hojaRutaService.finalizarRuta(id, kmFin, incidencias));
    }

    @PostMapping("/entregas/{id}/confirmar")
    public ResponseEntity<HojaRutaEntrega> confirmarEntrega(@PathVariable Long id,
                                                             @RequestParam(required = false) String persona,
                                                             @RequestParam(required = false) String incidencia,
                                                             @RequestParam(required = false) BigDecimal importe,
                                                             @RequestParam(required = false) String medioCobro) {
        return ResponseEntity.ok(hojaRutaService.confirmarEntrega(id, persona, incidencia, importe, medioCobro));
    }

    @PostMapping("/entregas/{id}/incidencia")
    public ResponseEntity<HojaRutaEntrega> marcarIncidencia(@PathVariable Long id,
                                                             @RequestParam String incidencia) {
        return ResponseEntity.ok(hojaRutaService.marcarIncidencia(id, incidencia));
    }
}
