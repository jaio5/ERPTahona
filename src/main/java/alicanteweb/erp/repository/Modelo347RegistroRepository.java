package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Modelo347Registro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface Modelo347RegistroRepository extends JpaRepository<Modelo347Registro, Long> {

    List<Modelo347Registro> findByEjercicioOrderByNifTerceroAsc(Integer ejercicio);

    List<Modelo347Registro> findByEjercicioAndGeneradoTrue(Integer ejercicio);

    List<Modelo347Registro> findByEjercicioAndNifTercero(Integer ejercicio, String nifTercero);

    @Query("SELECT SUM(r.importeOperaciones) FROM Modelo347Registro r WHERE r.ejercicio = :ejercicio AND r.claveOperacion = :clave")
    BigDecimal sumImporteByEjercicioAndClave(@Param("ejercicio") Integer ejercicio, @Param("clave") String clave);

    @Query("SELECT COUNT(r) FROM Modelo347Registro r WHERE r.ejercicio = :ejercicio AND r.importeOperaciones >= :limite")
    Long countByEjercicioAndImporteMayorQue(@Param("ejercicio") Integer ejercicio, @Param("limite") BigDecimal limite);

    void deleteByEjercicioAndGeneradoFalse(Integer ejercicio);
}

