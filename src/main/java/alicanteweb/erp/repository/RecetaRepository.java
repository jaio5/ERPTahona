package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Receta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

@Repository
public interface RecetaRepository extends JpaRepository<Receta, Long> {
    Optional<Receta> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);

    // Carga articuloResultante para mapear a DTO sin sesión abierta (open-in-view desactivado).
    @EntityGraph(attributePaths = {"articuloResultante"})
    List<Receta> findByActivo(Boolean activo);
    List<Receta> findByNombreContainingIgnoreCase(String texto);
    List<Receta> findByArticuloResultante_IdAndActivoTrue(Long articuloId);

    @Query("SELECT r FROM Receta r LEFT JOIN FETCH r.articuloResultante WHERE r.id = :id")
    Optional<Receta> findDetailById(Long id);

    @Query("""
            SELECT DISTINCT r FROM Receta r
            JOIN FETCH r.articuloResultante ar
            LEFT JOIN FETCH r.ingredientes i
            LEFT JOIN FETCH i.articulo
            WHERE r.activo = true AND ar.id IN :articuloIds
            """)
    List<Receta> findActivasConIngredientesByArticuloIds(Collection<Long> articuloIds);
}
