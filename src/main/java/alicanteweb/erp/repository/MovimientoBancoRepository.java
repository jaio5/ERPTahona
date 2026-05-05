package alicanteweb.erp.repository;

import alicanteweb.erp.entities.MovimientoBanco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para gestión de movimientos bancarios
 */
@Repository
public interface MovimientoBancoRepository extends JpaRepository<MovimientoBanco, Long> {
    
    /**
     * Encontrar movimientos por estado de conciliación
     */
    List<MovimientoBanco> findByConciliado(boolean conciliado);
    
    /**
     * Encontrar movimientos por rango de fechas
     */
    List<MovimientoBanco> findByFechaBetween(LocalDate desde, LocalDate hasta);
    
    /**
     * Encontrar movimientos por concepto
     */
    List<MovimientoBanco> findByConceptoContainingIgnoreCase(String concepto);

    @Query("SELECT m FROM MovimientoBanco m LEFT JOIN FETCH m.banco ORDER BY m.fecha ASC, m.id ASC")
    List<MovimientoBanco> findAllWithBanco();

    List<MovimientoBanco> findByBancoIdOrderByFechaAscIdAsc(Long bancoId);
}

