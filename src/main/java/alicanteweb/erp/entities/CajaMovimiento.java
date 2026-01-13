package alicanteweb.erp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un movimiento de caja
 */
@Getter
@Setter
@Entity
@Table(name = "caja_movimientos")
public class CajaMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_id", nullable = false)
    private Caja caja;

    @Column(name = "tipo", length = 20, nullable = false)
    private String tipo; // INGRESO, GASTO

    @Column(name = "concepto", nullable = false, length = 255)
    private String concepto;

    @Column(name = "importe", precision = 12, scale = 2, nullable = false)
    private BigDecimal importe;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "forma_pago", length = 50)
    private String formaPago; // EFECTIVO, TARJETA, TRANSFERENCIA

    @Column(name = "referencia", length = 100)
    private String referencia; // Número de factura, ticket, etc.

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    // Relaciones opcionales con otras entidades
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id")
    private Factura factura;

    @Column(name = "factura_compra_id")
    private Long facturaCompraId;
}

