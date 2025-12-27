package alicanteweb.erp.entities;
}
    }
        if (saldoInicial == null) saldoInicial = BigDecimal.ZERO;
        if (saldoActual == null) saldoActual = BigDecimal.ZERO;
        if (esPrincipal == null) esPrincipal = false;
        if (activo == null) activo = true;
    protected void onCreate() {
    @PrePersist

    private String observaciones;
    @Column(name = "observaciones", columnDefinition = "TEXT")

    private String email;
    @Column(name = "email", length = 100)

    private String telefono;
    @Column(name = "telefono", length = 20)

    private String contacto;
    @Column(name = "contacto", length = 100)

    private Boolean esPrincipal;
    @Column(name = "es_principal")

    private Boolean activo;
    @Column(name = "activo", nullable = false)
    @NotNull

    private BigDecimal saldoInicial;
    @Column(name = "saldo_inicial", precision = 12, scale = 2)

    private BigDecimal saldoActual;
    @Column(name = "saldo_actual", precision = 12, scale = 2)

    private String sucursal;
    @Column(name = "sucursal", length = 100)

    private String oficina;
    @Column(name = "oficina", length = 4)

    private String entidad;
    @Column(name = "entidad", length = 4)

    private String numeroCuenta;
    @Column(name = "numero_cuenta", length = 20)

    private String iban;
    @Column(name = "iban", nullable = false, length = 34)
    @NotNull

    private String swiftBic;
    @Column(name = "swift_bic", length = 11)

    private String nombre;
    @Column(name = "nombre", nullable = false, length = 100)
    @NotNull

    private Long id;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id

public class Banco {
@Table(name = "bancos")
@Entity
@Setter
@Getter
 */
 * FASE 4: Financiero
 * Entidad Banco - Cuentas bancarias de la empresa
/**

import java.math.BigDecimal;

import lombok.Setter;
import lombok.Getter;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.*;


