package com.eventgo.controller;

import com.eventgo.dto.ItemVendaDTO;
import com.eventgo.dto.ParticipanteDTO;
import com.eventgo.dto.VendaDTO;
import com.eventgo.model.*;
import com.eventgo.model.enums.FormaPagamento;
import com.eventgo.service.*;
import com.eventgo.util.AlertUtil;
import com.eventgo.util.ValidacaoUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;

public class VendaController implements Initializable {

    // Stepper Labels
    @FXML private Label step1Label;
    @FXML private Label step2Label;
    @FXML private Label step3Label;
    @FXML private Label step4Label;
    @FXML private Label lblEtapaTitulo;

    // Stepper Panes
    @FXML private VBox step1Pane;
    @FXML private VBox step2Pane;
    @FXML private VBox step3Pane;
    @FXML private VBox step4Pane;

    // Step 1 Controls
    @FXML private ComboBox<Evento> cbEventos;
    @FXML private ComboBox<Setor> cbSetores;
    @FXML private ComboBox<TipoIngresso> cbTiposIngresso;
    @FXML private TextField txtQuantidade;
    @FXML private Label lblDisponibilidade;
    @FXML private Label lblItemResumo;
    @FXML private Label lblSubtotalResumo;
    @FXML private Label lblTotalResumo;

    // Step 2 Controls
    @FXML private TextField txtCpfParticipante;
    @FXML private TextField txtNomeParticipante;
    @FXML private TextField txtEmailParticipante;
    @FXML private TextField txtTelefoneParticipante;

    // Step 3 Controls
    @FXML private Button btnPayCartao;
    @FXML private Button btnPayDinheiro;
    @FXML private Button btnPayPix;
    @FXML private Label lblItemResumoStep3;
    @FXML private Label lblSubtotalStep3;
    @FXML private Label lblTotalStep3;

    // Step 4 Controls
    @FXML private Label lblComprovanteEventoNome;
    @FXML private Label lblComprovanteEventoDetalhes;
    @FXML private Label lblComprovanteParticipante;
    @FXML private Label lblComprovanteSetor;
    @FXML private Label lblComprovanteIdentificador;

    // Services
    private final EventoService eventoService;
    private final SetorService setorService;
    private final TipoIngressoService tipoIngressoService;
    private final LoteService loteService;
    private final ParticipanteService participanteService;
    private final VendaService vendaService;
    private final IngressoService ingressoService;

    private Lote loteAtivo;
    private int quantidadeSelecionada = 1;
    private FormaPagamento formaPagamentoSelecionada = FormaPagamento.CARTAO_CREDITO;
    private Venda vendaConcluida;

    private final NumberFormat moedaFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public VendaController() {
        this.eventoService = new EventoService();
        this.setorService = new SetorService();
        this.tipoIngressoService = new TipoIngressoService();
        this.loteService = new LoteService();
        this.participanteService = new ParticipanteService();
        this.vendaService = new VendaService();
        this.ingressoService = new IngressoService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarComboBoxes();
        carregarEventosAbertos();
        mostrarStep(1);
    }

