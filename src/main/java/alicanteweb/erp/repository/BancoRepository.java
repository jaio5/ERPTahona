package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Banco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BancoRepository extends JpaRepository<Banco, Long> {

    List<Banco> findByActivoTrue();

    Optional<Banco> findByPrincipalTrue();

    Optional<Banco> findByIban(String iban);

    List<Banco> findByNombreContainingIgnoreCase(String nombre);
}

