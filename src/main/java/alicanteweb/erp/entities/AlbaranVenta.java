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

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Size(max = 50)
    @NotNull
    @Column(name = "numero", nullable = false, length = 50)
    private String numero;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Size(max = 20)
    @ColumnDefault("'PENDIENTE'")
    @Column(name = "estado", length = 20)
    private String estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "almacen_id")
    private Almacen almacen;

    @Lob
    @Column(name = "observaciones")
    private String observaciones;

    /** Nº de lote de trazabilidad mostrado en el albarán ("ARTICULOS CON Nº LOTE"). */
    @Size(max = 50)
    @Column(name = "numero_lote", length = 50)
    private String numeroLote;

    @ColumnDefault("0.00")
    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;

    /** Descuento global del documento (sobre el conjunto): tipo PORCENTAJE|IMPORTE y valor. */
    @Size(max = 10)
    @Column(name = "descuento_global_tipo", length = 10)
    private String descuentoGlobalTipo;

    @ColumnDefault("0.00")
    @Column(name = "descuento_global_valor", precision = 10, scale = 2)
    private BigDecimal descuentoGlobalValor;

    @OneToMany(mappedBy = "albaran", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AlbaranVentaLinea> albaranVentaLineas = new LinkedHashSet<>();

    @PrePersist
    protected void onCreate() {
        if (estado == null || estado.isBlank()) {
            estado = "PENDIENTE";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlbaranVenta that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    @Override
    public String toString() { return "AlbaranVenta{id=" + id + ", numero='" + numero + "'}"; }

    public alicanteweb.erp.entities.enums.EstadoAlbaranEnum getEstadoEnum() {
        if (estado == null) return null;
        try {
            return alicanteweb.erp.entities.enums.EstadoAlbaranEnum.valueOf(estado);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
