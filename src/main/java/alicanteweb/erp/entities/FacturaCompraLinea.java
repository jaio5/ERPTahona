package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Línea de factura de compra
 */
@Getter
@Setter
@Entity
@Table(name = "facturas_compra_lineas")
public class FacturaCompraLinea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_compra_id", nullable = false)
    private FacturaCompra facturaCompra;

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

    /**
     * Calcula el importe de la línea
     */
    public void calcularImporte() {
        if (cantidad == null || precioUnitario == null) {
            importe = BigDecimal.ZERO;
            return;
        }

        BigDecimal subtotal = cantidad.multiply(precioUnitario);

        if (descuento != null && descuento.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal importeDescuento = subtotal.multiply(descuento).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            subtotal = subtotal.subtract(importeDescuento);
        }

        importe = subtotal;
    }

    @Override
    public String toString() {
        return descripcion + " - " + cantidad + " x " + precioUnitario + "€";
    }
}

