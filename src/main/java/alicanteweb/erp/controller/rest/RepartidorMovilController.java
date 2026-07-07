package alicanteweb.erp.controller.rest;

import alicanteweb.erp.entities.HojaRuta;
import alicanteweb.erp.entities.HojaRutaEntrega;
import alicanteweb.erp.config.SecurityConfig.ErpUserPrincipal;
import alicanteweb.erp.service.HojaRutaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * API específica para la app móvil del repartidor.
 * El repartidor ve su hoja de ruta del día, las paradas, y confirma cada entrega.
 */
@RestController
@RequestMapping("/api/movil")
@PreAuthorize("@permisos.puede('reparto', 'ver')")
public class RepartidorMovilController {

    private static final Logger log = LoggerFactory.getLogger(RepartidorMovilController.class);

    private final HojaRutaService hojaRutaService;

    public RepartidorMovilController(HojaRutaService hojaRutaService) {
        this.hojaRutaService = hojaRutaService;
    }

    /**
     * Obtiene la hoja de ruta del repartidor para hoy.
     * El repartidor se identifica con su nombre (conductor).
     */
    @GetMapping("/mi-ruta")
    public ResponseEntity<?> getMiRutaDelDia(@RequestParam(required = false) String conductor,
                                              Authentication auth) {
        if (auth == null) return forbidden();

        HojaRuta miHoja;
        if (isAdmin(auth) && conductor != null && !conductor.isBlank()) {
            miHoja = hojaRutaService.findByFecha(LocalDate.now()).stream()
                    .filter(h -> conductor.equalsIgnoreCase(h.getConductor()))
                    .findFirst()
                    .orElse(null);
        } else {
            ErpUserPrincipal principal = principal(auth);
            miHoja = hojaRutaService.findRutaDelUsuario(
                    LocalDate.now(), principal.id(), principal.username(), principal.displayName()
            ).orElse(null);
        }

        if (miHoja == null) {
            return ResponseEntity.ok(Map.of(
                "tieneRuta", false,
                "mensaje", "No tienes ruta asignada para hoy"
            ));
        }

        List<HojaRutaEntrega> entregas = hojaRutaService.getEntregasDetalle(miHoja.getId());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tieneRuta", true);
        body.put("hojaId", miHoja.getId());
        body.put("fecha", miHoja.getFecha().toString());
        body.put("estado", miHoja.getEstado());
        body.put("conductor", miHoja.getConductor());
        body.put("vehiculo", miHoja.getVehiculo() != null ? miHoja.getVehiculo().getMatricula() : "No asignado");
        body.put("totalParadas", entregas.size());
        body.put("pendientes", entregas.stream().filter(e -> !Boolean.TRUE.equals(e.getEntregado())).count());
        body.put("entregadas", entregas.stream().filter(e -> Boolean.TRUE.equals(e.getEntregado())).count());
        body.put("paradas", entregas.stream().map(this::toParada).toList());
        return ResponseEntity.ok(body);
    }

