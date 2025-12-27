package alicanteweb.erp.repository;

import alicanteweb.erp.entities.LibroFacturasRecibidas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LibroFacturasRecibidasRepository extends JpaRepository<LibroFacturasRecibidas, Long> {

    List<LibroFacturasRecibidas> findByEjercicioAndPeriodoOrderByNumeroRegistroAsc(Integer ejercicio, Integer periodo);

    List<LibroFacturasRecibidas> findByEjercicioOrderByNumeroRegistroAsc(Integer ejercicio);

    List<LibroFacturasRecibidas> findByFechaExpedicionBetweenOrderByFechaExpedicionAsc(LocalDate inicio, LocalDate fin);

    @Query("SELECT COALESCE(MAX(l.numeroRegistro), 0) FROM LibroFacturasRecibidas l WHERE l.ejercicio = :ejercicio AND l.periodo = :periodo")
    Integer findMaxNumeroRegistro(@Param("ejercicio") Integer ejercicio, @Param("periodo") Integer periodo);

    @Query("SELECT SUM(l.baseImponible) FROM LibroFacturasRecibidas l WHERE l.ejercicio = :ejercicio AND l.periodo = :periodo")
    java.math.BigDecimal sumBaseImponibleByEjercicioAndPeriodo(@Param("ejercicio") Integer ejercicio, @Param("periodo") Integer periodo);

    @Query("SELECT SUM(l.cuotaIvaSoportada) FROM LibroFacturasRecibidas l WHERE l.ejercicio = :ejercicio AND l.periodo = :periodo")
    java.math.BigDecimal sumCuotaIvaSoportadaByEjercicioAndPeriodo(@Param("ejercicio") Integer ejercicio, @Param("periodo") Integer periodo);

    @Query("SELECT SUM(l.cuotaIvaDeducible) FROM LibroFacturasRecibidas l WHERE l.ejercicio = :ejercicio AND l.periodo = :periodo")
    java.math.BigDecimal sumCuotaIvaDeducibleByEjercicioAndPeriodo(@Param("ejercicio") Integer ejercicio, @Param("periodo") Integer periodo);

    List<LibroFacturasRecibidas> findByIntracomunitariaTrue();

    List<LibroFacturasRecibidas> findByImportacionTrue();

    List<LibroFacturasRecibidas> findByInversionSujetoPasivoTrue();

    @Query("SELECT l.tipoIva, SUM(l.baseImponible), SUM(l.cuotaIvaSoportada), SUM(l.cuotaIvaDeducible) " +
           "FROM LibroFacturasRecibidas l WHERE l.ejercicio = :ejercicio AND l.periodo = :periodo GROUP BY l.tipoIva")
    List<Object[]> resumenPorTipoIva(@Param("ejercicio") Integer ejercicio, @Param("periodo") Integer periodo);
}


