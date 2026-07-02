package alicanteweb.erp.service;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.FacturaRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;

/**
 * Envío de documentos por email (facturas en PDF a clientes).
 * Requiere configurar MAIL_HOST, MAIL_PORT, MAIL_USERNAME y MAIL_PASSWORD.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final FacturaRepository facturaRepository;
    private final ImpresionService impresionService;
    private final EmpresaConfigService empresaConfigService;
    private final AuditoriaService auditoriaService;

    public EmailService(JavaMailSender mailSender,
                        FacturaRepository facturaRepository,
                        ImpresionService impresionService,
                        EmpresaConfigService empresaConfigService,
                        AuditoriaService auditoriaService) {
        this.mailSender = mailSender;
        this.facturaRepository = facturaRepository;
        this.impresionService = impresionService;
        this.empresaConfigService = empresaConfigService;
        this.auditoriaService = auditoriaService;
    }

    public boolean estaConfigurado() {
        String usuario = System.getenv("MAIL_USERNAME");
        return usuario != null && !usuario.isBlank();
    }

    /**
     * Envía la factura en PDF al email indicado (o al del cliente si no se indica).
     */
    @Transactional
    public String enviarFacturaPorEmail(Long facturaId, String emailDestino, Usuario usuario) {
        if (!estaConfigurado()) {
            throw new IllegalStateException("El envío de email no está configurado: define MAIL_HOST, "
                    + "MAIL_PORT, MAIL_USERNAME y MAIL_PASSWORD en el entorno");
        }
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));
        if (!"EMITIDA".equals(factura.getEstado()) && !"ANULADA".equals(factura.getEstado())) {
            throw new IllegalStateException("Solo se pueden enviar por email facturas emitidas");
        }

        String destino = emailDestino != null && !emailDestino.isBlank()
                ? emailDestino.trim()
                : (factura.getCliente() != null ? factura.getCliente().getEmail() : null);
        if (destino == null || destino.isBlank()) {
            throw new IllegalArgumentException("El cliente no tiene email; indica un destinatario");
        }

        String nombreEmpresa = empresaConfigService.getConfiguracionActiva()
                .map(e -> e.getNombreComercial() != null && !e.getNombreComercial().isBlank()
                        ? e.getNombreComercial() : e.getNombreEmpresa())
                .orElse("ERP Tahona");

        try {
            File pdf = impresionService.generarFacturaPdf(factura);
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setFrom(System.getenv("MAIL_USERNAME"));
            helper.setTo(destino);
            helper.setSubject("Factura " + factura.getNumero() + " · " + nombreEmpresa);
            helper.setText("Estimado cliente:\n\nAdjuntamos la factura " + factura.getNumero()
                    + ".\n\nUn saludo,\n" + nombreEmpresa, false);
            helper.addAttachment("factura-" + factura.getNumero() + ".pdf", pdf);
            mailSender.send(mensaje);

            auditoriaService.registrarAccion(usuario, "ENVIO_EMAIL_FACTURA", "Factura",
                    String.valueOf(facturaId), "Factura " + factura.getNumero() + " enviada a " + destino);
            log.info("Factura {} enviada por email a {}", factura.getNumero(), destino);
            return destino;
        } catch (jakarta.mail.MessagingException e) {
            throw new IllegalStateException("No se pudo enviar el email: " + e.getMessage(), e);
        }
    }
}