    /**
     * El repartidor confirma una entrega desde el móvil.
     */
    @PostMapping("/confirmar/{entregaId}")
    @PreAuthorize("@permisos.puede('reparto', 'editar')")
    public ResponseEntity<?> confirmarParada(@PathVariable Long entregaId,
                                              @RequestParam(required = false) String personaRecepcion,
                                              @RequestParam(required = false) String incidencia,
                                               @RequestParam(required = false) BigDecimal importeCobrado,
                                               @RequestParam(required = false) String medioCobro,
                                               @RequestParam(required = false) Double latitud,
                                               @RequestParam(required = false) Double longitud,
                                               Authentication auth) {
        if (!puedeGestionarEntrega(entregaId, auth)) return forbidden();
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
        } catch (RuntimeException e) {
            log.error("Error al confirmar entrega {}: {}", entregaId, e.getMessage(), e);
            return errBadRequest(e);
        }
    }

    /**
     * El repartidor marca una parada con incidencia (no entregado).
     */
    @PostMapping("/incidencia/{entregaId}")
    @PreAuthorize("@permisos.puede('reparto', 'editar')")
    public ResponseEntity<?> marcarIncidencia(@PathVariable Long entregaId,
                                               @RequestParam String motivo,
                                               @RequestParam(required = false) Double latitud,
                                               @RequestParam(required = false) Double longitud,
                                               Authentication auth) {
        if (!puedeGestionarEntrega(entregaId, auth)) return forbidden();
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
        } catch (RuntimeException e) {
            log.error("Error al registrar incidencia en entrega {}: {}", entregaId, e.getMessage(), e);
            return errBadRequest(e);
        }
    }

    /**
     * El repartidor inicia su ruta (marca hora de salida).
     */
    @PostMapping("/iniciar-ruta/{hojaId}")
    @PreAuthorize("@permisos.puede('reparto', 'editar')")
    public ResponseEntity<?> iniciarRuta(@PathVariable Long hojaId, Authentication auth) {
        if (!puedeGestionarHoja(hojaId, auth)) return forbidden();
        try {
            HojaRuta hoja = hojaRutaService.iniciarRuta(hojaId);
            return ResponseEntity.ok(Map.of(
                "ok", true,
                "estado", hoja.getEstado(),
                "horaSalida", hoja.getHoraSalida() != null ? hoja.getHoraSalida().toString() : null
            ));
        } catch (RuntimeException e) {
            log.error("Error al iniciar ruta {}: {}", hojaId, e.getMessage(), e);
            return errBadRequest(e);
        }
    }

    /**
     * El repartidor finaliza su ruta (marca hora de llegada, km finales).
     */
    @PostMapping("/finalizar-ruta/{hojaId}")
    @PreAuthorize("@permisos.puede('reparto', 'editar')")
    public ResponseEntity<?> finalizarRuta(@PathVariable Long hojaId,
                                            @RequestParam BigDecimal kmFin,
                                            @RequestParam(required = false) String incidencias,
                                            Authentication auth) {
        if (!puedeGestionarHoja(hojaId, auth)) return forbidden();
        try {
            HojaRuta hoja = hojaRutaService.finalizarRuta(hojaId, kmFin, incidencias);
            return ResponseEntity.ok(Map.of(
                "ok", true,
                "estado", hoja.getEstado(),
                "horaLlegada", hoja.getHoraLlegada() != null ? hoja.getHoraLlegada().toString() : null,
                "kmFin", hoja.getKmFin()
            ));
        } catch (RuntimeException e) {
            log.error("Error al finalizar ruta {}: {}", hojaId, e.getMessage(), e);
            return errBadRequest(e);
        }
    }

    private boolean puedeGestionarHoja(Long hojaId, Authentication auth) {
        if (isAdmin(auth)) return true;
        ErpUserPrincipal principal = principalOrNull(auth);
        return principal != null && hojaRutaService.puedeGestionarHoja(
                hojaId, principal.id(), principal.username(), principal.displayName());
    }

    private boolean puedeGestionarEntrega(Long entregaId, Authentication auth) {
        if (isAdmin(auth)) return true;
        ErpUserPrincipal principal = principalOrNull(auth);
        return principal != null && hojaRutaService.puedeGestionarEntrega(
                entregaId, principal.id(), principal.username(), principal.displayName());
    }

    private boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())
                        || "ROLE_ADMINISTRADOR".equals(a.getAuthority()));
    }

    private ErpUserPrincipal principal(Authentication auth) {
        ErpUserPrincipal principal = principalOrNull(auth);
        if (principal == null) throw new IllegalStateException("Principal ERP no disponible");
        return principal;
    }

    private ErpUserPrincipal principalOrNull(Authentication auth) {
        return auth != null && auth.getPrincipal() instanceof ErpUserPrincipal principal ? principal : null;
    }

    private ResponseEntity<Map<String, Object>> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "No tienes permiso para gestionar esta ruta"));
    }

    private ResponseEntity<Map<String, Object>> errBadRequest(RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of("ok", false, "error", e.getMessage()));
    }

    private Map<String, Object> toParada(HojaRutaEntrega entrega) {
        Map<String, Object> parada = new LinkedHashMap<>();
        parada.put("id", entrega.getId());
        parada.put("orden", entrega.getOrden());
        parada.put("cliente", entrega.getCliente().getNombre());
        parada.put("direccion", entrega.getCliente().getDireccion() != null
                ? entrega.getCliente().getDireccion() : "");
        parada.put("poblacion", entrega.getCliente().getPoblacion() != null
                ? entrega.getCliente().getPoblacion() : "");
        parada.put("entregado", entrega.getEntregado());
        parada.put("estado", entrega.getEstadoEntrega() != null ? entrega.getEstadoEntrega() : "PENDIENTE");
        parada.put("incidencia", entrega.getIncidencia());
        return parada;
    }
}
