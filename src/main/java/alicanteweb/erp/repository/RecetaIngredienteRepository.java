package alicanteweb.erp.repository;

import alicanteweb.erp.entities.RecetaIngrediente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecetaIngredienteRepository extends JpaRepository<RecetaIngrediente, Long> {
    List<RecetaIngrediente> findByRecetaIdOrderByOrden(Long recetaId);
    void deleteByRecetaId(Long recetaId);

    @Query("SELECT i FROM RecetaIngrediente i JOIN FETCH i.articulo WHERE i.receta.id = :id ORDER BY i.orden")
    List<RecetaIngrediente> findDetailByRecetaId(Long id);
}
