package alicanteweb.erp.repository;

import alicanteweb.erp.entities.LoteInsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoteInsumoRepository extends JpaRepository<LoteInsumo, Long> {
    List<LoteInsumo> findByLoteProductoId(Long loteProductoId);
    List<LoteInsumo> findByLoteInsumoId(Long loteInsumoId);

    @Query("SELECT li FROM LoteInsumo li JOIN FETCH li.loteInsumo i LEFT JOIN FETCH i.articulo JOIN FETCH li.loteProducto p LEFT JOIN FETCH p.articulo WHERE p.id = :id")
    List<LoteInsumo> findDetailByLoteProductoId(Long id);

    @Query("SELECT li FROM LoteInsumo li JOIN FETCH li.loteInsumo i LEFT JOIN FETCH i.articulo JOIN FETCH li.loteProducto p LEFT JOIN FETCH p.articulo WHERE i.id = :id")
    List<LoteInsumo> findDetailByLoteInsumoId(Long id);
}
