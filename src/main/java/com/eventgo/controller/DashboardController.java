package com.eventgo.controller;

import com.eventgo.model.Evento;
import com.eventgo.model.Usuario;
import com.eventgo.service.EventoService;
import com.eventgo.service.SessaoUsuario;
import com.eventgo.util.AlertUtil;
import com.eventgo.util.NavigationUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label lblSaudacao;

    @FXML private TableView<Evento> tabelaProximosEventos;
    @FXML private TableColumn<Evento, String> colNome;
    @FXML private TableColumn<Evento, String> colData;
    @FXML private TableColumn<Evento, String> colLocal;

    private final EventoService eventoService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public DashboardController() {
        this.eventoService = new EventoService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Usuario user = SessaoUsuario.getInstancia().getUsuarioLogado();
        if (user != null && lblSaudacao != null) {
            lblSaudacao.setText("Olá, " + user.getNome());
        }

        configurarTabela();
        atualizarDashboard();
    }

    private void configurarTabela() {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colData.setCellValueFactory(cell -> {
            Evento e = cell.getValue();
            if (e.getDataEvento() != null && e.getHorario() != null) {
                return new SimpleStringProperty(e.getDataEvento().format(dateFormatter) + " " + e.getHorario().format(timeFormatter));
            } else if (e.getDataEvento() != null) {
                return new SimpleStringProperty(e.getDataEvento().format(dateFormatter));
            }
            return new SimpleStringProperty("-");
        });
        colLocal.setCellValueFactory(new PropertyValueFactory<>("local"));
    }

    @FXML
    public void atualizarDashboard() {
        try {
            List<Evento> todosEventos = eventoService.listar(null);
            tabelaProximosEventos.setItems(FXCollections.observableArrayList(todosEventos));
        } catch (SQLException e) {
            AlertUtil.exibirErro("Erro ao carregar eventos recentes: " + e.getMessage());
        }
    }

    @FXML
    public void atalhoNovoEvento() {
        NavigationUtil.carregarView("evento.fxml");
    }

    @FXML
    public void atalhoVenda() {
        NavigationUtil.carregarView("venda.fxml");
    }

    @FXML
    public void atalhoCheckin() {
        NavigationUtil.carregarView("placeholder.fxml");
    }

    @FXML
    public void atalhoCancelamento() {
        NavigationUtil.carregarView("cancelamento.fxml");
    }

    @FXML
    public void atalhoRelatorios() {
        NavigationUtil.carregarView("placeholder.fxml");
    }

    @FXML
    public void handleLogout() {
        SessaoUsuario.getInstancia().encerrarSessao();
        Stage stage = (Stage) lblSaudacao.getScene().getWindow();
        NavigationUtil.trocarCena(stage, "login.fxml", "GoTicket - Sistema de Gestão de Eventos e Ingressos", 900, 600);
    }
}
