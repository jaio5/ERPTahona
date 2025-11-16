package alicanteweb.erp.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sectores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sectore {

    @Id
    @Column(name = "CodigoSector", length = 500)
    private String codigoSector;

    @Column(name = "DescripcionSector", length = 500)
    private String descripcionSector;
}

