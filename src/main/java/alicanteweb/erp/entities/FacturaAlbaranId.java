package alicanteweb.erp.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FacturaAlbaranId implements Serializable {
    @Column(name = "factura_id", nullable = false)
    private Long facturaId;

    @Column(name = "albaran_id", nullable = false)
    private Long albaranId;

    public FacturaAlbaranId() {}
    public FacturaAlbaranId(Long facturaId, Long albaranId) {
        this.facturaId = facturaId;
        this.albaranId = albaranId;
    }
    public Long getFacturaId() { return facturaId; }
    public void setFacturaId(Long facturaId) { this.facturaId = facturaId; }
    public Long getAlbaranId() { return albaranId; }
    public void setAlbaranId(Long albaranId) { this.albaranId = albaranId; }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FacturaAlbaranId that = (FacturaAlbaranId) o;
        return Objects.equals(facturaId, that.facturaId) && Objects.equals(albaranId, that.albaranId);
    }
    @Override
    public int hashCode() {
        return Objects.hash(facturaId, albaranId);
    }

}