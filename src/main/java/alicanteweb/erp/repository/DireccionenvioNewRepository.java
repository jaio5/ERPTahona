package alicanteweb.erp.repository;

import alicanteweb.erp.entities.DireccionenvioNew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DireccionenvioNewRepository extends JpaRepository<DireccionenvioNew, Long> {
    List<DireccionenvioNew> findByClienteId(Long clienteId);
    List<DireccionenvioNew> findByPoblacionContainingIgnoreCase(String poblacion);
    DireccionenvioNew findByCodigoDireccion(Integer codigoDireccion);
}

