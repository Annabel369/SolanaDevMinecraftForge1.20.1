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
        return getConnection(url);
    }

    private Connection getConnection(String connectionUrl) throws SQLException {
        java.util.Properties props = new java.util.Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("useSSL", ConfigManager.DB_USE_SSL.get().toString());
        props.setProperty("verifyServerCertificate", ConfigManager.DB_VERIFY_CERT.get().toString());
        props.setProperty("autoReconnect", "true");
        props.setProperty("allowPublicKeyRetrieval", "true");
        
        String fixedUrl = connectionUrl;
        if (connectionUrl.startsWith("jdbc:mysql://")) {
            String hostPart = connectionUrl.substring(13);
            if (hostPart.contains("/") && hostPart.contains(":")) {
                int portIndex = hostPart.indexOf(":");
                int slashIndex = hostPart.indexOf("/");
                String portStr = hostPart.substring(portIndex + 1, slashIndex);
                
                if (portStr.equals("25565")) {
                    fixedUrl = "jdbc:mysql://" + hostPart.substring(0, portIndex) + ":3306" + hostPart.substring(slashIndex);
                }
            }
        }
        
        return DriverManager.getConnection(fixedUrl, props);
    }

    public void setupTables() {
        // 1. Tenta criar o Banco de Dados primeiro
        try {
            String baseUrl = url.substring(0, url.lastIndexOf("/") + 1);
            String dbName = url.substring(url.lastIndexOf("/") + 1);
            if (dbName.contains("?")) dbName = dbName.substring(0, dbName.indexOf("?"));

            try (Connection rootConn = getConnection(baseUrl);
                 java.sql.Statement stmt = rootConn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
                LOGGER.info("DatabaseManager: Banco de dados '" + dbName + "' verificado/criado.");
            }
        } catch (Exception e) {
            LOGGER.warning("DatabaseManager: Não foi possível verificar/criar o banco de dados (pode já existir): " + e.getMessage());
        }

        // 2. Cria as tabelas
        String[] queries = {
            "CREATE TABLE IF NOT EXISTS jogadores (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "nome VARCHAR(50) NOT NULL UNIQUE)",

            "CREATE TABLE IF NOT EXISTS carteiras (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "jogador_id INT NOT NULL, " +
            "endereco VARCHAR(100) NOT NULL UNIQUE, " +
            "chave_privada TEXT, " +
            "frase_secreta TEXT, " +
            "FOREIGN KEY (jogador_id) REFERENCES jogadores(id))",

            "CREATE TABLE IF NOT EXISTS banco (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "jogador VARCHAR(50) UNIQUE, " +
            "saldo DOUBLE DEFAULT 0, " +
            "investimento DOUBLE DEFAULT 0, " +
            "divida DOUBLE DEFAULT 0)",

            "CREATE TABLE IF NOT EXISTS locked_chests (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "world VARCHAR(100), " +
            "x DOUBLE, " +
            "y DOUBLE, " +
            "z DOUBLE, " +
            "password VARCHAR(100), " +
            "UNIQUE KEY (world, x, y, z))",

            "CREATE TABLE IF NOT EXISTS homes (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "player_uuid VARCHAR(100), " +
            "home_name VARCHAR(50), " +
            "world VARCHAR(100), " +
            "x DOUBLE, y DOUBLE, z DOUBLE, " +
            "UNIQUE KEY (player_uuid, home_name))",

            "CREATE TABLE IF NOT EXISTS livro_caixa (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "jogador VARCHAR(50), " +
            "tipo_transacao VARCHAR(50), " +
            "valor DOUBLE, " +
            "moeda VARCHAR(10), " +
            "assinatura TEXT, " +
            "data_hora DATETIME)"
        };

        try (Connection conn = getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            for (String query : queries) {
                stmt.executeUpdate(query);
            }
            LOGGER.info("DatabaseManager: Tabelas verificadas/criadas com sucesso!");
        } catch (SQLException e) {
            LOGGER.severe("DatabaseManager: Erro ao configurar tabelas: " + e.getMessage());
        }
    }
}
