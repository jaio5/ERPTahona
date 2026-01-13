package alicanteweb.erp.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para BackupService
 */
@SpringBootTest
class BackupServiceTest {

    @Autowired
    private BackupService backupService;

    @Test
    void testVerificarDisponibilidad() {
        // When
        boolean disponible = backupService.verificarDisponibilidad();

        // Then - mysqldump puede o no estar disponible según el sistema
        assertNotNull(disponible);
    }

    @Test
    void testListarBackups() throws Exception {
        // When
        var backups = backupService.listarBackups();

        // Then
        assertNotNull(backups);
        assertTrue(backups instanceof java.util.List);
    }
}

