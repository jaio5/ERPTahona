package alicanteweb.erp.repository;

import alicanteweb.erp.entities.MovimientoBancario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovimientoBancarioRepository extends JpaRepository<MovimientoBancario, Long> {

    List<MovimientoBancario> findByBancoIdOrderByFechaDescIdDesc(Long bancoId);

    List<MovimientoBancario> findByBancoIdAndFechaBetweenOrderByFechaDescIdDesc(
            Long bancoId, LocalDate inicio, LocalDate fin);

    List<MovimientoBancario> findByConciliadoFalse();

    @Query("SELECT SUM(m.importe) FROM MovimientoBancario m WHERE m.banco.id = :bancoId AND m.tipo = :tipo")
    java.math.BigDecimal sumImporteByBancoAndTipo(@Param("bancoId") Long bancoId, @Param("tipo") String tipo);
}

