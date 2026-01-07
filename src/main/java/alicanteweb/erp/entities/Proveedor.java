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
 * Entidad Proveedor
 */
@Getter
@Setter
@Entity
@Table(name = "proveedores")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Size(max = 100)
    @Column(name = "codigo", nullable = false, unique = true, length = 100)
    private String codigo;

    @NotNull
    @Size(max = 255)
    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Size(max = 255)
    @Column(name = "nombre_comercial")
    private String nombreComercial;

    @Size(max = 20)
    @Column(name = "cif", length = 20)
    private String cif;

    @Size(max = 255)
    @Column(name = "direccion")
    private String direccion;

    @Size(max = 10)
    @Column(name = "codigo_postal", length = 10)
    private String codigoPostal;

    @Size(max = 100)
    @Column(name = "poblacion", length = 100)
    private String poblacion;

    @Size(max = 100)
    @Column(name = "provincia", length = 100)
    private String provincia;

    @Size(max = 100)
    @Column(name = "pais", length = 100)
    @ColumnDefault("'España'")
    private String pais;

    @Size(max = 20)
    @Column(name = "telefono", length = 20)
    private String telefono;

    @Size(max = 20)
    @Column(name = "telefono2", length = 20)
    private String telefono2;

    @Size(max = 20)
    @Column(name = "fax", length = 20)
    private String fax;

    @Size(max = 255)
    @Column(name = "email")
    private String email;

    @Size(max = 255)
    @Column(name = "web")
    private String web;

    @Size(max = 255)
    @Column(name = "persona_contacto")
    private String personaContacto;

    @Size(max = 34)
    @Column(name = "iban", length = 34)
    private String iban;

    @Size(max = 11)
    @Column(name = "swift", length = 11)
    private String swift;

    @Column(name = "dias_pago")
    private Integer diasPago;

    @Size(max = 50)
    @Column(name = "forma_pago", length = 50)
    private String formaPago;

    @Column(name = "descuento", precision = 5, scale = 2)
    private BigDecimal descuento;

    @Column(name = "limite_credito", precision = 10, scale = 2)
    private BigDecimal limiteCredito;

    @Column(name = "activo")
    @ColumnDefault("TRUE")
    private Boolean activo;

    @Lob
    @Column(name = "notas")
    private String notas;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "fecha_ultima_compra")
    private LocalDateTime fechaUltimaCompra;

    @Column(name = "total_compras", precision = 12, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal totalCompras;

    @Column(name = "total_facturas_pendientes", precision = 12, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal totalFacturasPendientes;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaModificacion = LocalDateTime.now();
        if (activo == null) {
            activo = true;
        }
        if (pais == null || pais.isEmpty()) {
            pais = "España";
        }
        if (totalCompras == null) {
            totalCompras = BigDecimal.ZERO;
        }
        if (totalFacturasPendientes == null) {
            totalFacturasPendientes = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}

