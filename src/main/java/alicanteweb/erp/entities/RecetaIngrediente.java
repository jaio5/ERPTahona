package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "receta_ingredientes")
public class RecetaIngrediente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receta_id", nullable = false)
    private Receta receta;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id", nullable = false)
    private Articulo articulo;

    @NotNull
    @Column(name = "cantidad", nullable = false, precision = 10, scale = 3)
    private BigDecimal cantidad;

    @Size(max = 20)
    @Column(name = "unidad", length = 20)
    private String unidad;

    @Column(name = "orden")
    private Integer orden;

    @Column(name = "notas", length = 500)
    private String notas;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecetaIngrediente)) return false;
        return Objects.equals(id, ((RecetaIngrediente) o).id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
