package alicanteweb.erp.repository;

import alicanteweb.erp.entities.MovimientoStock;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository para gestionar movimientos de stock
 */
@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {

    /**
     * Busca movimientos por artículo
     */
    List<MovimientoStock> findByArticuloId(Long articuloId);

    /**
     * Busca movimientos por almacén origen
     */
    List<MovimientoStock> findByAlmacenOrigenId(Long almacenOrigenId);

    /**
     * Busca movimientos por almacén destino
     */
    List<MovimientoStock> findByAlmacenDestinoId(Long almacenDestinoId);

    /**
     * Busca movimientos por tipo
     */
    List<MovimientoStock> findByTipo(String tipo);

    /**
     * Busca movimientos por artículo y tipo
     */
    List<MovimientoStock> findByArticuloIdAndTipo(Long articuloId, String tipo);

    /**
     * Busca movimientos entre fechas con artículo cargado
     */
    @EntityGraph(attributePaths = {"articulo"})
    List<MovimientoStock> findByFechaBetween(LocalDate desde, LocalDate hasta);
}

