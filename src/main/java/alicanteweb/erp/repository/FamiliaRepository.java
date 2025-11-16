package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Familia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FamiliaRepository extends JpaRepository<Familia, String> {
}

