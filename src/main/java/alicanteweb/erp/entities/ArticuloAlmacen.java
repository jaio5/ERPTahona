package alicanteweb.erp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "articulo_almacen", uniqueConstraints = @UniqueConstraint(columnNames = {"articulo_id", "almacen_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ArticuloAlmacen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "articulo_id", nullable = false)
    private Articulo articulo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "almacen_id", nullable = false)
    private Almacen almacen;

    @Column(name = "stock", precision = 10, scale = 2, nullable = false)
    private BigDecimal stock = BigDecimal.ZERO;

    public ArticuloAlmacen(Articulo articulo, Almacen almacen) {
        this.articulo = articulo;
        this.almacen = almacen;
        this.stock = BigDecimal.ZERO;
    }
}
