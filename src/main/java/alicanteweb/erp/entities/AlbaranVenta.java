package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad de albarán de venta.
 * Nota: se usa @Getter/@Setter en lugar de @Data para evitar equals/hashCode
 * inadecuado sobre colecciones lazy en entidades JPA.
 */
@Getter
@Setter
@Entity
@Table(name = "albaranes_venta")
public class AlbaranVenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "numero", nullable = false, length = 50)
    private String numero;

    @Column(name = "fecha")
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "almacen_id")
    private Almacen almacen;

    @Lob
    @Column(name = "observaciones")
    private String observaciones;

    @ColumnDefault("0.00")
    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "albaran", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AlbaranVentaLinea> albaranVentaLineas = new LinkedHashSet<>();

    /** @deprecated Usar {@link #getAlbaranVentaLineas()} directamente. */
    @Deprecated(since = "1.0", forRemoval = true)
    public Set<AlbaranVentaLinea> getLineas() {
        return this.albaranVentaLineas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlbaranVenta)) return false;
        AlbaranVenta that = (AlbaranVenta) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    @Override
    public String toString() { return "AlbaranVenta{id=" + id + ", numero='" + numero + "'}"; }
}