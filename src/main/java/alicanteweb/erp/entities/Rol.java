package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Entidad Rol para sistema de autorización
 */
@Data
@Entity
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Nombre del rol
     */
    @NotNull
    @Size(max = 50)
    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre; // ADMINISTRADOR, GERENTE, VENDEDOR, ALMACEN, CONTABLE, etc.

    /**
     * Descripción del rol
     */
    @Size(max = 255)
    @Column(name = "descripcion")
    private String descripcion;

    /**
     * Permisos del rol (JSON)
     * Estructura: {
     *   "clientes": {"ver": true, "crear": true, "editar": true, "eliminar": false},
     *   "facturas": {"ver": true, "crear": true, "editar": false, "eliminar": false},
     *   "articulos": {"ver": true, "crear": false, "editar": false, "eliminar": false},
     *   ...
     * }
     */
    @Column(name = "permisos", columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Map<String, Boolean>> permisos;

    /**
     * Rol activo
     */
    @NotNull
    @ColumnDefault("true")
    @Column(name = "activo", nullable = false)
    private Boolean activo;

    /**
     * Rol del sistema (no se puede eliminar)
     */
    @NotNull
    @ColumnDefault("false")
    @Column(name = "es_sistema", nullable = false)
    private Boolean esSistema;

    /**
     * Usuarios con este rol
     * TODO: La base de datos actual no soporta esta relación - el rol está en la columna 'role' de 'users'
     */
    // @OneToMany(mappedBy = "rol")
    // private Set<Usuario> usuarios = new LinkedHashSet<>();

    @PrePersist
    protected void onCreate() {
        if (activo == null) {
            activo = true;
        }
        if (esSistema == null) {
            esSistema = false;
        }
    }
}


