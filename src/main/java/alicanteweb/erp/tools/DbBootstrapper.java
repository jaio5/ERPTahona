package alicanteweb.erp.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Herramienta de consola para crear la base de datos y usuario local para desarrollo.
 * - Valida los identificadores para evitar inyección sobre nombres de objeto.
 * - Escapa literales cuando se usan como valores SQL.
 */
public class DbBootstrapper {

    private static final Logger log = LoggerFactory.getLogger(DbBootstrapper.class);

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "root";
        String pass = System.getenv("MYSQL_ROOT_PASSWORD");
        if (pass == null || pass.isBlank()) pass = "";
        String dbName = "tahona";
        if (args.length >= 1) dbName = args[0];
        if (args.length >= 2) user = args[1];
        if (args.length >= 3) pass = args[2];

        // Validar valores para evitar inyección accidental cuando se interpolan en SQL DDL
        if (isInvalidIdentifier(dbName) || isInvalidIdentifier(user)) {
            log.error("Nombre de base de datos o usuario inválido. Solo se permiten letras, números, guion bajo y guion medio.");
            System.exit(2);
        }

        log.info("Intentando conectar a MySQL para crear DB '{}' como usuario '{}'...", dbName, user);
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            try (Statement st = conn.createStatement()) {
                // NOTA: Identificadores (nombres de BD/usuario) no admiten parámetros en PreparedStatement.
                // Por eso validamos rigurosamente con isInvalidIdentifier() y escapamos literales.
                st.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + dbName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");

                // Para la contraseña, podemos usar PreparedStatement para evitar concatenar el literal directamente.
                try (java.sql.PreparedStatement ps = conn.prepareStatement("CREATE USER IF NOT EXISTS '" + user + "'@'localhost' IDENTIFIED BY ?")) {
                    ps.setString(1, pass == null ? "" : pass);
                    ps.executeUpdate();
                }

                st.executeUpdate("GRANT ALL PRIVILEGES ON `" + dbName + "`.* TO '" + user + "'@'localhost'");
                st.executeUpdate("FLUSH PRIVILEGES");
            }
            log.info("Base de datos y permisos creados/asegurados.");
            System.exit(0);
        } catch (SQLException e) {
            log.error("Fallo al crear la DB o asignar permisos: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    // Permitir solo identificadores seguros para nombres de BD/usuario
    private static boolean isInvalidIdentifier(String s) {
        if (s == null || s.isBlank()) return true;
        return !s.matches("[A-Za-z0-9_\\-]{1,64}");
    }

    // Escapar literal SQL simple para usar dentro de comillas simples: reemplaza ' por ''
    private static String escapeSqlLiteral(String s) {
        if (s == null) return "";
        return s.replace("'", "''");
    }
}
