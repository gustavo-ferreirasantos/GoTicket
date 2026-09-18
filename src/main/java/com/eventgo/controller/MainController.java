package com.eventgo.controller;

import com.eventgo.model.Usuario;
import com.eventgo.service.SessaoUsuario;
import com.eventgo.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private StackPane contentArea;

    @FXML
    private Button btnNavDashboard;
    @FXML
    private Button btnNavEventos;
    @FXML
    private Button btnNavVenda;
    @FXML
    private Button btnNavCancelamento;
    @FXML
    private Button btnNavCheckin;
    @FXML
    private Button btnNavRelatorios;

    private Button botaoAtivoAtual;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        NavigationUtil.setContentArea(contentArea);
        configurarPermissoes();

        // Carrega Início (Dashboard) por padrão
        navDashboard();
    }

    private void configurarPermissoes() {
        boolean podeUsarPortaria = SessaoUsuario.getInstancia().isPortaria();
        btnNavCheckin.setVisible(podeUsarPortaria);
        btnNavCheckin.setManaged(podeUsarPortaria);
    }

    private void destacarBotao(Button btn) {
        if (botaoAtivoAtual != null) {
            botaoAtivoAtual.getStyleClass().remove("active");
        }
        if (btn != null) {
            btn.getStyleClass().add("active");
            botaoAtivoAtual = btn;
        }
    }

    @FXML
    public void navDashboard() {
        destacarBotao(btnNavDashboard);
        NavigationUtil.carregarView("dashboard.fxml");
    }

    @FXML
    public void navEventos() {
        destacarBotao(btnNavEventos);
        NavigationUtil.carregarView("evento.fxml");
    }

    @FXML
    public void navVenda() {
        destacarBotao(btnNavVenda);
        NavigationUtil.carregarView("venda.fxml");
    }

    @FXML
    public void navCancelamento() {
        destacarBotao(btnNavCancelamento);
        NavigationUtil.carregarView("cancelamento.fxml");
    }

    @FXML
    public void navCheckin() {
        if (!SessaoUsuario.getInstancia().isPortaria()) {
            com.eventgo.util.AlertUtil.exibirErro("Usuário sem permissão para registrar entrada.");
            return;
        }
        destacarBotao(btnNavCheckin);
        NavigationUtil.carregarView("checkin.fxml");
    }

    @FXML
    public void navRelatorios() {
        destacarBotao(btnNavRelatorios);
        NavigationUtil.carregarView("relatorio.fxml");
    }

    @FXML
    public void handleLogout() {
        SessaoUsuario.getInstancia().encerrarSessao();
        Stage stage = (Stage) contentArea.getScene().getWindow();
        NavigationUtil.trocarCena(stage, "login.fxml", "GoTicket - Sistema de Gestão de Eventos e Ingressos", 900, 600);
    }
}
