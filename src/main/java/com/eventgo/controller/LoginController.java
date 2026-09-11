package com.eventgo.controller;

import com.eventgo.model.Usuario;
import com.eventgo.service.SessaoUsuario;
import com.eventgo.service.UsuarioService;
import com.eventgo.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField txtLogin;

    @FXML
    private PasswordField txtSenha;

    @FXML
    private Label lblMensagem;

    @FXML
    private Button btnEntrar;

    private final UsuarioService usuarioService;

    public LoginController() {
        this.usuarioService = new UsuarioService();
    }

    @FXML
    private void handleLogin() {
        lblMensagem.setVisible(false);
        String login = txtLogin.getText();
        String senha = txtSenha.getText();

        try {
            Usuario usuario = usuarioService.autenticar(login, senha);
            SessaoUsuario.getInstancia().iniciarSessao(usuario);

            // Redireciona para tela principal
            Stage stage = (Stage) btnEntrar.getScene().getWindow();
            NavigationUtil.trocarCena(stage, "main.fxml", "GoTicket - Sistema de Gestão de Eventos e Ingressos", 1100, 720);

        } catch (IllegalArgumentException | IllegalStateException e) {
            lblMensagem.setText(e.getMessage());
            lblMensagem.setVisible(true);
        } catch (SQLException e) {
            lblMensagem.setText("Erro de conexão com o banco de dados.");
            lblMensagem.setVisible(true);
        }
    }
}
