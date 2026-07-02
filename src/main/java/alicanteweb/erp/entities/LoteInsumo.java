package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "lote_insumos")
public class LoteInsumo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_producto_id", nullable = false)
    private Lote loteProducto;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_insumo_id", nullable = false)
    private Lote loteInsumo;

    @NotNull
    @Column(name = "cantidad_usada", nullable = false, precision = 10, scale = 3)
    private BigDecimal cantidadUsada;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoteInsumo)) return false;
        return Objects.equals(id, ((LoteInsumo) o).id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
