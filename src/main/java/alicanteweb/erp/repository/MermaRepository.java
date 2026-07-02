package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Merma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MermaRepository extends JpaRepository<Merma, Long> {
    List<Merma> findByFechaBetween(LocalDate inicio, LocalDate fin);
    List<Merma> findByArticuloId(Long articuloId);

    @Query("SELECT m FROM Merma m LEFT JOIN FETCH m.articulo WHERE m.tipo = :tipo ORDER BY m.fecha DESC")
    List<Merma> findByTipo(String tipo);

    @Query("SELECT m FROM Merma m LEFT JOIN FETCH m.articulo ORDER BY m.fecha DESC")
    List<Merma> findAllConArticulo();

    @Query("SELECT m FROM Merma m JOIN FETCH m.articulo WHERE m.id = :id")
    Optional<Merma> findDetailById(Long id);
}
