package alicanteweb.erp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import alicanteweb.erp.util.FinancialMath;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "factura_lineas")
public class FacturaLinea {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "factura_id")
    private Factura factura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id")
    private Articulo articulo;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "cantidad", precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", precision = 10, scale = 4)
    private BigDecimal precioUnitario;

    @Column(name = "precio", precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "descuento", precision = 5, scale = 2)
    private BigDecimal descuento;

    @Column(name = "iva", precision = 5, scale = 2)
    private BigDecimal iva;

    @Column(name = "total", precision = 12, scale = 2)
    private BigDecimal total;

    /**
     * Obtener precio unitario (alias para precio si no existe precioUnitario)
     */
    public BigDecimal getPrecioUnitario() {
        return precioUnitario != null ? precioUnitario : precio;
    }

    /**
     * Calcular total de la línea
     */
    public BigDecimal getTotal() {
        if (total != null) return total;

        if (cantidad == null || getPrecioUnitario() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal subtotal = cantidad.multiply(getPrecioUnitario());

        if (descuento != null && descuento.compareTo(BigDecimal.ZERO) > 0) {
            subtotal = subtotal.subtract(FinancialMath.porcentaje(subtotal, descuento));
        }

        return subtotal;
    }
}