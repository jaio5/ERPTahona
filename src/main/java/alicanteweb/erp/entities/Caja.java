package alicanteweb.erp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Entidad que representa una caja diaria
 */
@Getter
@Setter
@Entity
@Table(name = "cajas")
public class Caja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "saldo_inicial", precision = 12, scale = 2, nullable = false)
    private BigDecimal saldoInicial;

    @Column(name = "saldo_final", precision = 12, scale = 2)
    private BigDecimal saldoFinal;

    @Column(name = "saldo_teorico", precision = 12, scale = 2)
    private BigDecimal saldoTeorico;

    @Column(name = "diferencia", precision = 12, scale = 2)
    private BigDecimal diferencia;

    @Column(name = "estado", length = 20)
    private String estado; // ABIERTA, CERRADA, ARQUEADA

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_apertura_id")
    private Usuario usuarioApertura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_cierre_id")
    private Usuario usuarioCierre;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @OneToMany(mappedBy = "caja", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CajaMovimiento> movimientos = new LinkedHashSet<>();

    /**
     * Calcular saldo teórico (inicial + movimientos)
     */
    public BigDecimal calcularSaldoTeorico() {
        BigDecimal saldo = saldoInicial != null ? saldoInicial : BigDecimal.ZERO;
        for (CajaMovimiento mov : movimientos) {
            if ("INGRESO".equals(mov.getTipo())) {
                saldo = saldo.add(mov.getImporte());
            } else if ("GASTO".equals(mov.getTipo())) {
                saldo = saldo.subtract(mov.getImporte());
            }
        }
        return saldo;
    }

    /**
     * Calcular diferencia (final - teórico)
     */
    public BigDecimal calcularDiferencia() {
        if (saldoFinal == null) return BigDecimal.ZERO;
        BigDecimal teorico = calcularSaldoTeorico();
        return saldoFinal.subtract(teorico);
    }
}

