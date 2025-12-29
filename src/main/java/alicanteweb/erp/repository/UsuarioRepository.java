package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Usuario
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByEmail(String email);

    @Query("SELECT u FROM Usuario u WHERE u.username = :login OR u.email = :login")
    Optional<Usuario> findByUsernameOrEmail(@Param("login") String login);

    List<Usuario> findByEnabledTrue();

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<Usuario> findByBloqueadoTrue();

    long countByEnabledTrue();

    @Query("SELECT u FROM Usuario u WHERE " +
           "LOWER(u.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Usuario> buscar(@Param("search") String search);
}


