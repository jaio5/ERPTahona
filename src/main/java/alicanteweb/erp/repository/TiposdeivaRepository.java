package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Tiposdeiva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TiposdeivaRepository extends JpaRepository<Tiposdeiva, Integer> {
}

