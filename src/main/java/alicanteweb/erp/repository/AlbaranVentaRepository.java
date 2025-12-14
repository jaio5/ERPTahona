package alicanteweb.erp.repository;

import alicanteweb.erp.entities.AlbaranVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbaranVentaRepository extends JpaRepository<AlbaranVenta, Long> {
    Optional<AlbaranVenta> findByNumero(String numero);
    boolean existsByNumero(String numero);

    @Query("SELECT DISTINCT a FROM AlbaranVenta a LEFT JOIN FETCH a.cliente LEFT JOIN FETCH a.almacen")
    List<AlbaranVenta> findAllWithRelations();

    List<AlbaranVenta> findByCliente_Id(Long clienteId);
}

