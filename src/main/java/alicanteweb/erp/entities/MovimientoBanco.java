package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Movimientos bancarios (ingresos y gastos)
 */
@Getter
@Setter
@Entity
@Table(name = "movimientos_banco")
public class MovimientoBanco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banco_id", nullable = false)
    private Banco banco;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Size(max = 20)
    @Column(name = "tipo", length = 20) // INGRESO, GASTO, TRASPASO
    private String tipo;

    @Column(name = "concepto", length = 500)
    private String concepto;

    @NotNull
    @Column(name = "importe", nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;

    @Column(name = "saldo_resultante", precision = 12, scale = 2)
    private BigDecimal saldoResultante;

    // Relaciones opcionales
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id")
    private Factura factura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_compra_id")
    private FacturaCompra facturaCompra;

    @Column(name = "conciliado")
    private Boolean conciliado;

    @Column(name = "fecha_conciliacion")
    private LocalDate fechaConciliacion;

    @Lob
    @Column(name = "observaciones")
    private String observaciones;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (conciliado == null) conciliado = false;
    }
}

