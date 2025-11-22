// java
package alicanteweb.erp.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "verifactu_evidence")
public class VerifactuEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "factura_id", nullable = false, length = 100, unique = true)
    private String facturaId;

    private String serie;
    private String numero;

    @Column(name = "fecha_emision")
    private LocalDateTime fechaEmision;

    @Column(nullable = false, length = 128)
    private String hash;

    @Column(name = "hash_anterior", length = 128)
    private String hashAnterior;

    @Lob
    private byte[] signature;

    @Column(name = "cert_fingerprint", length = 128)
    private String certFingerprint;

    @Column(columnDefinition = "json")
    private String metadata;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // getters y setters
    // (omitir por brevedad)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFacturaId() { return facturaId; }
    public void setFacturaId(String facturaId) { this.facturaId = facturaId; }
    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    public String getHashAnterior() { return hashAnterior; }
    public void setHashAnterior(String hashAnterior) { this.hashAnterior = hashAnterior; }
    public byte[] getSignature() { return signature; }
    public void setSignature(byte[] signature) { this.signature = signature; }
    public String getCertFingerprint() { return certFingerprint; }
    public void setCertFingerprint(String certFingerprint) { this.certFingerprint = certFingerprint; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
