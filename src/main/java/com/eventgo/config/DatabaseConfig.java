package com.eventgo.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConfig {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());
    private static HikariDataSource dataSource;
    private static Properties properties = new Properties();

    static {
        carregarConfiguracoes();
        inicializarPool();
    }

    private static void carregarConfiguracoes() {
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                LOGGER.log(Level.WARNING, "Arquivo database.properties não encontrado. Usando padrões.");
                properties.setProperty("db.url", "jdbc:postgresql://localhost:5433/eventgo_db");
                properties.setProperty("db.user", "postgres");
                properties.setProperty("db.password", "postgres");
                properties.setProperty("db.schema", "eventgo");
            } else {
                properties.load(input);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar database.properties", ex);
        }
    }

    private static void inicializarPool() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(properties.getProperty("db.url", "jdbc:postgresql://localhost:5433/eventgo_db"));
            config.setUsername(properties.getProperty("db.user", "postgres"));
            config.setPassword(properties.getProperty("db.password", "postgres"));
            config.setSchema(properties.getProperty("db.schema", "eventgo"));

            int maxPoolSize = Integer.parseInt(properties.getProperty("db.pool.maximumPoolSize", "10"));
            int minIdle = Integer.parseInt(properties.getProperty("db.pool.minimumIdle", "2"));
            long idleTimeout = Long.parseLong(properties.getProperty("db.pool.idleTimeout", "30000"));
            long connTimeout = Long.parseLong(properties.getProperty("db.pool.connectionTimeout", "10000"));

            config.setMaximumPoolSize(maxPoolSize);
            config.setMinimumIdle(minIdle);
            config.setIdleTimeout(idleTimeout);
            config.setConnectionTimeout(connTimeout);

            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);
            LOGGER.info("HikariCP DataSource inicializado com sucesso.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Falha ao inicializar HikariDataSource", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            inicializarPool();
        }
        if (dataSource == null) {
            throw new SQLException("Não foi possível conectar ao banco de dados. Verifique se o PostgreSQL está em execução e acessível.");
        }
        return dataSource.getConnection();
    }

    public static void fecharPool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LOGGER.info("HikariCP DataSource encerrado.");
        }
    }

    public static Properties getProperties() {
        return properties;
    }
}
