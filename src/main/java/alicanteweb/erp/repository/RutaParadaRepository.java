package alicanteweb.erp.repository;

import alicanteweb.erp.entities.RutaParada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RutaParadaRepository extends JpaRepository<RutaParada, Long> {
    List<RutaParada> findByRutaIdOrderByOrden(Long rutaId);
    List<RutaParada> findByClienteId(Long clienteId);
    void deleteByRutaId(Long rutaId);
}
