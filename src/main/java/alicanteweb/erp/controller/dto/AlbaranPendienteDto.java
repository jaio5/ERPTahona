package alicanteweb.erp.controller.dto;

import alicanteweb.erp.entities.AlbaranVenta;

import java.math.BigDecimal;

public record AlbaranPendienteDto(Long id, String numero, Long clienteId, String clienteNombre, BigDecimal total) {

    public static AlbaranPendienteDto from(AlbaranVenta albaran) {
        return new AlbaranPendienteDto(
                albaran.getId(),
                albaran.getNumero(),
                albaran.getCliente() != null ? albaran.getCliente().getId() : null,
                albaran.getCliente() != null ? albaran.getCliente().getNombre() : null,
                albaran.getTotal() != null ? albaran.getTotal() : BigDecimal.ZERO
        );
    }
}
