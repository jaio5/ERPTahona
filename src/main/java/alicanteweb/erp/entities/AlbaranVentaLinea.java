package alicanteweb.erp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "albaran_venta_lineas")
public class AlbaranVentaLinea {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id")
    private Articulo articulo;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "cantidad", precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "precio", precision = 10, scale = 2)
    private BigDecimal precio;

    @ColumnDefault("0.00")
    @Column(name = "descuento", precision = 10, scale = 2)
    private BigDecimal descuento;

    /** Interpreta {@link #descuento}: PORCENTAJE (por defecto) o IMPORTE (€ fijo por línea). */
    @Column(name = "descuento_tipo", length = 10)
    private String descuentoTipo;

    @Column(name = "iva", precision = 5, scale = 2)
    private BigDecimal iva;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "albaran_id")
    private AlbaranVenta albaran;

}