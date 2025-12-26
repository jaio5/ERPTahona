package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entidad para gestionar consentimientos RGPD
 * Cumplimiento con RGPD (UE 2016/679) y LOPDGDD (Ley OrgÃ¡nica 3/2018)
 */
@Getter
@Setter
@Entity
@Table(name = "rgpd_consentimientos")
public class RgpdConsentimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @NotNull
    @Size(max = 50)
    @Column(name = "tipo_consentimiento", nullable = false, length = 50)
    private String tipoConsentimiento;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "otorgado", nullable = false)
    private Boolean otorgado;

    @NotNull
    @Column(name = "fecha_consentimiento", nullable = false)
    private LocalDateTime fechaConsentimiento;

    @Column(name = "fecha_revocacion")
    private LocalDateTime fechaRevocacion;

    @Size(max = 45)
    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;

    @Column(name = "texto_consentimiento", columnDefinition = "TEXT")
    private String textoConsentimiento;

    @Size(max = 20)
    @Column(name = "version_politica", length = 20)
    private String versionPolitica;

    @Size(max = 50)
    @Column(name = "canal", length = 50)
    private String canal;

    @Email
    @Size(max = 100)
    @Column(name = "email", length = 100)
    private String email;

    @Size(max = 200)
    @Column(name = "nombre", length = 200)
    private String nombre;

    @Column(name = "metadata")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_registro_id")
    private Usuario usuarioRegistro;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @PrePersist
    protected void onCreate() {
        if (fechaConsentimiento == null) {
            fechaConsentimiento = LocalDateTime.now();
        }
        if (activo == null) {
            activo = true;
        }
        if (otorgado == null) {
            otorgado = false;
        }
    }
}


