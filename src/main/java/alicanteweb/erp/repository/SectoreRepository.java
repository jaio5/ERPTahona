package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Sectore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SectoreRepository extends JpaRepository<Sectore, String> {
}

