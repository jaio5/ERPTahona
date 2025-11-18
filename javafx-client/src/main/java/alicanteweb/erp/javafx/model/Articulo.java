package alicanteweb.erp.javafx.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Articulo {
    @JsonProperty("codigoArticulo")
    private String codigoArticulo;

    @JsonProperty("descripcionCorta")
    private String descripcionCorta;

    @JsonProperty("descripcionArticulo")
    private String descripcionArticulo;

    @JsonProperty("pvp1")
    private BigDecimal pvp1;

    // getters y setters
    public String getCodigoArticulo() { return codigoArticulo; }
    public void setCodigoArticulo(String codigoArticulo) { this.codigoArticulo = codigoArticulo; }

    public String getDescripcionCorta() { return descripcionCorta; }
    public void setDescripcionCorta(String descripcionCorta) { this.descripcionCorta = descripcionCorta; }

    public String getDescripcionArticulo() { return descripcionArticulo; }
    public void setDescripcionArticulo(String descripcionArticulo) { this.descripcionArticulo = descripcionArticulo; }

    public BigDecimal getPvp1() { return pvp1; }
    public void setPvp1(BigDecimal pvp1) { this.pvp1 = pvp1; }
}
