package alicanteweb.erp.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FacturaAlbaranId implements Serializable {
    private static final long serialVersionUID = 2053609762435630127L;
    @Column(name = "factura_id", nullable = false)
    private Long facturaId;

    @Column(name = "albaran_id", nullable = false)
    private Long albaranId;

    public Long getFacturaId() {
        return facturaId;
    }

    public void setFacturaId(Long facturaId) {
        this.facturaId = facturaId;
    }

    public Long getAlbaranId() {
        return albaranId;
    }

    public void setAlbaranId(Long albaranId) {
        this.albaranId = albaranId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        FacturaAlbaranId entity = (FacturaAlbaranId) o;
        return Objects.equals(this.facturaId, entity.facturaId) &&
                Objects.equals(this.albaranId, entity.albaranId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(facturaId, albaranId);
    }

}