package alicanteweb.erp.service;

import alicanteweb.erp.entities.Caja;
import alicanteweb.erp.entities.Ticket;
import alicanteweb.erp.entities.Usuario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio del TPV (Punto de Venta)
 * FASE 7: TPV
 */
@Service
@Slf4j
public class TPVService {

    private final AuditoriaService auditoriaService;

    public TPVService(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    /**
     * Abre una caja/turno
     */
    @Transactional
    public Caja abrirCaja(String nombreCaja, Usuario usuario, BigDecimal saldoInicial) {
        log.info("Abriendo caja: {} por usuario: {}", nombreCaja, usuario.getUsername());

        Caja caja = new Caja();
        caja.setNombre(nombreCaja);
        caja.setUsuarioApertura(usuario);
        caja.setFechaApertura(LocalDateTime.now());
        caja.setSaldoInicial(saldoInicial);
        caja.setEstado("ABIERTA");
        caja.setTotalVentas(BigDecimal.ZERO);
        caja.setTotalEfectivo(BigDecimal.ZERO);
        caja.setTotalTarjeta(BigDecimal.ZERO);
        caja.setNumeroTickets(0);

        // cajaRepository.save(caja);

        auditoriaService.registrarAccion(usuario, "APERTURA_CAJA", "Caja",
                nombreCaja, "Apertura con saldo inicial: " + saldoInicial);

        return caja;
    }

    /**
     * Cierra una caja/turno
     */
    @Transactional
    public void cerrarCaja(Caja caja, Usuario usuario) {
        log.info("Cerrando caja: {}", caja.getNombre());

        caja.setFechaCierre(LocalDateTime.now());
        caja.setEstado("CERRADA");

        // Calcular saldo final
        BigDecimal saldoFinal = caja.getSaldoInicial()
                .add(caja.getTotalEfectivo());
        caja.setSaldoFinal(saldoFinal);

        // cajaRepository.save(caja);

        auditoriaService.registrarAccion(usuario, "CIERRE_CAJA", "Caja",
                caja.getId().toString(),
                "Cierre - Ventas: " + caja.getTotalVentas() + " - Saldo final: " + saldoFinal);
    }

    /**
     * Registra una venta en TPV
     */
    @Transactional
    public Ticket registrarVenta(Caja caja, BigDecimal total, String metodoPago, Usuario usuario) {
        log.info("Registrando venta TPV: {} - {}", total, metodoPago);

        // Generar número de ticket
        int siguienteNumero = caja.getNumeroTickets() + 1;
        String numeroTicket = String.format("%s-%04d",
                caja.getNombre(), siguienteNumero);

        Ticket ticket = new Ticket();
        ticket.setCaja(caja);
        ticket.setNumero(numeroTicket);
        ticket.setFecha(LocalDateTime.now());
        ticket.setTotal(total);
        ticket.setMetodoPago(metodoPago);
        ticket.setUsuario(usuario);

        // Actualizar totales de caja
        caja.setTotalVentas(caja.getTotalVentas().add(total));
        caja.setNumeroTickets(siguienteNumero);

        if ("EFECTIVO".equals(metodoPago)) {
            caja.setTotalEfectivo(caja.getTotalEfectivo().add(total));
        } else if ("TARJETA".equals(metodoPago)) {
            caja.setTotalTarjeta(caja.getTotalTarjeta().add(total));
        }

        // ticketRepository.save(ticket);
        // cajaRepository.save(caja);

        return ticket;
    }

    /**
     * Genera reporte de caja
     */
    public Map<String, Object> generarReporteCaja(Caja caja) {
        Map<String, Object> reporte = new HashMap<>();

        reporte.put("caja", caja.getNombre());
        reporte.put("usuario", caja.getUsuarioApertura().getUsername());
        reporte.put("fecha_apertura", caja.getFechaApertura());
        reporte.put("fecha_cierre", caja.getFechaCierre());
        reporte.put("saldo_inicial", caja.getSaldoInicial());
        reporte.put("total_ventas", caja.getTotalVentas());
        reporte.put("total_efectivo", caja.getTotalEfectivo());
        reporte.put("total_tarjeta", caja.getTotalTarjeta());
        reporte.put("numero_tickets", caja.getNumeroTickets());
        reporte.put("saldo_final", caja.getSaldoFinal());
        reporte.put("estado", caja.getEstado());

        // Ticket medio
        if (caja.getNumeroTickets() > 0) {
            BigDecimal ticketMedio = caja.getTotalVentas()
                    .divide(new BigDecimal(caja.getNumeroTickets()), 2, java.math.RoundingMode.HALF_UP);
            reporte.put("ticket_medio", ticketMedio);
        }

        return reporte;
    }
}

