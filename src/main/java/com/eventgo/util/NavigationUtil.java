package com.eventgo.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NavigationUtil {

    private static final Logger LOGGER = Logger.getLogger(NavigationUtil.class.getName());
    private static StackPane contentArea;

    public static void setContentArea(StackPane area) {
        contentArea = area;
    }

    public static <T> T carregarView(String fxmlPath) {
        if (contentArea == null) {
            LOGGER.severe("ContentArea não foi inicializado!");
            return null;
        }

        try {
            java.net.URL resource = NavigationUtil.class.getResource("/fxml/" + fxmlPath);
            if (resource == null) {
                throw new IOException("Arquivo FXML não encontrado no classpath: /fxml/" + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Node node = loader.load();
            contentArea.getChildren().setAll(node);
            return loader.getController();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar FXML: " + fxmlPath, e);
            AlertUtil.exibirErro("Não foi possível carregar a tela (" + fxmlPath + "): " + (e.getMessage() != null ? e.getMessage() : e.toString()));
            return null;
        }
    }

    public static void trocarCena(Stage stage, String fxmlPath, String titulo, int largura, int altura) {
        try {
            java.net.URL resource = NavigationUtil.class.getResource("/fxml/" + fxmlPath);
            if (resource == null) {
                throw new IOException("Arquivo FXML não encontrado no classpath: /fxml/" + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            Scene scene = new Scene(root, largura, altura);
            
            // Adiciona CSS global de forma segura
            java.net.URL cssUrl = NavigationUtil.class.getResource("/css/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.setTitle(titulo);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao trocar cena para: " + fxmlPath, e);
            AlertUtil.exibirErro("Erro ao inicializar a interface gráfica: " + (e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }
}
