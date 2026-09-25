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
                
                // Executa script inicial
                stmt.execute(sql);

                // Garante atualização do constraint de perfis
                try {
                    stmt.execute("ALTER TABLE eventgo.usuario DROP CONSTRAINT IF EXISTS usuario_perfil_check; " +
                                 "ALTER TABLE eventgo.usuario ADD CONSTRAINT usuario_perfil_check CHECK (perfil IN ('ADMIN', 'FUNCIONARIO', 'OPERADOR_BILHETERIA', 'OPERADOR_PORTARIA'));");
                } catch (Exception ignored) {}

                // Garante que o usuário funcionário comum padrão existe com a senha 'funcionario123'
                try {
                    String hashFunc = com.eventgo.util.SenhaUtil.hashSenha("funcionario123");
                    String sqlFunc = "INSERT INTO eventgo.usuario (nome, login, senha_hash, perfil, ativo) " +
                                     "VALUES ('Funcionário', 'funcionario', '" + hashFunc + "', 'FUNCIONARIO', true) " +
                                     "ON CONFLICT (login) DO UPDATE SET senha_hash = EXCLUDED.senha_hash, perfil = 'FUNCIONARIO', ativo = true;";
                    stmt.execute(sqlFunc);
                } catch (Exception ex) {
                    LOGGER.warning("Não foi possível criar o usuário funcionário padrão: " + ex.getMessage());
                }

                LOGGER.info("Migrations executadas com sucesso!");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao executar migrations do banco de dados: " + e.getMessage(), e);
        }
    }
}
