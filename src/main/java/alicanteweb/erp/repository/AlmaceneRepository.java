package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Almacene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlmaceneRepository extends JpaRepository<Almacene, Integer> {
}
