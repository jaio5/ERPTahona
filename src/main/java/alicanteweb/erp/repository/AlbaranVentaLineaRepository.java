package alicanteweb.erp.repository;

import alicanteweb.erp.entities.AlbaranVentaLinea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbaranVentaLineaRepository extends JpaRepository<AlbaranVentaLinea, Long> {

    @Query("SELECT l FROM AlbaranVentaLinea l LEFT JOIN FETCH l.articulo WHERE l.albaran.id = :albaranId")
    List<AlbaranVentaLinea> findByAlbaranIdWithArticulo(Long albaranId);
}

