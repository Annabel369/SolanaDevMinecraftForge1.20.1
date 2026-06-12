package com.SolanaDevMinecraft;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger("SolanaForge");
    private final String url;
    private final String user;
    private final String password;

    public DatabaseManager(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.severe("Driver MySQL não encontrado!");
        }
    }

    public Connection getConnection() throws SQLException {
        java.util.Properties props = new java.util.Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("useSSL", ConfigManager.DB_USE_SSL.get().toString());
        props.setProperty("verifyServerCertificate", ConfigManager.DB_VERIFY_CERT.get().toString());
        props.setProperty("autoReconnect", "true");
        props.setProperty("allowPublicKeyRetrieval", "true");
        
        return DriverManager.getConnection(url, props);
    }

    public void setupTables() {
        // Reverting table creation to avoid messing with existing schema
        // The tables should already exist or be managed elsewhere
        LOGGER.info("DatabaseManager inicializado (tabelas gerenciadas externamente).");
    }
}
