package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Size(max = 255)
    @NotNull
    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Size(max = 50)
    @Column(name = "cif", length = 50)
    private String cif;

    @Size(max = 20)
    @Column(name = "telefono", length = 20)
    private String telefono;

    @Size(max = 100)
    @Column(name = "email", length = 100)
    private String email;

    @Size(max = 255)
    @Column(name = "direccion")
    private String direccion;

    @Size(max = 100)
    @Column(name = "poblacion", length = 100)
    private String poblacion;

    @Size(max = 20)
    @Column(name = "codigo_postal", length = 20)
    private String codigoPostal;

    @Size(max = 50)
    @Column(name = "provincia", length = 50)
    private String provincia;

    @Lob
    @Column(name = "notas")
    private String notas;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    // ── Domiciliación SEPA ──
    @Size(max = 34)
    @Column(name = "iban", length = 34)
    private String iban;

    /** Referencia única del mandato SEPA (AT-01). */
    @Size(max = 35)
    @Column(name = "mandato_sepa_referencia", length = 35)
    private String mandatoSepaReferencia;

    /** Fecha de firma del mandato SEPA (AT-25). */
    @Column(name = "mandato_sepa_fecha")
    private java.time.LocalDate mandatoSepaFecha;

    /**
     * PrePersist para asegurar valores por defecto
     */
    @PrePersist
    protected void onCreate() {
        if (activo == null) {
            activo = true;
        }
    }

    @OneToMany(mappedBy = "cliente")
    private Set<DireccionenvioNew> direccionesenvioNews = new LinkedHashSet<>();

    @OneToMany(mappedBy = "cliente")
    private Set<Factura> facturas = new LinkedHashSet<>();

    @OneToMany(mappedBy = "cliente")
    private Set<Pedido> pedidos = new LinkedHashSet<>();

}