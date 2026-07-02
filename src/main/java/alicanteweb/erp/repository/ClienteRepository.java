package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByCif(String cif);
    Optional<Cliente> findByCifIgnoreCase(String cif);
    boolean existsByCif(String cif);
    Optional<Cliente> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    List<Cliente> findByNombreContainingIgnoreCase(String texto);
    Optional<Cliente> findFirstByNombreIgnoreCase(String nombre);
    List<Cliente> findByActivoTrue();

    @Query("SELECT c FROM Cliente c WHERE :q IS NULL OR " +
           "LOWER(c.nombre) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(COALESCE(c.cif,'')) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(COALESCE(c.email,'')) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<Cliente> buscarPaginado(@Param("q") String q, Pageable pageable);

    @Query("""
        SELECT c FROM Cliente c
        WHERE :q IS NULL
           OR LOWER(COALESCE(c.codigo, '')) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(COALESCE(c.nombre, '')) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(COALESCE(c.cif, '')) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(COALESCE(c.poblacion, '')) LIKE LOWER(CONCAT('%', :q, '%'))
        ORDER BY c.nombre
        """)
    List<Cliente> buscarParaApi(@Param("q") String q, Pageable pageable);
}
