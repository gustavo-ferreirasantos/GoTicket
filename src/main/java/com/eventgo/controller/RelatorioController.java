package com.eventgo.controller;

import com.eventgo.dto.VendaRelatorioDTO;
import com.eventgo.model.Evento;
import com.eventgo.model.enums.FormaPagamento;
import com.eventgo.model.enums.StatusVenda;
import com.eventgo.service.EventoService;
import com.eventgo.service.IngressoService;
import com.eventgo.service.RelatorioService;
import com.eventgo.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class RelatorioController implements Initializable {

    @FXML private ComboBox<Evento> cbEvento;
    @FXML private ComboBox<StatusVenda> cbStatus;
    @FXML private ComboBox<FormaPagamento> cbPagamento;

    @FXML private Label lblTotalVendas;
    @FXML private Label lblTotalReceita;

    @FXML private TableView<VendaRelatorioDTO> tabelaVendas;
    @FXML private TableColumn<VendaRelatorioDTO, Long> colVendaId;
    @FXML private TableColumn<VendaRelatorioDTO, String> colEvento;
    @FXML private TableColumn<VendaRelatorioDTO, String> colDataVenda;
    @FXML private TableColumn<VendaRelatorioDTO, String> colPagamento;
    @FXML private TableColumn<VendaRelatorioDTO, Integer> colQtdIngressos;
    @FXML private TableColumn<VendaRelatorioDTO, String> colValorTotal;
    @FXML private TableColumn<VendaRelatorioDTO, String> colStatus;
    @FXML private TableColumn<VendaRelatorioDTO, Void> colAcoes;

    private final RelatorioService relatorioService;
    private final EventoService eventoService;
    private final IngressoService ingressoService;

    private final NumberFormat moedaFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public RelatorioController() {
        this.relatorioService = new RelatorioService();
        this.eventoService = new EventoService();
        this.ingressoService = new IngressoService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarComboBoxes();
        configurarTabela();
        carregarDados();
    }

    private void configurarComboBoxes() {
        cbEvento.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Evento item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Todos" : item.getNome());
            }
        });
        cbEvento.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Evento item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Todos" : item.getNome());
            }
        });

        List<StatusVenda> statusList = new ArrayList<>();
        statusList.add(null);
        for (StatusVenda s : StatusVenda.values()) statusList.add(s);
        cbStatus.setItems(FXCollections.observableArrayList(statusList));
        cbStatus.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(StatusVenda item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Todos" : item.getDescricao());
            }
        });
        cbStatus.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(StatusVenda item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Todos" : item.getDescricao());
            }
        });

        List<FormaPagamento> pagList = new ArrayList<>();
        pagList.add(null);
        for (FormaPagamento f : FormaPagamento.values()) pagList.add(f);
        cbPagamento.setItems(FXCollections.observableArrayList(pagList));
        cbPagamento.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(FormaPagamento item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Todos" : item.getDescricao());
            }
        });
        cbPagamento.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(FormaPagamento item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Todos" : item.getDescricao());
            }
        });

        try {
            List<Evento> eventos = new ArrayList<>();
            eventos.add(null);
            eventos.addAll(eventoService.listar(null));
            cbEvento.setItems(FXCollections.observableArrayList(eventos));
        } catch (SQLException e) {
            AlertUtil.exibirErro("Erro ao carregar eventos: " + e.getMessage());
        }
    }

    private void configurarTabela() {
        colVendaId.setCellValueFactory(new PropertyValueFactory<>("vendaId"));
        colEvento.setCellValueFactory(new PropertyValueFactory<>("eventoNome"));

        colDataVenda.setCellValueFactory(cell -> {
            VendaRelatorioDTO v = cell.getValue();
            String data = v.getDataVenda() != null ? v.getDataVenda().format(dateTimeFormatter) : "-";
            return javafx.beans.binding.Bindings.createStringBinding(() -> data);
        });

        colPagamento.setCellValueFactory(cell -> {
            VendaRelatorioDTO v = cell.getValue();
            String pag = v.getFormaPagamento() != null ? v.getFormaPagamento().getDescricao() : "-";
            return javafx.beans.binding.Bindings.createStringBinding(() -> pag);
        });

        colQtdIngressos.setCellValueFactory(new PropertyValueFactory<>("quantidadeIngressos"));

        colValorTotal.setCellValueFactory(cell -> {
            VendaRelatorioDTO v = cell.getValue();
            String valor = v.getValorTotal() != null ? moedaFormat.format(v.getValorTotal()) : "R$ 0,00";
            return javafx.beans.binding.Bindings.createStringBinding(() -> valor);
        });

        colStatus.setCellValueFactory(cell -> {
            VendaRelatorioDTO v = cell.getValue();
            String status = v.getStatus() != null ? v.getStatus().getDescricao() : "-";
            return javafx.beans.binding.Bindings.createStringBinding(() -> status);
        });

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().clear();
                    if ("Confirmada".equals(item)) {
                        badge.getStyleClass().add("badge-ativo");
                    } else if ("Cancelada".equals(item)) {
                        badge.getStyleClass().add("badge-cancelado");
                    } else {
                        badge.getStyleClass().add("badge-inativo");
                    }
                    setGraphic(badge);
                }
            }
        });

        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnReemitir = new Button("Emitir");

            {
                btnReemitir.getStyleClass().add("btn-secondary");
                btnReemitir.setStyle("-fx-padding: 6 16;");
                btnReemitir.setOnAction(e -> {
                    VendaRelatorioDTO venda = getTableView().getItems().get(getIndex());
                    reemitirComprovante(venda);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    VendaRelatorioDTO venda = getTableView().getItems().get(getIndex());
                    btnReemitir.setDisable(venda.getPrimeiroIngressoCodigo() == null);
                    setGraphic(btnReemitir);
                }
            }
        });
    }

    private void carregarDados() {
        try {
            List<VendaRelatorioDTO> vendas = relatorioService.listarVendas(null, null, null);
            tabelaVendas.setItems(FXCollections.observableArrayList(vendas));
            atualizarResumo(vendas);
        } catch (SQLException e) {
            AlertUtil.exibirErro("Erro ao carregar vendas: " + e.getMessage());
        }
    }

    @FXML
    public void aplicarFiltro() {
        try {
            Evento evento = cbEvento.getValue();
            Long eventoId = evento != null ? evento.getId() : null;
            StatusVenda status = cbStatus.getValue();
            FormaPagamento pagamento = cbPagamento.getValue();

            List<VendaRelatorioDTO> vendas = relatorioService.listarVendas(eventoId, status, pagamento);
            tabelaVendas.setItems(FXCollections.observableArrayList(vendas));
            atualizarResumo(vendas);
        } catch (SQLException e) {
            AlertUtil.exibirErro("Erro ao filtrar vendas: " + e.getMessage());
        }
    }

    @FXML
    public void limparFiltro() {
        cbEvento.setValue(null);
        cbStatus.setValue(null);
        cbPagamento.setValue(null);
        carregarDados();
    }

    private void atualizarResumo(List<VendaRelatorioDTO> vendas) {
        int totalVendas = vendas.size();
        BigDecimal receitaTotal = vendas.stream()
                .map(VendaRelatorioDTO::getValorTotal)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        lblTotalVendas.setText("Vendas: " + totalVendas);
        lblTotalReceita.setText("Receita: " + moedaFormat.format(receitaTotal));
    }

    private void reemitirComprovante(VendaRelatorioDTO venda) {
        if (venda.getPrimeiroIngressoCodigo() == null) {
            AlertUtil.exibirAviso("Esta venda não possui ingressos associados.");
            return;
        }

        boolean imprimir = AlertUtil.confirmar("Emitir Comprovante",
                "Deseja imprimir o comprovante na impressora local?");

        if (imprimir) {
            try {
                boolean impresso = relatorioService.imprimirComprovante(
                        venda.getVendaId(), venda.getFormaPagamento());
                if (impresso) {
                    AlertUtil.exibirSucesso("Comprovante enviado para a impressora com sucesso!");
                } else {
                    AlertUtil.exibirAviso("Nenhuma impressora detectada no sistema.");
                }

                ingressoService.marcarComoEmitido(venda.getPrimeiroIngressoCodigo());
                perguntarSalvarPdf(venda);
            } catch (Exception e) {
                AlertUtil.exibirErro("Erro ao imprimir: " + e.getMessage());
                perguntarSalvarPdf(venda);
            }
        } else {
            perguntarSalvarPdf(venda);
        }
    }

    private void perguntarSalvarPdf(VendaRelatorioDTO venda) {
        boolean salvarPdf = AlertUtil.confirmar("Salvar como PDF",
                "Deseja salvar o comprovante como arquivo PDF?");

        if (salvarPdf) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Salvar Comprovante PDF");
            fileChooser.setInitialFileName("Comprovante_Venda_" + venda.getVendaId() + ".pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documento PDF (*.pdf)", "*.pdf"));

            File file = fileChooser.showSaveDialog(tabelaVendas.getScene().getWindow());
            if (file != null) {
                try {
                    relatorioService.salvarComprovante(
                            venda.getVendaId(), venda.getFormaPagamento(), file.toPath());
                    AlertUtil.exibirSucesso("Comprovante salvo com sucesso em: " + file.getAbsolutePath());
                } catch (Exception e) {
                    AlertUtil.exibirErro("Erro ao salvar PDF: " + e.getMessage());
                }
            }
        }
    }
}
