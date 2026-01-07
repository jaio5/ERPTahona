package alicanteweb.erp.repository;

import alicanteweb.erp.entities.MovimientoCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {

    List<MovimientoCaja> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);

    List<MovimientoCaja> findByTipo(String tipo);

    List<MovimientoCaja> findByFechaOrderByFechaDesc(LocalDate fecha);

    @Query("SELECT COALESCE(SUM(m.importe), 0) FROM MovimientoCaja m WHERE m.tipo = 'INGRESO'")
    BigDecimal calcularTotalIngresos();

    @Query("SELECT COALESCE(SUM(m.importe), 0) FROM MovimientoCaja m WHERE m.tipo = 'GASTO'")
    BigDecimal calcularTotalGastos();

    @Query("SELECT COALESCE(SUM(CASE WHEN m.tipo = 'INGRESO' THEN m.importe ELSE -m.importe END), 0) FROM MovimientoCaja m")
    BigDecimal calcularSaldoCaja();
}

