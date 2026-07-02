package alicanteweb.erp.repository;

import alicanteweb.erp.entities.RecepcionLinea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecepcionLineaRepository extends JpaRepository<RecepcionLinea, Long> {

    List<RecepcionLinea> findByRecepcionId(Long recepcionId);

    @Query("SELECT l FROM RecepcionLinea l LEFT JOIN FETCH l.articulo WHERE l.recepcion.id = :recepcionId")
    List<RecepcionLinea> findByRecepcionIdWithArticulo(@Param("recepcionId") Long recepcionId);

    void deleteByRecepcionId(Long recepcionId);
}
