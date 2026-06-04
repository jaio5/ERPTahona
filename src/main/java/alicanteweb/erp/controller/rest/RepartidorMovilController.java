package alicanteweb.erp.controller.rest;

import alicanteweb.erp.entities.HojaRuta;
import alicanteweb.erp.entities.HojaRutaEntrega;
import alicanteweb.erp.service.AutenticacionService;
import alicanteweb.erp.service.HojaRutaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * API específica para la app móvil del repartidor.
 * El repartidor ve su hoja de ruta del día, las paradas, y confirma cada entrega.
 */
@RestController
@RequestMapping("/api/movil")
public class RepartidorMovilController {

    private final HojaRutaService hojaRutaService;
    private final AutenticacionService autenticacionService;

    public RepartidorMovilController(HojaRutaService hojaRutaService,
                                      AutenticacionService autenticacionService) {
        this.hojaRutaService = hojaRutaService;
        this.autenticacionService = autenticacionService;
    }

    /**
     * Obtiene la hoja de ruta del repartidor para hoy.
     * El repartidor se identifica con su nombre (conductor).
     */
    @GetMapping("/mi-ruta")
    public ResponseEntity<?> getMiRutaDelDia(@RequestParam String conductor) {
        List<HojaRuta> hojas = hojaRutaService.findByFecha(LocalDate.now());
        HojaRuta miHoja = hojas.stream()
                .filter(h -> conductor.equalsIgnoreCase(h.getConductor()))
                .findFirst()
                .orElse(null);

        if (miHoja == null) {
            return ResponseEntity.ok(Map.of(
                "tieneRuta", false,
                "mensaje", "No tienes ruta asignada para hoy"
            ));
        }

        List<HojaRutaEntrega> entregas = hojaRutaService.getEntregas(miHoja.getId());

        return ResponseEntity.ok(Map.of(
            "tieneRuta", true,
            "hojaId", miHoja.getId(),
            "fecha", miHoja.getFecha().toString(),
            "estado", miHoja.getEstado(),
            "conductor", miHoja.getConductor(),
            "vehiculo", miHoja.getVehiculo() != null ? miHoja.getVehiculo().getMatricula() : "No asignado",
            "totalParadas", entregas.size(),
            "pendientes", entregas.stream().filter(e -> !Boolean.TRUE.equals(e.getEntregado())).count(),
            "entregadas", entregas.stream().filter(e -> Boolean.TRUE.equals(e.getEntregado())).count(),
            "paradas", entregas.stream().map(e -> Map.of(
                "id", e.getId(),
                "orden", e.getOrden(),
                "cliente", e.getCliente().getNombre(),
                "direccion", e.getCliente().getDireccion() != null ? e.getCliente().getDireccion() : "",
                "poblacion", e.getCliente().getPoblacion() != null ? e.getCliente().getPoblacion() : "",
                "entregado", e.getEntregado(),
                "estado", e.getEstadoEntrega() != null ? e.getEstadoEntrega() : "PENDIENTE",
                "incidencia", e.getIncidencia()
            )).toList()
        ));
    }

    /**
     * El repartidor confirma una entrega desde el móvil.
     */
    @PostMapping("/confirmar/{entregaId}")
    public ResponseEntity<?> confirmarParada(@PathVariable Long entregaId,
                                              @RequestParam(required = false) String personaRecepcion,
                                              @RequestParam(required = false) String incidencia,
                                              @RequestParam(required = false) BigDecimal importeCobrado,
                                              @RequestParam(required = false) String medioCobro,
                                              @RequestParam(required = false) Double latitud,
                                              @RequestParam(required = false) Double longitud) {
        try {
            HojaRutaEntrega entrega = hojaRutaService.confirmarEntrega(
                    entregaId, personaRecepcion, incidencia, importeCobrado, medioCobro);
            if (latitud != null) entrega.setLatitud(latitud);
            if (longitud != null) entrega.setLongitud(longitud);
            hojaRutaService.saveEntrega(entrega);

            return ResponseEntity.ok(Map.of(
                "ok", true,
                "mensaje", "Entrega confirmada",
                "entregaId", entrega.getId(),
                "estado", entrega.getEstadoEntrega()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "ok", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * El repartidor marca una parada con incidencia (no entregado).
     */
    @PostMapping("/incidencia/{entregaId}")
    public ResponseEntity<?> marcarIncidencia(@PathVariable Long entregaId,
                                               @RequestParam String motivo,
                                               @RequestParam(required = false) Double latitud,
                                               @RequestParam(required = false) Double longitud) {
        try {
            HojaRutaEntrega entrega = hojaRutaService.marcarIncidencia(entregaId, motivo);
            if (latitud != null) entrega.setLatitud(latitud);
            if (longitud != null) entrega.setLongitud(longitud);
            hojaRutaService.saveEntrega(entrega);

            return ResponseEntity.ok(Map.of(
                "ok", true,
                "mensaje", "Incidencia registrada",
                "entregaId", entrega.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "ok", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * El repartidor inicia su ruta (marca hora de salida).
     */
    @PostMapping("/iniciar-ruta/{hojaId}")
    public ResponseEntity<?> iniciarRuta(@PathVariable Long hojaId) {
        try {
            HojaRuta hoja = hojaRutaService.iniciarRuta(hojaId);
            return ResponseEntity.ok(Map.of(
                "ok", true,
                "estado", hoja.getEstado(),
                "horaSalida", hoja.getHoraSalida() != null ? hoja.getHoraSalida().toString() : null
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", e.getMessage()));
        }
    }

    /**
     * El repartidor finaliza su ruta (marca hora de llegada, km finales).
     */
    @PostMapping("/finalizar-ruta/{hojaId}")
    public ResponseEntity<?> finalizarRuta(@PathVariable Long hojaId,
                                            @RequestParam BigDecimal kmFin,
                                            @RequestParam(required = false) String incidencias) {
        try {
            HojaRuta hoja = hojaRutaService.finalizarRuta(hojaId, kmFin, incidencias);
            return ResponseEntity.ok(Map.of(
                "ok", true,
                "estado", hoja.getEstado(),
                "horaLlegada", hoja.getHoraLlegada() != null ? hoja.getHoraLlegada().toString() : null,
                "kmFin", hoja.getKmFin()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", e.getMessage()));
        }
    }
}
