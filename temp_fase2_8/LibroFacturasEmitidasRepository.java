package alicanteweb.erp.repository;
}
    List<Object[]> resumenPorTipoIva(@Param("ejercicio") Integer ejercicio, @Param("periodo") Integer periodo);
           "WHERE l.ejercicio = :ejercicio AND l.periodo = :periodo GROUP BY l.tipoIva")
    @Query("SELECT l.tipoIva, SUM(l.baseImponible), SUM(l.cuotaIva) FROM LibroFacturasEmitidas l " +

    List<LibroFacturasEmitidas> findByExportacionTrue();

    List<LibroFacturasEmitidas> findByIntracomunitariaTrue();

    java.math.BigDecimal sumCuotaIvaByEjercicioAndPeriodo(@Param("ejercicio") Integer ejercicio, @Param("periodo") Integer periodo);
    @Query("SELECT SUM(l.cuotaIva) FROM LibroFacturasEmitidas l WHERE l.ejercicio = :ejercicio AND l.periodo = :periodo")

    java.math.BigDecimal sumBaseImponibleByEjercicioAndPeriodo(@Param("ejercicio") Integer ejercicio, @Param("periodo") Integer periodo);
    @Query("SELECT SUM(l.baseImponible) FROM LibroFacturasEmitidas l WHERE l.ejercicio = :ejercicio AND l.periodo = :periodo")

    Integer findMaxNumeroRegistro(@Param("ejercicio") Integer ejercicio, @Param("periodo") Integer periodo);
    @Query("SELECT COALESCE(MAX(l.numeroRegistro), 0) FROM LibroFacturasEmitidas l WHERE l.ejercicio = :ejercicio AND l.periodo = :periodo")

    List<LibroFacturasEmitidas> findByFechaExpedicionBetweenOrderByFechaExpedicionAsc(LocalDate inicio, LocalDate fin);

    List<LibroFacturasEmitidas> findByEjercicioOrderByNumeroRegistroAsc(Integer ejercicio);

    List<LibroFacturasEmitidas> findByEjercicioAndPeriodoOrderByNumeroRegistroAsc(Integer ejercicio, Integer periodo);

public interface LibroFacturasEmitidasRepository extends JpaRepository<LibroFacturasEmitidas, Long> {
@Repository

import java.util.List;
import java.time.LocalDate;

import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import alicanteweb.erp.entities.LibroFacturasEmitidas;



