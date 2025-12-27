package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Movimiento bancario - Pagos y cobros
 * FASE 4: Financiero
 */
@Getter
@Setter
@Entity
@Table(name = "movimientos_bancarios")
public class MovimientoBancario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banco_id", nullable = false)
    private Banco banco;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @NotNull
    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo; // INGRESO, PAGO, TRANSFERENCIA

    @Column(name = "concepto", length = 200)
    private String concepto;

    @NotNull
    @Column(name = "importe", nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;

    @Column(name = "saldo_anterior", precision = 12, scale = 2)
    private BigDecimal saldoAnterior;

    @Column(name = "saldo_posterior", precision = 12, scale = 2)
    private BigDecimal saldoPosterior;

    @Column(name = "factura_id")
    private Long facturaId;

    @Column(name = "numero_operacion", length = 50)
    private String numeroOperacion;

    @Column(name = "conciliado")
    private Boolean conciliado;

    @Column(name = "fecha_conciliacion")
    private LocalDate fechaConciliacion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @PrePersist
    protected void onCreate() {
        if (conciliado == null) conciliado = false;
    }
}

