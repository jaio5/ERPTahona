package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Formasdepago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormasdepagoRepository extends JpaRepository<Formasdepago, String> {
}

