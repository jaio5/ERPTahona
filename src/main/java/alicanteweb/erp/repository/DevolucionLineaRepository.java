package alicanteweb.erp.repository;

import alicanteweb.erp.entities.DevolucionLinea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DevolucionLineaRepository extends JpaRepository<DevolucionLinea, Long> {
    List<DevolucionLinea> findByDevolucionId(Long devolucionId);
}
