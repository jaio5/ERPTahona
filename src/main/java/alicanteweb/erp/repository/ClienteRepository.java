package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByCif(String cif);
    boolean existsByCif(String cif);
    Optional<Cliente> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    List<Cliente> findByNombreContainingIgnoreCase(String texto);
}
