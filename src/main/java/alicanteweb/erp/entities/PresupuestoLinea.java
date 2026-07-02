package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import alicanteweb.erp.util.FinancialMath;
import java.math.BigDecimal;

/**
 * Línea de presupuesto
 */
@Getter
@Setter
@Entity
@Table(name = "presupuestos_lineas")
public class PresupuestoLinea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presupuesto_id", nullable = false)
    private Presupuesto presupuesto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id")
    private Articulo articulo;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @NotNull
    @Column(name = "cantidad", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @NotNull
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 4)
    private BigDecimal precioUnitario;

    @Column(name = "descuento", precision = 5, scale = 2)
    private BigDecimal descuento;

    @Column(name = "tipo_iva", precision = 5, scale = 2)
    private BigDecimal tipoIva;

    @NotNull
    @Column(name = "importe", nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;

    @Column(name = "orden")
    private Integer orden;

    public void calcularImporte() {
        if (cantidad == null || precioUnitario == null) {
            importe = BigDecimal.ZERO;
            return;
        }

        BigDecimal subtotal = cantidad.multiply(precioUnitario);

        if (descuento != null && descuento.compareTo(BigDecimal.ZERO) > 0) {
            subtotal = subtotal.subtract(FinancialMath.porcentaje(subtotal, descuento));
        }

        importe = subtotal;
    }

    public BigDecimal getTotal() {
        return importe;
    }
}

