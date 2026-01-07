package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Caja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CajaRepository extends JpaRepository<Caja, Long> {
    Optional<Caja> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    List<Caja> findByActivaTrue();
    List<Caja> findByActivaFalse();
    Optional<Caja> findByNombre(String nombre);
}

