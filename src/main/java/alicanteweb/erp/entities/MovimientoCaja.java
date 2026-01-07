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
 * Entidad MovimientoCaja - Movimientos de entrada/salida de efectivo
 */
@Getter
@Setter
@Entity
@Table(name = "movimientos_caja")
public class MovimientoCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @NotNull
    @Size(max = 10)
    @Column(name = "tipo", nullable = false, length = 10)
    private String tipo; // INGRESO, GASTO

    @NotNull
    @Column(name = "importe", nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;

    @NotNull
    @Size(max = 200)
    @Column(name = "concepto", nullable = false, length = 200)
    private String concepto;

    @Size(max = 100)
    @Column(name = "categoria", length = 100)
    private String categoria; // Ventas, Compras, Gastos generales, etc.

    @Size(max = 100)
    @Column(name = "documento", length = 100)
    private String documento; // Referencia a factura, albarán, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id")
    private Factura factura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Size(max = 500)
    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (fecha == null) {
            fecha = LocalDate.now();
        }
    }

    /**
     * Valida que el tipo sea correcto
     */
    public void setTipo(String tipo) {
        if (tipo != null && !tipo.equals("INGRESO") && !tipo.equals("GASTO")) {
            throw new IllegalArgumentException("Tipo debe ser INGRESO o GASTO");
        }
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return tipo + " - " + importe + "€ - " + concepto;
    }
}

