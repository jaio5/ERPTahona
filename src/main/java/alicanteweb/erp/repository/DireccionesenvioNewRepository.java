package alicanteweb.erp.repository;

import alicanteweb.erp.entities.DireccionesenvioNew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DireccionesenvioNewRepository extends JpaRepository<DireccionesenvioNew, Long> {
    List<DireccionesenvioNew> findByClienteId(Long clienteId);
    List<DireccionesenvioNew> findByPoblacionContainingIgnoreCase(String poblacion);
    DireccionesenvioNew findByCodigoDireccion(Integer codigoDireccion);
}

