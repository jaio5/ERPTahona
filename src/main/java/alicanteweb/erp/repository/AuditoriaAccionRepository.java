package alicanteweb.erp.repository;

import alicanteweb.erp.entities.AuditoriaAccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad AuditoriaAccion
 */
@Repository
public interface AuditoriaAccionRepository extends JpaRepository<AuditoriaAccion, Long> {

    /**
     * Buscar auditorías por usuario
     */
    List<AuditoriaAccion> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    /**
     * Buscar auditorías por tipo de acción
     */
    List<AuditoriaAccion> findByTipoAccionOrderByFechaDesc(String tipoAccion);

    /**
     * Buscar auditorías por entidad
     */
    List<AuditoriaAccion> findByEntidadTipoAndEntidadIdOrderByFechaDesc(String entidadTipo, String entidadId);

    /**
     * Buscar auditorías por módulo
     */
    List<AuditoriaAccion> findByModuloOrderByFechaDesc(String modulo);

    /**
     * Buscar auditorías por rango de fechas
     */
    List<AuditoriaAccion> findByFechaBetweenOrderByFechaDesc(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Buscar auditorías por usuario y rango de fechas
     */
    List<AuditoriaAccion> findByUsuarioIdAndFechaBetweenOrderByFechaDesc(
        Long usuarioId, LocalDateTime inicio, LocalDateTime fin);

    /**
     * Buscar auditorías por resultado
     */
    List<AuditoriaAccion> findByResultadoOrderByFechaDesc(String resultado);

    /**
     * Buscar auditorías con errores
     */
    List<AuditoriaAccion> findByResultadoInOrderByFechaDesc(List<String> resultados);

    /**
     * Buscar auditorías recientes (últimas N horas)
     */
    @Query("SELECT a FROM AuditoriaAccion a WHERE a.fecha >= :fecha ORDER BY a.fecha DESC")
    List<AuditoriaAccion> findRecientes(@Param("fecha") LocalDateTime fecha);

    /**
     * Buscar auditorías de un módulo por usuario
     */
    List<AuditoriaAccion> findByUsuarioIdAndModuloOrderByFechaDesc(Long usuarioId, String modulo);

    /**
     * Contar acciones por usuario
     */
    long countByUsuarioId(Long usuarioId);

    /**
     * Contar acciones por tipo
     */
    long countByTipoAccion(String tipoAccion);

    /**
     * Contar acciones por módulo
     */
    long countByModulo(String modulo);

    /**
     * Estadísticas por módulo
     */
    @Query("SELECT a.modulo, COUNT(a) FROM AuditoriaAccion a GROUP BY a.modulo ORDER BY COUNT(a) DESC")
    List<Object[]> estadisticasPorModulo();

    /**
     * Estadísticas por usuario
     */
    @Query("SELECT u.username, COUNT(a) FROM AuditoriaAccion a JOIN a.usuario u GROUP BY u.username ORDER BY COUNT(a) DESC")
    List<Object[]> estadisticasPorUsuario();

    /**
     * Estadísticas por acción
     */
    @Query("SELECT a.tipoAccion, COUNT(a) FROM AuditoriaAccion a GROUP BY a.tipoAccion ORDER BY COUNT(a) DESC")
    List<Object[]> estadisticasPorAccion();

    /**
     * Estadísticas por resultado
     */
    @Query("SELECT a.resultado, COUNT(a) FROM AuditoriaAccion a GROUP BY a.resultado")
    List<Object[]> estadisticasPorResultado();

    /**
     * Buscar acciones de login fallidos
     */
    @Query("SELECT a FROM AuditoriaAccion a WHERE a.tipoAccion = 'LOGIN' AND a.resultado = 'ERROR' ORDER BY a.fecha DESC")
    List<AuditoriaAccion> findLoginsFallidos();

    /**
     * Buscar accesos denegados
     */
    @Query("SELECT a FROM AuditoriaAccion a WHERE a.resultado = 'DENEGADO' ORDER BY a.fecha DESC")
    List<AuditoriaAccion> findAccesosDenegados();

    /**
     * Buscar por IP
     */
    List<AuditoriaAccion> findByIpOrderByFechaDesc(String ip);

    /**
     * Actividad reciente por entidad (para mostrar historial)
     */
    @Query("SELECT a FROM AuditoriaAccion a WHERE a.entidadTipo = :tipo AND a.entidadId = :id ORDER BY a.fecha DESC")
    List<AuditoriaAccion> findHistorialEntidad(@Param("tipo") String tipo, @Param("id") String id);
}


