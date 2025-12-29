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
@Table(name = "users")
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

    @ColumnDefault("true")
    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Size(max = 20)
    @Column(name = "role", length = 20)
    private String role;

    @Size(max = 100)
    @Column(name = "nombre", length = 100)
    private String nombre;

    @Email
    @Size(max = 100)
    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @ColumnDefault("0")
    @Column(name = "intentos_fallidos")
    private Integer intentosFallidos;

    @ColumnDefault("false")
    @Column(name = "bloqueado")
    private Boolean bloqueado;

    @Column(name = "fecha_bloqueo")
    private LocalDateTime fechaBloqueo;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    // Para compatibilidad con código existente
    @Transient
    public Boolean getActivo() {
        return enabled;
    }

    @Transient
    public void setActivo(Boolean activo) {
        this.enabled = activo;
    }

    @Transient
    public String getNombreCompleto() {
        return nombre;
    }

    @Transient
    public void setNombreCompleto(String nombreCompleto) {
        this.nombre = nombreCompleto;
    }

    @Transient
    public LocalDateTime getUltimoLogin() {
        return ultimoAcceso;
    }

    @Transient
    public void setUltimoLogin(LocalDateTime ultimoLogin) {
        this.ultimoAcceso = ultimoLogin;
    }

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
        if (enabled == null) {
            enabled = true;
        }
        if (bloqueado == null) {
            bloqueado = false;
        }
        if (intentosFallidos == null) {
            intentosFallidos = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
}




