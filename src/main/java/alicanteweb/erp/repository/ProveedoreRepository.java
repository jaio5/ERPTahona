package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Proveedore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProveedoreRepository extends JpaRepository<Proveedore,Integer> {
}
