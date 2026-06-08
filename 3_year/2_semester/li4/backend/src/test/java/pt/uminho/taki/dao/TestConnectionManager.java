package pt.uminho.taki.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class TestConnectionManager {
    
    private static final String URL = "jdbc:postgresql://localhost:5433/taki_local_db_1";
    private static final String USER = "admin";
    private static final String PASSWORD = "password123";

    static {
        System.setProperty("TAKI_DB_URL", URL);
        System.setProperty("TAKI_DB_USER", USER);
        System.setProperty("TAKI_DB_PASSWORD", PASSWORD);
        
        try {
            initializeDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initializeDatabase() throws Exception {
        String baseDir = System.getProperty("user.dir");
        if (baseDir.endsWith("backend")) {
            baseDir = baseDir.substring(0, baseDir.length() - "/backend".length());
        }
        java.nio.file.Path schemaPath = java.nio.file.Paths.get(baseDir, "database", "schema_postgres.sql");
        java.nio.file.Path logicaPath = java.nio.file.Paths.get(baseDir, "database", "logica_ativa_postgres.sql");

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Ignorar erros se as tabelas já existirem na base de dados de testes permanente
            if (java.nio.file.Files.exists(schemaPath)) {
                String schemaSql = java.nio.file.Files.readString(schemaPath);
                try { stmt.execute(schemaSql); } catch (Exception e) {}
            }
            if (java.nio.file.Files.exists(logicaPath)) {
                String logicaSql = java.nio.file.Files.readString(logicaPath);
                try { stmt.execute(logicaSql); } catch (Exception e) {}
            }
        }
    }

    public static void clearDatabase() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            // Não fazer TRUNCATE global para não quebrar outros testes em paralelo
        }
    }
}
