package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad de almacén.
 * Nota: se usa @Getter/@Setter en lugar de @Data para evitar equals/hashCode
 * inadecuado sobre colecciones lazy en entidades JPA.
 */
@Getter
@Setter
@Entity
@Table(name = "almacenes")
public class Almacen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Size(max = 255)
    @Column(name = "nombre")
    private String nombre;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "capacidad", precision = 10, scale = 2)
    private BigDecimal capacidad;

    @Column(name = "disponible", precision = 10, scale = 2)
    private BigDecimal disponible;

    @Column(name = "localidad", length = 255)
    private String localidad;

    @Column(name = "responsable", length = 255)
    private String responsable;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Almacen that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    @Override
    public String toString() { return "Almacen{id=" + id + ", codigo='" + codigo + "', nombre='" + nombre + "'}"; }
}