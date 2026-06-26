package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Recepcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecepcionRepository extends JpaRepository<Recepcion, Long> {
    List<Recepcion> findByEstado(String estado);
}
