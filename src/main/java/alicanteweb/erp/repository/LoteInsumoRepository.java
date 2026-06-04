package alicanteweb.erp.repository;

import alicanteweb.erp.entities.LoteInsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoteInsumoRepository extends JpaRepository<LoteInsumo, Long> {
    List<LoteInsumo> findByLoteProductoId(Long loteProductoId);
    List<LoteInsumo> findByLoteInsumoId(Long loteInsumoId);
}
