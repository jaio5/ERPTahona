package alicanteweb.erp.repository;

import alicanteweb.erp.entities.RecetaIngrediente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecetaIngredienteRepository extends JpaRepository<RecetaIngrediente, Long> {
    List<RecetaIngrediente> findByRecetaIdOrderByOrden(Long recetaId);
    void deleteByRecetaId(Long recetaId);
}
