package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final EmpresaConfigService empresaConfigService;

    public EmailService(JavaMailSender mailSender, EmpresaConfigService empresaConfigService) {
        this.mailSender = mailSender; this.empresaConfigService = empresaConfigService;
    }

    public boolean enviarFactura(Factura factura, String destinatario) {
        String mailUser = System.getenv("MAIL_USERNAME");
        if (mailUser == null || mailUser.isBlank()) {
            log.warn("Email no configurado. Configura MAIL_USERNAME y MAIL_PASSWORD.");
            return false;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setTo(destinatario);
            helper.setSubject("Factura " + factura.getNumero() + " - " + empresaConfigService.getConfiguracionActiva()
                    .map(EmpresaConfig::getNombreEmpresa).orElse("ERP Tahona"));
            String body = "<h3>Factura " + factura.getNumero() + "</h3>"
                + "<p>Estimado/a " + (factura.getCliente() != null ? factura.getCliente().getNombre() : "cliente") + ",</p>"
                + "<p>Adjuntamos su factura por importe de <strong>"
                + (factura.getTotal() != null ? String.format("%.2f €", factura.getTotal()) : "0,00 €") + "</strong>.</p>"
                + "<p>Fecha: " + (factura.getFecha() != null ? factura.getFecha().toString() : "-") + "</p>"
                + "<p>Gracias por su confianza.</p>";
            helper.setText(body, true);
            mailSender.send(msg);
            log.info("Factura {} enviada a {}", factura.getNumero(), destinatario);
            return true;
        } catch (Exception e) {
            log.error("Error enviando email de factura {}: {}", factura.getNumero(), e.getMessage());
            return false;
        }
    }

    public boolean enviarPresupuesto(Presupuesto presupuesto, String destinatario) {
        String mailUser = System.getenv("MAIL_USERNAME");
        if (mailUser == null || mailUser.isBlank()) { log.warn("Email no configurado."); return false; }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setTo(destinatario);
            helper.setSubject("Presupuesto " + presupuesto.getNumero());
            String body = "<h3>Presupuesto " + presupuesto.getNumero() + "</h3>"
                + "<p>Estimado/a cliente, adjuntamos nuestro presupuesto.</p>"
                + "<p>Válido hasta: " + (presupuesto.getFechaValidez() != null ? presupuesto.getFechaValidez().toString() : "-") + "</p>";
            helper.setText(body, true);
            mailSender.send(msg);
            return true;
        } catch (Exception e) {
            log.error("Error enviando email de presupuesto {}: {}", presupuesto.getNumero(), e.getMessage());
            return false;
        }
    }
}
