package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Size(min = 3, max = 50)
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @NotNull
    @Size(max = 255)
    @Column(name = "password", nullable = false)
    private String password;

    @NotNull
    @Email
    @Size(max = 100)
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @NotNull
    @Size(max = 100)
    @Column(name = "nombre_completo", nullable = false, length = 100)
    private String nombreCompleto;

    @Size(max = 20)
    @Column(name = "telefono", length = 20)
    private String telefono;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id")
    private Rol rol;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "bloqueado", nullable = false)
    private Boolean bloqueado;

    @NotNull
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;

    @Column(name = "fecha_cambio_password")
    private LocalDateTime fechaCambioPassword;

    @ColumnDefault("0")
    @Column(name = "intentos_fallidos")
    private Integer intentosFallidos;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "requiere_cambio_password", nullable = false)
    private Boolean requiereCambioPassword;

    @Size(max = 255)
    @Column(name = "token_recuperacion", length = 255)
    private String tokenRecuperacion;

    @Column(name = "fecha_expiracion_token")
    private LocalDateTime fechaExpiracionToken;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @OneToMany(mappedBy = "usuario")
    private Set<AuditoriaAccion> auditoriasAcciones = new LinkedHashSet<>();

    @OneToMany(mappedBy = "usuarioRegistro")
    private Set<RgpdConsentimiento> consentimientosRegistrados = new LinkedHashSet<>();

    @OneToMany(mappedBy = "usuario")
    private Set<RgpdAccesoDatos> accesosRealizados = new LinkedHashSet<>();

    @OneToMany(mappedBy = "usuarioResponsable")
    private Set<RgpdSolicitud> solicitudesResponsable = new LinkedHashSet<>();

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (activo == null) {
            activo = true;
        }
        if (bloqueado == null) {
            bloqueado = false;
        }
        if (intentosFallidos == null) {
            intentosFallidos = 0;
        }
        if (requiereCambioPassword == null) {
            requiereCambioPassword = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (fechaCambioPassword == null) {
            fechaCambioPassword = LocalDateTime.now();
        }
    }
}




