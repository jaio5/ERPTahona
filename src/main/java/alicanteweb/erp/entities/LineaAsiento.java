package alicanteweb.erp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entidad que representa una línea de asiento contable
 */
@Getter
@Setter
@Entity
@Table(name = "lineas_asiento")
public class LineaAsiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asiento_id", nullable = false)
    private AsientoContable asiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private PlanCuentas cuenta;

    @Column(name = "debe", precision = 12, scale = 2)
    private BigDecimal debe = BigDecimal.ZERO;

    @Column(name = "haber", precision = 12, scale = 2)
    private BigDecimal haber = BigDecimal.ZERO;

    @Column(name = "concepto", length = 255)
    private String concepto;

    @Column(name = "orden")
    private Integer orden;

    @PrePersist
    protected void onCreate() {
        if (debe == null) debe = BigDecimal.ZERO;
        if (haber == null) haber = BigDecimal.ZERO;
    }
}

