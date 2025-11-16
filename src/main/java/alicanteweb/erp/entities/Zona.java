package alicanteweb.erp.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "zonas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Zona {

    @Id
    @Column(name = "Codigo_de_zona", length = 500)
    private String codigoDeZona;

    @Column(name = "Descripcion_Zona", length = 500)
    private String descripcionZona;
}

