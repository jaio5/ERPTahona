package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Caja física de la empresa
 */
@Getter
@Setter
@Entity
@Table(name = "cajas")
public class Caja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 100)
    @Column(name = "codigo", nullable = false, unique = true, length = 100)
    private String codigo;

    @NotNull
    @Size(max = 255)
    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "saldo_inicial", precision = 12, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal saldoInicial;

    @Column(name = "saldo_actual", precision = 12, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal saldoActual;

    @Column(name = "activa")
    @ColumnDefault("TRUE")
    private Boolean activa;

    @Lob
    @Column(name = "observaciones")
    private String observaciones;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaModificacion = LocalDateTime.now();
        if (activa == null) activa = true;
        if (saldoInicial == null) saldoInicial = BigDecimal.ZERO;
        if (saldoActual == null) saldoActual = saldoInicial;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
}

