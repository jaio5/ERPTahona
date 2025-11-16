package alicanteweb.erp.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "tiposdeiva")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tiposdeiva {

    @Id
    @Column(name = "Codigo_Tipo_de_IVA")
    private Integer codigoTipoDeIva;

    @Column(name = "Porcentaje_de_IVA", precision = 15, scale = 2)
    private BigDecimal porcentajeDeIva;

    @Column(name = "Porcentaje_de_REQ", precision = 15, scale = 2)
    private BigDecimal porcentajeDeReq;

    @Column(name = "SubcuentaIVASoportado")
    private Integer subcuentaIvaSoportado;

    @Column(name = "SubcuentaIVARepercutido")
    private Integer subcuentaIvaRepercutido;

    @Column(name = "SubcuentaREQRepercutido")
    private Integer subcuentaReqRepercutido;
}

