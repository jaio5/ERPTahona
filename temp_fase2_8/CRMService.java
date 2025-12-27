package alicanteweb.erp.service;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.Lead;
import alicanteweb.erp.entities.Oportunidad;
import alicanteweb.erp.repository.ClienteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio CRM - Gestión de leads y oportunidades
 * FASE 5: CRM
 */
@Service
@Slf4j
public class CRMService {

    private final ClienteRepository clienteRepository;
    private final AuditoriaService auditoriaService;

    // Repositorios que se crearían
    // private final LeadRepository leadRepository;
    // private final OportunidadRepository oportunidadRepository;

    public CRMService(ClienteRepository clienteRepository,
                     AuditoriaService auditoriaService) {
        this.clienteRepository = clienteRepository;
        this.auditoriaService = auditoriaService;
    }

    /**
     * Convierte un lead en cliente
     */
    @Transactional
    public Cliente convertirLeadACliente(Lead lead) {
        log.info("Convirtiendo lead {} a cliente", lead.getId());

        Cliente cliente = new Cliente();
        cliente.setNombre(lead.getNombre());
        cliente.setEmail(lead.getEmail());
        cliente.setTelefono(lead.getTelefono());
        cliente.setActivo(true);

        Cliente guardado = clienteRepository.save(cliente);

        // Marcar lead como convertido
        lead.setEstado("CLIENTE");
        lead.setActivo(false);
        // leadRepository.save(lead);

        auditoriaService.registrarAccion(null, "CONVERSION_LEAD", "Lead",
                lead.getId().toString(), "Convertido a cliente: " + guardado.getId());

        log.info("Lead convertido a cliente: {}", guardado.getId());
        return guardado;
    }

    /**
     * Calcula el valor del pipeline
     */
    public Map<String, Object> calcularPipeline() {
        Map<String, Object> pipeline = new HashMap<>();

        // En producción, calcular desde oportunidades reales
        BigDecimal valorTotal = BigDecimal.ZERO;
        BigDecimal valorPonderado = BigDecimal.ZERO;

        // Simulación
        pipeline.put("valor_total", valorTotal);
        pipeline.put("valor_ponderado", valorPonderado);
        pipeline.put("numero_oportunidades", 0);
        pipeline.put("tasa_conversion", 0);
        pipeline.put("ticket_medio", BigDecimal.ZERO);

        return pipeline;
    }

    /**
     * Obtiene funnel de ventas
     */
    public Map<String, Integer> obtenerFunnelVentas() {
        Map<String, Integer> funnel = new HashMap<>();

        // En producción, contar desde base de datos
        funnel.put("PROSPECTO", 0);
        funnel.put("CALIFICACION", 0);
        funnel.put("PROPUESTA", 0);
        funnel.put("NEGOCIACION", 0);
        funnel.put("CERRADA_GANADA", 0);
        funnel.put("CERRADA_PERDIDA", 0);

        return funnel;
    }

    /**
     * Genera reporte de conversión
     */
    public Map<String, Object> generarReporteConversion(LocalDate inicio, LocalDate fin) {
        Map<String, Object> reporte = new HashMap<>();

        // Simulación - en producción calcular desde BD
        reporte.put("leads_generados", 0);
        reporte.put("leads_calificados", 0);
        reporte.put("oportunidades_creadas", 0);
        reporte.put("ventas_cerradas", 0);
        reporte.put("tasa_conversion_lead_cliente", 0.0);
        reporte.put("tasa_conversion_oportunidad", 0.0);
        reporte.put("valor_total_cerrado", BigDecimal.ZERO);
        reporte.put("periodo_inicio", inicio);
        reporte.put("periodo_fin", fin);

        return reporte;
    }
}

