package alicanteweb.erp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(System.getenv().getOrDefault("MAIL_HOST", "smtp.gmail.com"));
        sender.setPort(Integer.parseInt(System.getenv().getOrDefault("MAIL_PORT", "587")));
        sender.setUsername(System.getenv("MAIL_USERNAME"));
        sender.setPassword(System.getenv("MAIL_PASSWORD"));
        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        return sender;
    }
}
