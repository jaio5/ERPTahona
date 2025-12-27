package alicanteweb.erp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuración de tareas programadas
 * Para backup automático y otras tareas periódicas
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // La anotación @EnableScheduling habilita el soporte para @Scheduled
}