    private void configurarComboBoxes() {

        cbEventos.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Evento item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome() + " (" + item.getDataEvento().format(dateFormatter) + ")");
            }
        });
        cbEventos.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Evento item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome() + " (" + item.getDataEvento().format(dateFormatter) + ")");
            }
        });

        cbSetores.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Setor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome());
            }
        });
        cbSetores.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Setor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome());
            }
        });

        cbTiposIngresso.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(TipoIngresso item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome() + " [" + item.getCategoria().getDescricao() + "]");
            }
        });
        cbTiposIngresso.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(TipoIngresso item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome() + " [" + item.getCategoria().getDescricao() + "]");
            }
        });
    }

    public void carregarEventosAbertos() {
        try {
            List<Evento> eventos = eventoService.listarAbertosParaVenda();
            cbEventos.setItems(FXCollections.observableArrayList(eventos));
            if (!eventos.isEmpty()) {
                cbEventos.setValue(eventos.get(0));
                aoSelecionarEvento();
            } else {
                cbEventos.setValue(null);
                lblDisponibilidade.setText("Nenhum evento aberto.");
            }
        } catch (SQLException e) {
            AlertUtil.exibirErro("Erro ao carregar eventos: " + e.getMessage());
        }
    }

    @FXML
    public void aoSelecionarEvento() {
        Evento evento = cbEventos.getValue();
        if (evento == null) return;
        try {
            List<Setor> setores = setorService.listarPorEvento(evento.getId());
            cbSetores.setItems(FXCollections.observableArrayList(setores));
            if (!setores.isEmpty()) {
                cbSetores.setValue(setores.get(0));
                aoSelecionarSetor();
            } else {
                cbSetores.setValue(null);
                cbTiposIngresso.getItems().clear();
                lblDisponibilidade.setText("0 ingressos disponíveis");
            }
        } catch (SQLException e) {
            AlertUtil.exibirErro("Erro ao carregar setores: " + e.getMessage());
        }
    }

    @FXML
    public void aoSelecionarSetor() {
        Setor setor = cbSetores.getValue();
        if (setor == null) return;
        try {
            List<TipoIngresso> tipos = tipoIngressoService.listarPorSetor(setor.getId());
            cbTiposIngresso.setItems(FXCollections.observableArrayList(tipos));
            if (!tipos.isEmpty()) {
                cbTiposIngresso.setValue(tipos.get(0));
                aoSelecionarTipo();
            } else {
                cbTiposIngresso.setValue(null);
                lblDisponibilidade.setText("0 ingressos disponíveis");
            }
        } catch (SQLException e) {
            AlertUtil.exibirErro("Erro ao carregar tipos: " + e.getMessage());
        }
    }

    @FXML
    public void aoSelecionarTipo() {
        TipoIngresso tipo = cbTiposIngresso.getValue();
        if (tipo == null) return;
        try {
            List<Lote> lotes = loteService.buscarDisponiveis(tipo.getId());
            if (!lotes.isEmpty()) {
                loteAtivo = lotes.get(0);
                lblDisponibilidade.setText(loteAtivo.getQuantidadeDisponivel() + " ingressos disponíveis");
            } else {
                loteAtivo = null;
                lblDisponibilidade.setText("0 ingressos disponíveis");
            }
            atualizarResumoStep1();
        } catch (SQLException e) {
            AlertUtil.exibirErro("Erro ao buscar lote: " + e.getMessage());
        }
    }

    @FXML
    public void aumentarQuantidade() {
        if (loteAtivo != null && quantidadeSelecionada < loteAtivo.getQuantidadeDisponivel()) {
            quantidadeSelecionada++;
            txtQuantidade.setText(String.valueOf(quantidadeSelecionada));
            atualizarResumoStep1();
        }
    }

    @FXML
    public void diminuirQuantidade() {
        if (quantidadeSelecionada > 1) {
            quantidadeSelecionada--;
            txtQuantidade.setText(String.valueOf(quantidadeSelecionada));
            atualizarResumoStep1();
        }
    }

    @FXML
    public void selecionarCartao() {
        formaPagamentoSelecionada = FormaPagamento.CARTAO_CREDITO;
        atualizarBotoesPagamento();
    }

    @FXML
    public void selecionarDinheiro() {
        formaPagamentoSelecionada = FormaPagamento.DINHEIRO;
        atualizarBotoesPagamento();
    }

    @FXML
    public void selecionarPix() {
        formaPagamentoSelecionada = FormaPagamento.PIX;
        atualizarBotoesPagamento();
    }

    private void atualizarBotoesPagamento() {
        if (btnPayCartao == null || btnPayDinheiro == null || btnPayPix == null) return;
        btnPayCartao.getStyleClass().remove("selected");
        btnPayDinheiro.getStyleClass().remove("selected");
        btnPayPix.getStyleClass().remove("selected");

        if (formaPagamentoSelecionada == FormaPagamento.CARTAO_CREDITO || formaPagamentoSelecionada == FormaPagamento.CARTAO_DEBITO) {
            btnPayCartao.getStyleClass().add("selected");
        } else if (formaPagamentoSelecionada == FormaPagamento.DINHEIRO) {
            btnPayDinheiro.getStyleClass().add("selected");
        } else if (formaPagamentoSelecionada == FormaPagamento.PIX) {
            btnPayPix.getStyleClass().add("selected");
        }
    }

    private void atualizarResumoStep1() {
        if (loteAtivo != null && cbSetores.getValue() != null && cbTiposIngresso.getValue() != null) {
            BigDecimal subtotal = loteAtivo.getPreco().multiply(BigDecimal.valueOf(quantidadeSelecionada));
            String descItem = quantidadeSelecionada + "x " + cbSetores.getValue().getNome() + " (" + cbTiposIngresso.getValue().getNome() + ")";
            lblItemResumo.setText(descItem + " — " + moedaFormat.format(loteAtivo.getPreco()));
            lblSubtotalResumo.setText(moedaFormat.format(subtotal));
            lblTotalResumo.setText(moedaFormat.format(subtotal));

            if (lblItemResumoStep3 != null) {
                lblItemResumoStep3.setText(descItem);
                lblSubtotalStep3.setText(moedaFormat.format(subtotal));
                lblTotalStep3.setText(moedaFormat.format(subtotal));
            }
        } else {
            lblItemResumo.setText("0x Ingresso");
            lblSubtotalResumo.setText("R$ 0,00");
            lblTotalResumo.setText("R$ 0,00");
            if (lblItemResumoStep3 != null) {
                lblItemResumoStep3.setText("0x Ingresso");
                lblSubtotalStep3.setText("R$ 0,00");
                lblTotalStep3.setText("R$ 0,00");
            }
        }
    }

    // ================= Stepper Navigation =================
    private void mostrarStep(int step) {
        step1Pane.setVisible(step == 1);
        step2Pane.setVisible(step == 2);
        step3Pane.setVisible(step == 3);
        step4Pane.setVisible(step == 4);

        step1Label.getStyleClass().removeAll("active");
        step2Label.getStyleClass().removeAll("active");
        step3Label.getStyleClass().removeAll("active");
        step4Label.getStyleClass().removeAll("active");

        switch (step) {
            case 1 -> {
                step1Label.getStyleClass().add("active");
                lblEtapaTitulo.setText("Nova Venda — Seleção");
            }
            case 2 -> {
                step2Label.getStyleClass().add("active");
                lblEtapaTitulo.setText("Nova Venda — Participantes");
            }
            case 3 -> {
                step3Label.getStyleClass().add("active");
                lblEtapaTitulo.setText("Nova Venda — Pagamento");
                atualizarBotoesPagamento();
                atualizarResumoStep1();
            }
            case 4 -> {
                step4Label.getStyleClass().add("active");
                lblEtapaTitulo.setText("Comprovante de Venda");
            }
        }
    }

    @FXML public void cancelarVenda() { reiniciarFluxoVenda(); }
    @FXML public void voltarParaStep1() { mostrarStep(1); }
    @FXML public void voltarParaStep2() { mostrarStep(2); }

    @FXML
    public void avancarParaStep2() {
        if (loteAtivo == null || cbEventos.getValue() == null) {
            AlertUtil.exibirAviso("Selecione um evento, setor e tipo de ingresso válidos.");
            return;
        }
        mostrarStep(2);
    }

    @FXML
    public void avancarParaStep3() {
        String cpf = txtCpfParticipante.getText() != null ? txtCpfParticipante.getText().trim() : "";
        String nome = txtNomeParticipante.getText() != null ? txtNomeParticipante.getText().trim() : "";
        String email = txtEmailParticipante.getText() != null ? txtEmailParticipante.getText().trim() : "";
        String tel = txtTelefoneParticipante.getText() != null ? txtTelefoneParticipante.getText().trim() : "";

        if (cpf.isEmpty()) {
            AlertUtil.exibirAviso("O campo CPF é obrigatório.");
            txtCpfParticipante.requestFocus();
            return;
        }

        if (!ValidacaoUtil.isCpfValido(cpf)) {
            AlertUtil.exibirAviso("O CPF informado é inválido. Digite um CPF válido com 11 dígitos.");
            txtCpfParticipante.requestFocus();
            return;
        }

        if (nome.isEmpty() || nome.length() < 3) {
            AlertUtil.exibirAviso("O campo Nome Completo é obrigatório e deve ter no mínimo 3 caracteres.");
            txtNomeParticipante.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            AlertUtil.exibirAviso("O campo E-mail (Gmail/correio eletrônico) é obrigatório.");
            txtEmailParticipante.requestFocus();
            return;
        }

        if (!ValidacaoUtil.isEmailValido(email)) {
            AlertUtil.exibirAviso("O E-mail informado é inválido. Exemplo: usuario@email.com");
            txtEmailParticipante.requestFocus();
            return;
        }

        if (tel.isEmpty()) {
            AlertUtil.exibirAviso("O campo Telefone é obrigatório.");
            txtTelefoneParticipante.requestFocus();
            return;
        }

        mostrarStep(3);
    }

    @FXML
    public void confirmarPagamento() {
        try {
            String cpf = txtCpfParticipante.getText() != null ? txtCpfParticipante.getText().trim() : "";
            String nome = txtNomeParticipante.getText() != null ? txtNomeParticipante.getText().trim() : "";
            String email = txtEmailParticipante.getText() != null ? txtEmailParticipante.getText().trim() : "";
            String tel = txtTelefoneParticipante.getText() != null ? txtTelefoneParticipante.getText().trim() : "";

            if (cpf.isEmpty() || !ValidacaoUtil.isCpfValido(cpf) || nome.isEmpty() || email.isEmpty() || !ValidacaoUtil.isEmailValido(email) || tel.isEmpty()) {
                AlertUtil.exibirAviso("Por favor, preencha todos os campos do participante corretamente.");
                mostrarStep(2);
                return;
            }

            Participante participante = participanteService.cadastrarOuAtualizar(
                    new ParticipanteDTO(nome, cpf, tel, email)
            );

            ItemVendaDTO item = new ItemVendaDTO();
            item.setLoteId(loteAtivo.getId());
            item.setParticipanteId(participante.getId());
            item.setQuantidade(quantidadeSelecionada);
            item.setPrecoUnitario(loteAtivo.getPreco());

            Long usuarioId = SessaoUsuario.getInstancia().getUsuarioLogado() != null
                    ? SessaoUsuario.getInstancia().getUsuarioLogado().getId() : 1L;

            Evento evento = cbEventos.getValue();

            VendaDTO vendaDTO = new VendaDTO();
            vendaDTO.setUsuarioId(usuarioId);
            vendaDTO.setEventoId(evento.getId());
            vendaDTO.setFormaPagamento(formaPagamentoSelecionada);
            vendaDTO.setItens(List.of(item));

            this.vendaConcluida = vendaService.registrarVenda(vendaDTO);

            // Populate Step 4 Ticket Card
            lblComprovanteEventoNome.setText(evento.getNome());
            String dataStr = evento.getDataEvento() != null ? evento.getDataEvento().format(dateFormatter) : "";
            String horaStr = evento.getHorario() != null ? evento.getHorario().format(timeFormatter) : "20:00";
            String localStr = evento.getLocal() != null ? evento.getLocal() : "Local do Evento";
            lblComprovanteEventoDetalhes.setText(dataStr + " · " + horaStr + " · " + localStr);

            lblComprovanteParticipante.setText(participante.getNome());
            lblComprovanteSetor.setText(cbSetores.getValue().getNome() + " (" + cbTiposIngresso.getValue().getNome() + ")");

            if (!vendaConcluida.getIngressos().isEmpty()) {
                lblComprovanteIdentificador.setText(vendaConcluida.getIngressos().get(0).getCodigo().toString());
            } else {
                lblComprovanteIdentificador.setText("GT-2026-" + String.format("%06d", vendaConcluida.getId()));
            }

            mostrarStep(4);

        } catch (Exception e) {
            AlertUtil.exibirErro("Erro ao registrar venda: " + e.getMessage());
        }
    }

    @FXML
    public void imprimirComprovante() {
        if (vendaConcluida == null || vendaConcluida.getIngressos().isEmpty()) {
            AlertUtil.exibirSucesso("Comprovante enviado para a fila de impressão do sistema.");
            return;
        }

        try {
            UUID codigo = vendaConcluida.getIngressos().get(0).getCodigo();
            boolean impresso = ingressoService.imprimirIngressoLocal(codigo, formaPagamentoSelecionada);

            if (impresso) {
                AlertUtil.exibirSucesso("Ingresso enviado para a impressora com sucesso!");
            } else {
                AlertUtil.exibirAviso("Nenhuma impressora detectada no sistema.");
            }

            boolean salvarPdf = AlertUtil.confirmar("Salvar cópia em PDF",
                    "Deseja salvar uma cópia do comprovante como arquivo PDF?");
            if (salvarPdf) {
                abrirComprovantePdf();
            }
        } catch (Exception e) {
            AlertUtil.exibirErro("Erro ao imprimir ingresso: " + e.getMessage());
        }
    }

    @FXML
    public void abrirComprovantePdf() {
        if (vendaConcluida == null || vendaConcluida.getIngressos().isEmpty()) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Comprovante PDF");
        fileChooser.setInitialFileName("Comprovante_Venda_" + vendaConcluida.getId() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documento PDF (*.pdf)", "*.pdf"));

        File file = fileChooser.showSaveDialog(btnPayCartao.getScene().getWindow());
        if (file != null) {
            try {
                UUID codigo = vendaConcluida.getIngressos().get(0).getCodigo();
                ingressoService.salvarComprovantePDF(codigo, formaPagamentoSelecionada, file.toPath());
                AlertUtil.exibirSucesso("Comprovante salvo com sucesso em: " + file.getAbsolutePath());
            } catch (Exception e) {
                AlertUtil.exibirErro("Erro ao salvar PDF: " + e.getMessage());
            }
        }
    }

    @FXML
    public void reiniciarFluxoVenda() {
        quantidadeSelecionada = 1;
        txtQuantidade.setText("1");
        txtCpfParticipante.clear();
        txtNomeParticipante.clear();
        txtEmailParticipante.clear();
        txtTelefoneParticipante.clear();
        formaPagamentoSelecionada = FormaPagamento.CARTAO_CREDITO;
        carregarEventosAbertos();
        mostrarStep(1);
    }
}
