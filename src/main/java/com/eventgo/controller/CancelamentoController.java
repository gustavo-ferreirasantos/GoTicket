package com.eventgo.controller;

import com.eventgo.model.Ingresso;
import com.eventgo.model.enums.StatusIngresso;
import com.eventgo.service.IngressoService;
import com.eventgo.util.AlertUtil;
import com.eventgo.service.SessaoUsuario;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

public class CancelamentoController implements Initializable {

    @FXML private TextField txtCodigo;
    @FXML private Button btnConsultar;
    @FXML private VBox detalhesPane;
    @FXML private Label lblEventoNome;
    @FXML private Label lblEventoDetalhes;
    @FXML private Label lblParticipante;
    @FXML private Label lblCpf;
    @FXML private Label lblIdentificador;
    @FXML private Label lblPreco;
    @FXML private Label lblStatus;
    @FXML private TextArea txtMotivo;
    @FXML private Label lblAvisoCancelamento;
    @FXML private Button btnCancelar;
    @FXML private Button btnLimpar;

    private final IngressoService ingressoService;
    private Ingresso ingressoConsultado;

    public CancelamentoController() {
        this.ingressoService = new IngressoService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        detalhesPane.setVisible(false);
        detalhesPane.setManaged(false);

        if (!SessaoUsuario.getInstancia().podeCancelarIngresso()) {
            AlertUtil.exibirErro("Acesso restrito: apenas o Administrador tem permissão para cancelar ingressos.");
            btnConsultar.setDisable(true);
            btnCancelar.setDisable(true);
            txtCodigo.setDisable(true);
        }
    }

    @FXML
    public void consultarIngresso() {
        String codigoTexto = txtCodigo.getText();
        if (codigoTexto == null || codigoTexto.isBlank()) {
            AlertUtil.exibirAviso("Informe o código do ingresso.");
            return;
        }

        UUID codigo;
        try {
            codigo = UUID.fromString(codigoTexto.trim());
        } catch (IllegalArgumentException e) {
            AlertUtil.exibirErro("Formato de código inválido. Use o formato UUID.");
            return;
        }

        try {
            Optional<Ingresso> resultado = ingressoService.buscarPorCodigo(codigo);
            if (resultado.isEmpty()) {
                AlertUtil.exibirAviso("Nenhum ingresso encontrado com o código informado.");
                limparFormulario();
                return;
            }

            ingressoConsultado = resultado.get();
            exibirDetalhes(ingressoConsultado);
        } catch (Exception e) {
            AlertUtil.exibirErro("Erro ao consultar ingresso: " + e.getMessage());
        }
    }

    private void exibirDetalhes(Ingresso i) {
        lblEventoNome.setText(i.getNomeEvento() != null ? i.getNomeEvento() : "—");
        lblEventoDetalhes.setText(String.format("%s · %s · %s",
                i.getDataEventoFormatada() != null ? i.getDataEventoFormatada() : "—",
                i.getNomeSetor() != null ? i.getNomeSetor() : "—",
                i.getNomeTipoIngresso() != null ? i.getNomeTipoIngresso() : "—"));

        lblParticipante.setText(i.getNomeParticipante() != null ? i.getNomeParticipante() : "—");
        lblCpf.setText(i.getCpfParticipante() != null ? i.getCpfParticipante() : "—");
        lblIdentificador.setText(i.getCodigo() != null ? i.getCodigo().toString() : "—");
        lblPreco.setText(i.getPrecoPago() != null
                ? "R$ " + i.getPrecoPago().toPlainString().replace(".", ",")
                : "R$ 0,00");

        StatusIngresso status = i.getStatus();
        lblStatus.setText(status.getDescricao());
        lblStatus.getStyleClass().clear();
        if (status == StatusIngresso.CANCELADO) {
            lblStatus.getStyleClass().add("badge-cancelado");
        } else if (status == StatusIngresso.UTILIZADO) {
            lblStatus.getStyleClass().add("badge-inativo");
        } else {
            lblStatus.getStyleClass().add("badge-ativo");
        }

        boolean podeCancelar = (status == StatusIngresso.ATIVO || status == StatusIngresso.EMITIDO);
        btnCancelar.setVisible(podeCancelar);
        btnCancelar.setManaged(podeCancelar);
        txtMotivo.setDisable(!podeCancelar);
        lblAvisoCancelamento.setVisible(podeCancelar);
        lblAvisoCancelamento.setManaged(podeCancelar);

        detalhesPane.setVisible(true);
        detalhesPane.setManaged(true);
    }

    @FXML
    public void cancelarIngresso() {
        if (!SessaoUsuario.getInstancia().podeCancelarIngresso()) {
            AlertUtil.exibirErro("Apenas o Administrador tem permissão para cancelar ingressos.");
            return;
        }

        if (ingressoConsultado == null) {
            AlertUtil.exibirAviso("Consulte um ingresso antes de cancelar.");
            return;
        }

        if (ingressoConsultado.getStatus() == StatusIngresso.CANCELADO) {
            AlertUtil.exibirAviso("Este ingresso já foi cancelado anteriormente.");
            return;
        }
        if (ingressoConsultado.getStatus() == StatusIngresso.UTILIZADO) {
            AlertUtil.exibirAviso("Não é permitido cancelar um ingresso que já foi utilizado na entrada.");
            return;
        }

        String motivo = txtMotivo.getText();
        if (motivo == null || motivo.isBlank()) {
            AlertUtil.exibirAviso("O motivo do cancelamento é obrigatório.");
            return;
        }

        try {
            Long usuarioId = SessaoUsuario.getInstancia().getUsuarioLogado().getId();
            ingressoService.cancelarIngresso(ingressoConsultado.getCodigo(), usuarioId, motivo.trim());
            AlertUtil.exibirSucesso("Ingresso cancelado com sucesso. O estoque foi devolvido ao lote.");
            limparFormulario();
        } catch (IllegalArgumentException | IllegalStateException e) {
            AlertUtil.exibirErro(e.getMessage());
        } catch (Exception e) {
            AlertUtil.exibirErro("Erro ao cancelar ingresso: " + e.getMessage());
        }
    }

    @FXML
    public void limparFormulario() {
        txtCodigo.clear();
        txtMotivo.clear();
        detalhesPane.setVisible(false);
        detalhesPane.setManaged(false);
        ingressoConsultado = null;
    }
}
