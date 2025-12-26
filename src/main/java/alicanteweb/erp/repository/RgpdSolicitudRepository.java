package alicanteweb.erp.repository;

import alicanteweb.erp.entities.RgpdSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad RgpdSolicitud
 */
@Repository
public interface RgpdSolicitudRepository extends JpaRepository<RgpdSolicitud, Long> {

    /**
     * Buscar solicitudes por cliente
     */
    List<RgpdSolicitud> findByClienteIdOrderByFechaSolicitudDesc(Long clienteId);

    /**
     * Buscar solicitudes por email
     */
    List<RgpdSolicitud> findByEmailSolicitanteOrderByFechaSolicitudDesc(String email);

    /**
     * Buscar solicitudes por estado
     */
    List<RgpdSolicitud> findByEstadoOrderByFechaSolicitudDesc(String estado);

    /**
     * Buscar solicitudes por tipo de derecho
     */
    List<RgpdSolicitud> findByTipoDerechoOrderByFechaSolicitudDesc(String tipoDerecho);

    /**
     * Buscar solicitudes pendientes
     */
    List<RgpdSolicitud> findByEstadoInOrderByFechaSolicitudAsc(List<String> estados);

    /**
     * Buscar solicitudes por usuario responsable
     */
    List<RgpdSolicitud> findByUsuarioResponsableIdOrderByFechaSolicitudDesc(Long usuarioId);

    /**
     * Buscar solicitudes con fecha lÃ­mite prÃ³xima (dentro de los prÃ³ximos N dÃ­as)
     */
    @Query("SELECT s FROM RgpdSolicitud s WHERE s.fechaLimiteRespuesta <= :fecha AND s.estado IN ('PENDIENTE', 'EN_PROCESO') ORDER BY s.fechaLimiteRespuesta ASC")
    List<RgpdSolicitud> findProximasAVencer(@Param("fecha") LocalDateTime fecha);

    /**
     * Buscar solicitudes vencidas (pasada la fecha lÃ­mite y sin resolver)
     */
    @Query("SELECT s FROM RgpdSolicitud s WHERE s.fechaLimiteRespuesta < CURRENT_TIMESTAMP AND s.estado IN ('PENDIENTE', 'EN_PROCESO') ORDER BY s.fechaLimiteRespuesta ASC")
    List<RgpdSolicitud> findVencidas();

    /**
     * Buscar solicitudes por rango de fechas
     */
    List<RgpdSolicitud> findByFechaSolicitudBetweenOrderByFechaSolicitudDesc(
        LocalDateTime inicio, LocalDateTime fin);

    /**
     * Contar solicitudes por estado
     */
    long countByEstado(String estado);

    /**
     * Contar solicitudes pendientes
     */
    @Query("SELECT COUNT(s) FROM RgpdSolicitud s WHERE s.estado IN ('PENDIENTE', 'EN_PROCESO')")
    long countPendientes();

    /**
     * Contar solicitudes vencidas
     */
    @Query("SELECT COUNT(s) FROM RgpdSolicitud s WHERE s.fechaLimiteRespuesta < CURRENT_TIMESTAMP AND s.estado IN ('PENDIENTE', 'EN_PROCESO')")
    long countVencidas();

    /**
     * EstadÃ­sticas por tipo de derecho
     */
    @Query("SELECT s.tipoDerecho, COUNT(s) FROM RgpdSolicitud s GROUP BY s.tipoDerecho ORDER BY COUNT(s) DESC")
    List<Object[]> estadisticasPorTipoDerecho();

    /**
     * EstadÃ­sticas por estado
     */
    @Query("SELECT s.estado, COUNT(s) FROM RgpdSolicitud s GROUP BY s.estado ORDER BY COUNT(s) DESC")
    List<Object[]> estadisticasPorEstado();

    /**
     * Tiempo promedio de respuesta
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, s.fechaSolicitud, s.fechaRespuesta)) FROM RgpdSolicitud s WHERE s.fechaRespuesta IS NOT NULL")
    Double tiempoPromedioRespuestaHoras();
}


