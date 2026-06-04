package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "recetas")
public class Receta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Size(max = 255)
    @NotNull
    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "descripcion", length = 2000)
    private String descripcion;

    @Column(name = "tiempo_preparacion")
    private Integer tiempoPreparacion;

    @Column(name = "tiempo_horneado")
    private Integer tiempoHorneado;

    @Column(name = "temperatura_horneado")
    private Integer temperaturaHorneado;

    @Column(name = "rendimiento_cantidad", precision = 10, scale = 2)
    private BigDecimal rendimientoCantidad;

    @Size(max = 20)
    @Column(name = "unidad_rendimiento", length = 20)
    private String unidadRendimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_resultante_id")
    private Articulo articuloResultante;

    @Column(name = "activo")
    private Boolean activo = true;

    @Size(max = 500)
    @Column(name = "alergenos", length = 500)
    private String alergenos;

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RecetaIngrediente> ingredientes = new LinkedHashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Receta)) return false;
        return Objects.equals(id, ((Receta) o).id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    @Override
    public String toString() { return "Receta{id=" + id + ", codigo='" + codigo + "', nombre='" + nombre + "'}"; }
}
