package alicanteweb.erp.controller.dto;

import java.math.BigDecimal;
import java.util.List;

public record PendienteFacturarDto(Long clienteId, String clienteNombre, long totalAlbaranes,
                                   BigDecimal importeTotal, List<Long> albaranIds) {
}
