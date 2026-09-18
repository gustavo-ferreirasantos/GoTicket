package com.eventgo.controller;

import com.eventgo.model.Ingresso;
import com.eventgo.service.IngressoService;
import com.eventgo.service.SessaoUsuario;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.UUID;

public class CheckinController implements Initializable {

    private static final DateTimeFormatter DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm:ss");

    @FXML private TextField txtCodigo;
    @FXML private VBox resultadoPane;
    @FXML private Label lblResultado;
    @FXML private Label lblEvento;
    @FXML private Label lblEventoDetalhes;
    @FXML private Label lblParticipante;
    @FXML private Label lblCpf;
    @FXML private Label lblIdentificador;
    @FXML private Label lblDataCheckin;

    private final IngressoService ingressoService;

    public CheckinController() {
        this.ingressoService = new IngressoService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        limparResultado();
        txtCodigo.requestFocus();
    }

    @FXML
    public void registrarEntrada() {
        limparResultado();

        if (!SessaoUsuario.getInstancia().isPortaria()) {
            exibirNegado("ACESSO NEGADO: Usuário sem permissão para registrar entrada.");
            return;
        }

        String texto = txtCodigo.getText();
        if (texto == null || texto.isBlank()) {
            exibirNegado("Informe o identificador do ingresso.");
            return;
        }

        final UUID codigo;
        try {
            codigo = UUID.fromString(texto.trim());
        } catch (IllegalArgumentException e) {
            exibirNegado("Identificador inválido. Informe um UUID válido.");
            selecionarCodigo();
            return;
        }

        try {
            Ingresso ingresso = ingressoService.registrarCheckin(codigo);
            exibirAutorizado(ingresso);
            txtCodigo.clear();
        } catch (IllegalArgumentException | IllegalStateException e) {
            exibirNegado(e.getMessage());
            selecionarCodigo();
        } catch (SQLException e) {
            exibirNegado("Erro ao consultar o ingresso. Tente novamente.");
            selecionarCodigo();
        }

        txtCodigo.requestFocus();
    }

    @FXML
    public void limparFormulario() {
        txtCodigo.clear();
        limparResultado();
        txtCodigo.requestFocus();
    }

    private void exibirAutorizado(Ingresso ingresso) {
        lblResultado.setText("ENTRADA AUTORIZADA");
        lblResultado.setStyle("-fx-text-fill: #15803D; -fx-font-size: 18px; -fx-font-weight: bold;");
        lblEvento.setText(valorOuTraco(ingresso.getNomeEvento()));
        lblEventoDetalhes.setText(String.format("%s · %s · %s",
                valorOuTraco(ingresso.getDataEventoFormatada()),
                valorOuTraco(ingresso.getNomeSetor()),
                valorOuTraco(ingresso.getNomeTipoIngresso())));
        lblParticipante.setText(valorOuTraco(ingresso.getNomeParticipante()));
        lblCpf.setText(valorOuTraco(ingresso.getCpfParticipante()));
        lblIdentificador.setText(ingresso.getCodigo().toString());
        lblDataCheckin.setText(ingresso.getDataCheckin() != null
                ? ingresso.getDataCheckin().format(DATA_HORA)
                : "—");
        mostrarResultado();
    }

    private void exibirNegado(String mensagem) {
        lblResultado.setText(mensagem);
        lblResultado.setStyle("-fx-text-fill: #B91C1C; -fx-font-size: 16px; -fx-font-weight: bold;");
        limparDetalhes();
        mostrarResultado();
    }

    private void mostrarResultado() {
        resultadoPane.setVisible(true);
        resultadoPane.setManaged(true);
    }

    private void limparResultado() {
        resultadoPane.setVisible(false);
        resultadoPane.setManaged(false);
        limparDetalhes();
    }

    private void limparDetalhes() {
        lblEvento.setText("—");
        lblEventoDetalhes.setText("—");
        lblParticipante.setText("—");
        lblCpf.setText("—");
        lblIdentificador.setText("—");
        lblDataCheckin.setText("—");
    }

    private void selecionarCodigo() {
        txtCodigo.selectAll();
        txtCodigo.requestFocus();
    }

    private String valorOuTraco(String valor) {
        return valor == null || valor.isBlank() ? "—" : valor;
    }
}
