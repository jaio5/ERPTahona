package alicanteweb.erp.repository;

import alicanteweb.erp.entities.RgpdAccesoDatos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad RgpdAccesoDatos
 */
@Repository
public interface RgpdAccesoDatosRepository extends JpaRepository<RgpdAccesoDatos, Long> {

    /**
     * Buscar accesos por cliente
     */
    List<RgpdAccesoDatos> findByClienteIdOrderByFechaAccesoDesc(Long clienteId);

    /**
     * Buscar accesos por usuario
     */
    List<RgpdAccesoDatos> findByUsuarioIdOrderByFechaAccesoDesc(Long usuarioId);

    /**
     * Buscar accesos por tipo
     */
    List<RgpdAccesoDatos> findByTipoAccesoOrderByFechaAccesoDesc(String tipoAcceso);

    /**
     * Buscar accesos por mÃ³dulo
     */
    List<RgpdAccesoDatos> findByModuloOrderByFechaAccesoDesc(String modulo);

    /**
     * Buscar accesos por rango de fechas
     */
    List<RgpdAccesoDatos> findByFechaAccesoBetweenOrderByFechaAccesoDesc(
        LocalDateTime inicio, LocalDateTime fin);

    /**
     * Buscar accesos de un cliente por rango de fechas
     */
    List<RgpdAccesoDatos> findByClienteIdAndFechaAccesoBetweenOrderByFechaAccesoDesc(
        Long clienteId, LocalDateTime inicio, LocalDateTime fin);

    /**
     * Buscar accesos de un usuario por rango de fechas
     */
    List<RgpdAccesoDatos> findByUsuarioIdAndFechaAccesoBetweenOrderByFechaAccesoDesc(
        Long usuarioId, LocalDateTime inicio, LocalDateTime fin);

    /**
     * Buscar accesos recientes (Ãºltimas 24 horas)
     */
    @Query("SELECT a FROM RgpdAccesoDatos a WHERE a.fechaAcceso >= :fecha ORDER BY a.fechaAcceso DESC")
    List<RgpdAccesoDatos> findAccesosRecientes(@Param("fecha") LocalDateTime fecha);

    /**
     * Contar accesos por cliente
     */
    long countByClienteId(Long clienteId);

    /**
     * Contar accesos por usuario
     */
    long countByUsuarioId(Long usuarioId);

    /**
     * Contar accesos por tipo
     */
    long countByTipoAcceso(String tipoAcceso);

    /**
     * EstadÃ­sticas de accesos por mÃ³dulo
     */
    @Query("SELECT a.modulo, COUNT(a) FROM RgpdAccesoDatos a GROUP BY a.modulo ORDER BY COUNT(a) DESC")
    List<Object[]> estadisticasPorModulo();

    /**
     * EstadÃ­sticas de accesos por usuario
     */
    @Query("SELECT u.username, COUNT(a) FROM RgpdAccesoDatos a JOIN a.usuario u GROUP BY u.username ORDER BY COUNT(a) DESC")
    List<Object[]> estadisticasPorUsuario();
}


