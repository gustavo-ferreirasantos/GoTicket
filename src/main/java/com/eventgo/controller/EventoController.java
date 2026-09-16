package com.eventgo.controller;

import com.eventgo.dto.EventoDTO;
import com.eventgo.dto.FiltroEventoDTO;
import com.eventgo.dto.SetorDTO;
import com.eventgo.config.DatabaseConfig;
import com.eventgo.dto.TipoIngressoDTO;
import com.eventgo.model.Evento;
import com.eventgo.model.Lote;
import com.eventgo.model.Setor;
import com.eventgo.model.TipoIngresso;
import com.eventgo.model.enums.CategoriaIngresso;
import com.eventgo.model.enums.SituacaoEvento;
import com.eventgo.service.EventoService;
import com.eventgo.service.LoteService;
import com.eventgo.service.SetorService;
import com.eventgo.service.TipoIngressoService;
import com.eventgo.util.AlertUtil;
import com.eventgo.util.NavigationUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class EventoController implements Initializable {

    // ================== Frame03: Tabela Principal ==================
    @FXML private VBox listaEventosPane;
    @FXML private TextField txtBuscar;
    @FXML private TableView<Evento> tabelaEventos;
    @FXML private TableColumn<Evento, String> colNome;
    @FXML private TableColumn<Evento, String> colDataHora;
    @FXML private TableColumn<Evento, String> colLocal;
    @FXML private TableColumn<Evento, Integer> colCapacidade;
    @FXML private TableColumn<Evento, Void> colSituacao;
    @FXML private TableColumn<Evento, Void> colAcoes;

    // ================== Frame04: Modal Cadastrar Evento ==================
    @FXML private StackPane modalEventoOverlay;
    @FXML private Label lblModalEventoTitulo;
    @FXML private TextField txtEventoNome;
    @FXML private TextArea txtEventoDescricao;
    @FXML private DatePicker dpEventoData;
    @FXML private TextField txtEventoHorario;
    @FXML private TextField txtEventoLocal;
    @FXML private TextField txtEventoCapacidade;
    @FXML private ComboBox<SituacaoEvento> cbEventoSituacao;

    // ================== Frame05: Modal Setores e Lotes ==================
    @FXML private StackPane modalSetoresOverlay;
    @FXML private Label lblModalSetoresTitulo;
    @FXML private TableView<SetorLoteRow> tabelaSetoresLotes;
    @FXML private TableColumn<SetorLoteRow, String> colSetorNome;
    @FXML private TableColumn<SetorLoteRow, Integer> colSetorCapacidade;
    @FXML private TableColumn<SetorLoteRow, String> colSetorTipo;
    @FXML private TableColumn<SetorLoteRow, String> colSetorPreco;
    @FXML private TableColumn<SetorLoteRow, Integer> colSetorDisponivel;

    @FXML private TextField txtNovoSetorNome;
    @FXML private TextField txtNovoSetorCapacidade;
    @FXML private ComboBox<CategoriaIngresso> cbNovoSetorTipo;
    @FXML private TextField txtNovoSetorPreco;
    @FXML private TextField txtNovoSetorQuantidade;
    @FXML private Label capacidadeLabel;

    // Services
    private final EventoService eventoService;
    private final SetorService setorService;
    private final TipoIngressoService tipoIngressoService;
    private final LoteService loteService;

    private Evento eventoSelecionado;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public EventoController() {
        this.eventoService = new EventoService();
        this.setorService = new SetorService();
        this.tipoIngressoService = new TipoIngressoService();
        this.loteService = new LoteService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabelaEventos();
        configurarModalEvento();
        configurarModalSetores();
        carregarEventos();
    }

    private void configurarTabelaEventos() {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDataHora.setCellValueFactory(cell -> {
            Evento e = cell.getValue();
            if (e.getDataEvento() != null && e.getHorario() != null) {
                return new SimpleStringProperty(e.getDataEvento().format(dateFormatter) + " " + e.getHorario().format(timeFormatter));
            } else if (e.getDataEvento() != null) {
                return new SimpleStringProperty(e.getDataEvento().format(dateFormatter));
            }
            return new SimpleStringProperty("-");
        });
        colLocal.setCellValueFactory(new PropertyValueFactory<>("local"));
        colCapacidade.setCellValueFactory(new PropertyValueFactory<>("capacidadeTotal"));

        // Situação com Badges estilo Figma
        colSituacao.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Evento e = getTableRow().getItem();
                    badge.getStyleClass().clear();
                    if (e.getSituacao() == SituacaoEvento.ABERTO) {
                        badge.setText("Ativo");
                        badge.getStyleClass().add("badge-ativo");
                    } else {
                        badge.setText(e.getSituacao().getDescricao());
                        badge.getStyleClass().add("badge-encerrado");
                    }
                    setGraphic(badge);
                }
            }
        });

        // Ações com links no estilo Figma: Setores, Vender, Relatório
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnSetores = new Button("Setores");
            private final Button btnVender = new Button("Vender");
            private final Button btnRelatorio = new Button("Relatório");
            private final HBox container = new HBox(8, btnSetores, btnVender, btnRelatorio);

            {
                btnSetores.getStyleClass().add("btn-table-action");
                btnVender.getStyleClass().add("btn-table-action");
                btnRelatorio.getStyleClass().add("btn-table-action");

                btnSetores.setOnAction(evt -> {
                    Evento e = getTableRow().getItem();
                    if (e != null) abrirModalSetores(e);
                });

                btnVender.setOnAction(evt -> {
                    NavigationUtil.carregarView("venda.fxml");
                });

                btnRelatorio.setOnAction(evt -> {
                    NavigationUtil.carregarView("placeholder.fxml");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    private void configurarModalEvento() {
        cbEventoSituacao.setItems(FXCollections.observableArrayList(SituacaoEvento.values()));
        cbEventoSituacao.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(SituacaoEvento item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDescricao());
            }
        });
        cbEventoSituacao.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(SituacaoEvento item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDescricao());
            }
        });
    }

    private void configurarModalSetores() {
        colSetorNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colSetorCapacidade.setCellValueFactory(new PropertyValueFactory<>("capacidade"));
        colSetorTipo.setCellValueFactory(new PropertyValueFactory<>("tipoIngresso"));
        colSetorPreco.setCellValueFactory(new PropertyValueFactory<>("precoFormatado"));
        colSetorDisponivel.setCellValueFactory(new PropertyValueFactory<>("disponivel"));

        cbNovoSetorTipo.setItems(FXCollections.observableArrayList(CategoriaIngresso.values()));
        cbNovoSetorTipo.setValue(CategoriaIngresso.INTEIRA);
    }

    public void carregarEventos() {
        try {
            FiltroEventoDTO filtro = new FiltroEventoDTO();
            String busca = txtBuscar.getText() != null ? txtBuscar.getText().trim() : "";
            if (!busca.isEmpty()) {
                filtro.setNome(busca);
            }
            List<Evento> eventos = eventoService.listar(filtro);
            tabelaEventos.setItems(FXCollections.observableArrayList(eventos));
        } catch (SQLException e) {
            AlertUtil.exibirErro("Erro ao carregar eventos: " + e.getMessage());
        }
    }

    @FXML
    public void aplicarBusca() {
        carregarEventos();
    }

    // ================= Modal Cadastrar Evento =================
    @FXML
    public void abrirModalNovoEvento() {
        eventoSelecionado = null;
        lblModalEventoTitulo.setText("Cadastrar Evento");
        txtEventoNome.clear();
        txtEventoDescricao.clear();
        dpEventoData.setValue(LocalDate.now().plusDays(7));
        txtEventoHorario.setText("20:00");
        txtEventoLocal.clear();
        txtEventoCapacidade.clear();
        cbEventoSituacao.setValue(SituacaoEvento.ABERTO);

        if (listaEventosPane != null) listaEventosPane.setVisible(false);
        if (modalSetoresOverlay != null) modalSetoresOverlay.setVisible(false);
        modalEventoOverlay.setVisible(true);
    }

    @FXML
    public void fecharModalEvento() {
        modalEventoOverlay.setVisible(false);
        if (listaEventosPane != null) listaEventosPane.setVisible(true);
    }

    @FXML
    public void salvarEvento() {
        try {
            String nome = txtEventoNome.getText() != null ? txtEventoNome.getText().trim() : "";
            if (nome.isEmpty()) {
                AlertUtil.exibirAviso("O nome do evento é obrigatório.");
                txtEventoNome.requestFocus();
                return;
            }

            LocalDate data = dpEventoData.getValue();
            if (data == null) {
                AlertUtil.exibirAviso("A data do evento é obrigatória.");
                dpEventoData.requestFocus();
                return;
            }

            String horarioStr = txtEventoHorario.getText() != null ? txtEventoHorario.getText().trim() : "";
            if (horarioStr.isEmpty()) {
                AlertUtil.exibirAviso("O horário do evento é obrigatório (ex: 20:00).");
                txtEventoHorario.requestFocus();
                return;
            }

            String local = txtEventoLocal.getText() != null ? txtEventoLocal.getText().trim() : "";
            if (local.isEmpty()) {
                AlertUtil.exibirAviso("O local do evento é obrigatório.");
                txtEventoLocal.requestFocus();
                return;
            }

            String capStr = txtEventoCapacidade.getText() != null ? txtEventoCapacidade.getText().trim() : "";
            if (capStr.isEmpty()) {
                AlertUtil.exibirAviso("A capacidade total do evento é obrigatória.");
                txtEventoCapacidade.requestFocus();
                return;
            }

            int capacidadeTotal;
            try {
                capacidadeTotal = Integer.parseInt(capStr);
                if (capacidadeTotal <= 0) {
                    AlertUtil.exibirAviso("A capacidade total deve ser maior que zero.");
                    txtEventoCapacidade.requestFocus();
                    return;
                }
            } catch (NumberFormatException nfe) {
                AlertUtil.exibirAviso("Capacidade inválida. Digite apenas números ou um valor menor.");
                txtEventoCapacidade.requestFocus();
                return;
            }

            EventoDTO dto = new EventoDTO();
            dto.setNome(nome);
            dto.setDescricao(txtEventoDescricao.getText() != null ? txtEventoDescricao.getText().trim() : "");
            dto.setDataEvento(data);
            dto.setHorario(LocalTime.parse(horarioStr, timeFormatter));
            dto.setLocal(local);
            dto.setCapacidadeTotal(capacidadeTotal);
            dto.setSituacao(cbEventoSituacao.getValue() != null ? cbEventoSituacao.getValue() : SituacaoEvento.ABERTO);

            if (eventoSelecionado == null) {
                eventoService.cadastrar(dto);
                AlertUtil.exibirSucesso("Evento cadastrado com sucesso!");
            } else {
                dto.setId(eventoSelecionado.getId());
                eventoService.atualizar(dto);
                AlertUtil.exibirSucesso("Evento atualizado com sucesso!");
            }

            fecharModalEvento();
            carregarEventos();

        } catch (Exception e) {
            AlertUtil.exibirAviso("Aviso: " + e.getMessage());
        }
    }

    // ================= Modal Setores e Lotes =================
    public void abrirModalSetores(Evento evento) {
        this.eventoSelecionado = evento;
        lblModalSetoresTitulo.setText("Setores e Lotes — " + evento.getNome());
        limparFormNovoSetor();
        carregarTabelaSetoresLotes();
        if (listaEventosPane != null) listaEventosPane.setVisible(false);
        if (modalEventoOverlay != null) modalEventoOverlay.setVisible(false);
        modalSetoresOverlay.setVisible(true);
    }

    @FXML
    public void fecharModalSetores() {
        modalSetoresOverlay.setVisible(false);
        if (listaEventosPane != null) listaEventosPane.setVisible(true);
    }

    private void limparFormNovoSetor() {
        txtNovoSetorNome.clear();
        txtNovoSetorCapacidade.clear();
        cbNovoSetorTipo.setValue(CategoriaIngresso.INTEIRA);
        txtNovoSetorPreco.clear();
        txtNovoSetorQuantidade.clear();
    }

    private void carregarTabelaSetoresLotes() {
        if (eventoSelecionado == null) return;
        try {
            List<Setor> setores = setorService.listarPorEvento(eventoSelecionado.getId());
            List<SetorLoteRow> rows = new ArrayList<>();

            for (Setor s : setores) {
                List<TipoIngresso> tipos = tipoIngressoService.listarPorSetor(s.getId());
                if (tipos.isEmpty()) {
                    rows.add(new SetorLoteRow(s.getNome(), s.getCapacidade(), "Geral", "R$ 0,00", s.getCapacidade()));
                } else {
                    for (TipoIngresso t : tipos) {
                        List<Lote> lotes = loteService.listarPorTipoIngresso(t.getId());
                        if (lotes.isEmpty()) {
                            rows.add(new SetorLoteRow(s.getNome(), s.getCapacidade(), t.getNome(), "R$ 0,00", s.getCapacidade()));
                        } else {
                            for (Lote l : lotes) {
                                String preco = String.format("R$ %.2f", l.getPreco());
                                rows.add(new SetorLoteRow(s.getNome(), s.getCapacidade(), t.getNome(), preco, l.getQuantidadeDisponivel()));
                            }
                        }
                    }
                }
            }

            tabelaSetoresLotes.setItems(FXCollections.observableArrayList(rows));

            int capacidadeUsada = setores.stream()
                    .mapToInt(Setor::getCapacidade)
                    .sum();
            int capacidadeRestante = (eventoSelecionado.getCapacidadeTotal() != null ? eventoSelecionado.getCapacidadeTotal() : 0) - capacidadeUsada;
            capacidadeLabel.setText("Capacidade do setor (" + capacidadeRestante + " restante)");

        } catch (SQLException e) {
            AlertUtil.exibirErro("Erro ao carregar setores: " + e.getMessage());
        }
    }

    @FXML
    public void adicionarSetorELote() {
        if (eventoSelecionado == null) return;

        try {
            String nomeSetor = txtNovoSetorNome.getText() != null ? txtNovoSetorNome.getText().trim() : "";
            if (nomeSetor.isEmpty()) {
                AlertUtil.exibirAviso("O nome do setor é obrigatório.");
                txtNovoSetorNome.requestFocus();
                return;
            }

            String capStr = txtNovoSetorCapacidade.getText() != null ? txtNovoSetorCapacidade.getText().trim() : "";
            if (capStr.isEmpty()) {
                AlertUtil.exibirAviso("A capacidade do setor é obrigatória.");
                txtNovoSetorCapacidade.requestFocus();
                return;
            }

            int capacidade;
            try {
                capacidade = Integer.parseInt(capStr);
                if (capacidade <= 0) {
                    AlertUtil.exibirAviso("A capacidade do setor deve ser maior que zero.");
                    txtNovoSetorCapacidade.requestFocus();
                    return;
                }
            } catch (NumberFormatException nfe) {
                AlertUtil.exibirAviso("Capacidade do setor inválida. Digite apenas números ou um valor menor.");
                txtNovoSetorCapacidade.requestFocus();
                return;
            }

            int capacidadeUsada = setorService.listarPorEvento(eventoSelecionado.getId()).stream()
                    .mapToInt(Setor::getCapacidade)
                    .sum();
            int capacidadeRestante = (eventoSelecionado.getCapacidadeTotal() != null ? eventoSelecionado.getCapacidadeTotal() : 0) - capacidadeUsada;
            if (capacidade > capacidadeRestante) {
                AlertUtil.exibirAviso("A capacidade do setor (" + capacidade + ") excede a capacidade restante do evento (" + capacidadeRestante + ").");
                txtNovoSetorCapacidade.requestFocus();
                return;
            }

            CategoriaIngresso categoria = cbNovoSetorTipo.getValue() != null ? cbNovoSetorTipo.getValue() : CategoriaIngresso.INTEIRA;
            
            String precoStr = txtNovoSetorPreco.getText() != null ? txtNovoSetorPreco.getText().replace("R$", "").replace(",", ".").trim() : "";
            if (precoStr.isEmpty()) {
                AlertUtil.exibirAviso("O preço do lote é obrigatório.");
                txtNovoSetorPreco.requestFocus();
                return;
            }

            BigDecimal preco;
            try {
                preco = new BigDecimal(precoStr);
                if (preco.compareTo(BigDecimal.ZERO) < 0) {
                    AlertUtil.exibirAviso("O preço do lote não pode ser negativo.");
                    txtNovoSetorPreco.requestFocus();
                    return;
                }
            } catch (Exception pe) {
                AlertUtil.exibirAviso("Preço inválido. Exemplo: 150,00");
                txtNovoSetorPreco.requestFocus();
                return;
            }

            String qtdStr = txtNovoSetorQuantidade.getText() != null ? txtNovoSetorQuantidade.getText().trim() : "";
            if (qtdStr.isEmpty()) {
                AlertUtil.exibirAviso("A quantidade de ingressos do lote é obrigatória.");
                txtNovoSetorQuantidade.requestFocus();
                return;
            }

            int quantidade;
            try {
                quantidade = Integer.parseInt(qtdStr);
                if (quantidade <= 0) {
                    AlertUtil.exibirAviso("A quantidade de ingressos do lote deve ser maior que zero.");
                    txtNovoSetorQuantidade.requestFocus();
                    return;
                }
            } catch (NumberFormatException nfe) {
                AlertUtil.exibirAviso("Quantidade inválida. Digite apenas números ou um valor menor.");
                txtNovoSetorQuantidade.requestFocus();
                return;
            }

            // Cadastrar Setor + TipoIngresso + Lote em uma transação única
            Connection conn = DatabaseConfig.getConnection();
            try {
                conn.setAutoCommit(false);

                // 1. Cadastrar Setor
                Setor setor = new Setor();
                setor.setEventoId(eventoSelecionado.getId());
                setor.setNome(nomeSetor);
                setor.setCapacidade(capacidade);
                setorService.cadastrar(conn, setor);

                // 2. Cadastrar TipoIngresso
                TipoIngresso tipo = new TipoIngresso();
                tipo.setSetorId(setor.getId());
                tipo.setNome(categoria.getDescricao());
                tipo.setCategoria(categoria);
                tipoIngressoService.cadastrar(conn, tipo);

                // 3. Cadastrar Lote (com validação RN-02 na mesma transação)
                loteService.cadastrar(conn, tipo.getId(), preco, quantidade,
                        LocalDate.now(),
                        eventoSelecionado.getDataEvento() != null ? eventoSelecionado.getDataEvento() : LocalDate.now().plusMonths(1));

                conn.commit();

                AlertUtil.exibirSucesso("Setor e Lote adicionados com sucesso!");
                limparFormNovoSetor();
                carregarTabelaSetoresLotes();

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.close();
            }

        } catch (Exception e) {
            AlertUtil.exibirAviso("Aviso: " + e.getMessage());
        }
    }

    // Row model for Setores table
    public static class SetorLoteRow {
        private final String nome;
        private final Integer capacidade;
        private final String tipoIngresso;
        private final String precoFormatado;
        private final Integer disponivel;

        public SetorLoteRow(String nome, Integer capacidade, String tipoIngresso, String precoFormatado, Integer disponivel) {
            this.nome = nome;
            this.capacidade = capacidade;
            this.tipoIngresso = tipoIngresso;
            this.precoFormatado = precoFormatado;
            this.disponivel = disponivel;
        }

        public String getNome() { return nome; }
        public Integer getCapacidade() { return capacidade; }
        public String getTipoIngresso() { return tipoIngresso; }
        public String getPrecoFormatado() { return precoFormatado; }
        public Integer getDisponivel() { return disponivel; }
    }
}
