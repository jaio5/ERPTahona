package alicanteweb.erp.repository;

import alicanteweb.erp.entities.AsientoContable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AsientoContableRepository extends JpaRepository<AsientoContable, Long> {

    List<AsientoContable> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);

    List<AsientoContable> findByConcepto(String concepto);

    List<AsientoContable> findByAsientoAperturaTrue();

    List<AsientoContable> findByAsientoCierreTrue();
}

