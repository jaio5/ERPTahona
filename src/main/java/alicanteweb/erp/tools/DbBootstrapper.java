package alicanteweb.erp.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DbBootstrapper {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String pass = "Iirne322*";
        String dbName = "tahona";
        if (args.length >= 1) dbName = args[0];
        if (args.length >= 2) user = args[1];
        if (args.length >= 3) pass = args[2];

        System.out.println("Intentando conectar a MySQL para crear DB '" + dbName + "'...");
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + dbName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                st.executeUpdate("CREATE USER IF NOT EXISTS '" + user + "'@'localhost' IDENTIFIED BY '" + pass + "'");
                st.executeUpdate("GRANT ALL PRIVILEGES ON `" + dbName + "`.* TO '" + user + "'@'localhost'");
                st.executeUpdate("FLUSH PRIVILEGES");
            }
            System.out.println("Base de datos y permisos creados/asegurados.");
        } catch (SQLException e) {
            System.err.println("Fallo al crear la DB: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
