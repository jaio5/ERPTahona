package alicanteweb.erp.repository;

import alicanteweb.erp.entities.DireccionesenvioNew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DireccionesenvioNewRepository extends JpaRepository<DireccionesenvioNew, Integer> {
}
