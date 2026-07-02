package alicanteweb.erp.controller.dto;

import alicanteweb.erp.entities.Devolucion;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DevolucionDto(
        Long id, String numero, LocalDate fecha, String estado,
        Long clienteId, String cliente, String motivo, BigDecimal importeTotal
) {
    public static DevolucionDto from(Devolucion d) {
        return new DevolucionDto(d.getId(), d.getNumero(), d.getFecha(), d.getEstado(),
                d.getCliente() != null ? d.getCliente().getId() : null,
                d.getCliente() != null ? d.getCliente().getNombre() : null,
                d.getMotivo(), d.getImporteTotal());
    }
}
