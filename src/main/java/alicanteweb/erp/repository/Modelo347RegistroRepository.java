package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Modelo347Registro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository para gestionar registros del Modelo 347
 */
@Repository
public interface Modelo347RegistroRepository extends JpaRepository<Modelo347Registro, Long> {

    /**
     * Busca registros por ejercicio fiscal
     */
    List<Modelo347Registro> findByEjercicio(Integer ejercicio);

    /**
     * Busca registros por NIF del tercero
     */
    List<Modelo347Registro> findByNifDeclarado(String nifDeclarado);

    /**
     * Busca registros por ejercicio y tipo de operación
     */
    List<Modelo347Registro> findByEjercicioAndTipoOperacion(Integer ejercicio, String tipoOperacion);
}

