package com.eventgo;

import com.eventgo.config.DatabaseConfig;
import com.eventgo.config.DatabaseMigrator;
import com.eventgo.util.NavigationUtil;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.logging.Logger;

public class App extends Application {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    @Override
    public void init() {
        LOGGER.info("Inicializando EventGo Desktop...");
        // Executa migrations automáticas no banco de dados
        DatabaseMigrator.executarMigrations();
    }

    @Override
    public void start(Stage primaryStage) {
        LOGGER.info("Carregando tela de login...");
        NavigationUtil.trocarCena(primaryStage, "login.fxml", "GoTicket - Sistema de Gestão de Eventos e Ingressos", 900, 600);
    }

    @Override
    public void stop() {
        LOGGER.info("Encerrando aplicação e pool de conexões...");
        DatabaseConfig.fecharPool();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
