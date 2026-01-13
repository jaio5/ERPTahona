package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Presupuestos para clientes
 */
@Getter
@Setter
@Entity
@Table(name = "presupuestos")
public class Presupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 100)
    @Column(name = "numero", nullable = false, unique = true, length = 100)
    private String numero;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "fecha_validez")
    private LocalDate fechaValidez;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @NotNull
    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Size(max = 20)
    @ColumnDefault("'BORRADOR'")
    @Column(name = "estado", length = 20)
    private String estado; // BORRADOR, ENVIADO, ACEPTADO, RECHAZADO, CONVERTIDO

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id")
    private Factura factura; // Si se convirtió a factura

    @OneToMany(mappedBy = "presupuesto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PresupuestoLinea> lineas = new ArrayList<>();

    @Lob
    @Column(name = "observaciones")
    private String observaciones;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "usuario_creacion")
    private String usuarioCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaModificacion = LocalDateTime.now();
        if (estado == null) {
            estado = "BORRADOR";
        }
        if (total == null) {
            total = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }

    // ============================================
    // Métodos de conveniencia para tests
    // ============================================

    /**
     * Alias de getFechaValidez para compatibilidad con tests
     */
    public LocalDate getValidoHasta() {
        return this.fechaValidez;
    }

    /**
     * Alias de setFechaValidez para compatibilidad con tests
     */
    public void setValidoHasta(LocalDate validoHasta) {
        this.fechaValidez = validoHasta;
    }
}

