package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Almacene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlmaceneRepository extends JpaRepository<Almacene, Long> {
    Optional<Almacene> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}

