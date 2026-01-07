package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad Banco - Cuentas bancarias de la empresa
 */
@Getter
@Setter
@Entity
@Table(name = "bancos")
public class Banco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 100)
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre; // Nombre del banco (ej: "BBVA", "Santander")

    @Size(max = 24)
    @Column(name = "iban", length = 24)
    private String iban; // IBAN de la cuenta

    @Size(max = 11)
    @Column(name = "swift", length = 11)
    private String swift; // Código SWIFT/BIC

    @Size(max = 20)
    @Column(name = "numero_cuenta", length = 20)
    private String numeroCuenta; // Número de cuenta (formato antiguo)

    @Column(name = "saldo_actual", precision = 12, scale = 2)
    private BigDecimal saldoActual = BigDecimal.ZERO;

    @Size(max = 3)
    @Column(name = "moneda", length = 3)
    private String moneda = "EUR"; // Código ISO de moneda

    @Column(name = "principal")
    private Boolean principal = false; // Es la cuenta principal

    @Column(name = "activo")
    private Boolean activo = true;

    @Size(max = 500)
    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaModificacion = LocalDateTime.now();
        if (saldoActual == null) {
            saldoActual = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return nombre + (iban != null ? " - " + iban : "");
    }
}

