package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    Optional<Proveedor> findByCif(String cif);

    boolean existsByCif(String cif);

    List<Proveedor> findByNombreContainingIgnoreCase(String texto);
}
