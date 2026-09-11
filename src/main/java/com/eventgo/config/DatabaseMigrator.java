package com.eventgo.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DatabaseMigrator {

    private static final Logger LOGGER = Logger.getLogger(DatabaseMigrator.class.getName());

    public static void executarMigrations() {
        LOGGER.info("Iniciando execução de migrations do banco de dados...");
        String scriptPath = "db/migration/V1__schema_inicial.sql";

        try (InputStream in = DatabaseMigrator.class.getClassLoader().getResourceAsStream(scriptPath)) {
            if (in == null) {
                LOGGER.warning("Arquivo de migration não encontrado: " + scriptPath);
                return;
            }

            String sql = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            try (Connection conn = DatabaseConfig.getConnection();
                 Statement stmt = conn.createStatement()) {
                
                // Divide por comandos separados se necessário ou executa o bloco todo
                stmt.execute(sql);
                LOGGER.info("Migrations executadas com sucesso!");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao executar migrations do banco de dados: " + e.getMessage(), e);
        }
    }
}
