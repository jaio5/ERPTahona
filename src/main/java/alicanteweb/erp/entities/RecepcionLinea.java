package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "recepcion_lineas")
public class RecepcionLinea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recepcion_id", nullable = false)
    private Recepcion recepcion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id", nullable = false)
    private Articulo articulo;

    @Column(name = "cantidad_recibida", precision = 10, scale = 2)
    private BigDecimal cantidadRecibida;

    @Column(name = "cantidad_pedida", precision = 10, scale = 2)
    private BigDecimal cantidadPedida;

    @Column(name = "codigo_lote", length = 100)
    private String codigoLote;

    @Column(name = "fecha_caducidad")
    private LocalDate fechaCaducidad;

    @Column(name = "precio_unitario", precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecepcionLinea)) return false;
        return Objects.equals(id, ((RecepcionLinea) o).id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
