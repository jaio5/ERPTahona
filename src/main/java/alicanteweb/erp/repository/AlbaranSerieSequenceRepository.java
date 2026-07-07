package alicanteweb.erp.repository;

import alicanteweb.erp.entities.AlbaranSerieSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AlbaranSerieSequenceRepository extends JpaRepository<AlbaranSerieSequence, Long> {

    /**
     * Reserva un número de forma atómica en MySQL, incluida la primera reserva
     * de un ejercicio. LAST_INSERT_ID se mantiene por conexión y permite
     * recuperar exactamente el valor reservado por esta transacción.
     */
    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO albaran_series (serie, ejercicio, ultimo_numero)
        VALUES (:serie, :ejercicio, 1)
        ON DUPLICATE KEY UPDATE ultimo_numero = LAST_INSERT_ID(ultimo_numero + 1)
        """, nativeQuery = true)
    int reservarSiguiente(@Param("serie") String serie, @Param("ejercicio") int ejercicio);

    @Query(value = "SELECT LAST_INSERT_ID()", nativeQuery = true)
    long obtenerNumeroReservado();
}
