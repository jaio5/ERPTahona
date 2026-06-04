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
@Table(name = "devoluciones_lineas")
public class DevolucionLinea {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devolucion_id", nullable = false)
    private Devolucion devolucion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id", nullable = false)
    private Articulo articulo;

    @NotNull
    @Column(name = "cantidad", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "importe", precision = 10, scale = 2)
    private BigDecimal importe;

    @Size(max = 200)
    @Column(name = "motivo", length = 200)
    private String motivo;

    @Size(max = 30)
    @Column(name = "destino", length = 30)
    private String destino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id")
    private Lote lote;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DevolucionLinea)) return false;
        return Objects.equals(id, ((DevolucionLinea) o).id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
