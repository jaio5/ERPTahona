package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Línea de asiento contable (apunte contable).
 * Nota: se usa @Getter/@Setter en lugar de @Data para evitar equals/hashCode
 * inadecuado en entidades JPA con relaciones lazy.
 */
@Getter
@Setter
@Entity
@Table(name = "asientos_contables_lineas")
public class AsientoContableLinea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asiento_id", nullable = false)
    private AsientoContable asiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private PlanContable cuenta;

    @Column(name = "concepto", length = 500)
    private String concepto;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @NotNull
    @Column(name = "debe", nullable = false, precision = 12, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal debe;

    @NotNull
    @Column(name = "haber", nullable = false, precision = 12, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal haber;

    @Column(name = "orden")
    private Integer orden;

    @PrePersist
    protected void onCreate() {
        if (debe == null) debe = BigDecimal.ZERO;
        if (haber == null) haber = BigDecimal.ZERO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AsientoContableLinea)) return false;
        AsientoContableLinea that = (AsientoContableLinea) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    @Override
    public String toString() {
        return (cuenta != null ? cuenta.getCodigo() : "?") + " - " + concepto + " - D:" + debe + " H:" + haber;
    }
}

