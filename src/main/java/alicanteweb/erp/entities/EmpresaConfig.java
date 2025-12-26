package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

/**
 * Configuración de la empresa para facturas y Verifactu
 * Solo debe haber una fila activa
 */
@Getter
@Setter
@Entity
@Table(name = "empresa_config")
public class EmpresaConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Size(max = 255)
    @Column(name = "nombre_empresa", nullable = false)
    private String nombreEmpresa;

    @Size(max = 255)
    @Column(name = "nombre_comercial")
    private String nombreComercial;

    @NotNull
    @Size(max = 20)
    @Column(name = "cif", nullable = false, length = 20)
    private String cif;

    @Lob
    @Column(name = "direccion")
    private String direccion;

    @Size(max = 10)
    @Column(name = "codigo_postal", length = 10)
    private String codigoPostal;

    @Size(max = 100)
    @Column(name = "ciudad", length = 100)
    private String ciudad;

    @Size(max = 100)
    @Column(name = "provincia", length = 100)
    private String provincia;

    @Size(max = 100)
    @ColumnDefault("'España'")
    @Column(name = "pais", length = 100)
    private String pais;

    @Size(max = 20)
    @Column(name = "telefono", length = 20)
    private String telefono;

    @Size(max = 255)
    @Column(name = "email")
    private String email;

    @Size(max = 255)
    @Column(name = "web")
    private String web;

    @Size(max = 255)
    @Column(name = "registro_mercantil")
    private String registroMercantil;

    @Size(max = 100)
    @Column(name = "registro_sanitario", length = 100)
    private String registroSanitario;

    // Configuración Verifactu
    @ColumnDefault("TRUE")
    @Column(name = "verifactu_habilitado")
    private Boolean verifactuHabilitado;

    @Size(max = 20)
    @Column(name = "verifactu_nif_emisor", length = 20)
    private String verifactuNifEmisor;

    @Size(max = 100)
    @Column(name = "verifactu_nombre_sistema", length = 100)
    private String verifactuNombreSistema;

    @Size(max = 50)
    @Column(name = "verifactu_version_sistema", length = 50)
    private String verifactuVersionSistema;

    @Size(max = 50)
    @Column(name = "verifactu_id_dispositivo", length = 50)
    private String verifactuIdDispositivo;

    @ColumnDefault("TRUE")
    @Column(name = "activo")
    private Boolean activo;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaModificacion = LocalDateTime.now();
        if (activo == null) {
            activo = true;
        }
        if (verifactuHabilitado == null) {
            verifactuHabilitado = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
}

