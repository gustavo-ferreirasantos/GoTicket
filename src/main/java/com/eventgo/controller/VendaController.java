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
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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
import java.util.Objects;
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
    @FXML private Spinner<Integer> spQuantidade;
    @FXML private Label lblDisponibilidade;
    @FXML private VBox vboxCarrinho;
    @FXML private Label lblTotalResumo;

    // Step 2 Controls
    @FXML private VBox vboxParticipantes;

    // Step 3 Controls
    @FXML private Button btnPayCartao;
    @FXML private Button btnPayDinheiro;
    @FXML private Button btnPayPix;
    @FXML private VBox vboxResumoStep3;
    @FXML private Label lblTotalStep3;

    // Step 4 Controls
    @FXML private Label lblVendaConcluida;
    @FXML private FlowPane flowComprovantes;

    // Services
    private final EventoService eventoService;
    private final SetorService setorService;
    private final TipoIngressoService tipoIngressoService;
    private final LoteService loteService;
    private final ParticipanteService participanteService;
    private final VendaService vendaService;
    private final IngressoService ingressoService;

    private Lote loteAtivo;
    private FormaPagamento formaPagamentoSelecionada = FormaPagamento.CARTAO_CREDITO;
    private Venda vendaConcluida;

    // Carrinho da venda: todos os itens pertencem ao mesmo evento
    private final List<ItemCarrinho> carrinho = new ArrayList<>();
    private Evento eventoDoCarrinho;
    private boolean revertendoEvento;

    // Um formulário de titular por ingresso (na ordem de ingressosDoCarrinho())
    private final List<FormParticipante> formsParticipantes = new ArrayList<>();
    private final List<Label> statusComprovantes = new ArrayList<>();

    private final NumberFormat moedaFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private static class ItemCarrinho {
        final Setor setor;
        final TipoIngresso tipo;
        final Lote lote;
        int quantidade;

        ItemCarrinho(Setor setor, TipoIngresso tipo, Lote lote, int quantidade) {
            this.setor = setor;
            this.tipo = tipo;
            this.lote = lote;
            this.quantidade = quantidade;
        }

        String descricao() {
            return setor.getNome() + " (" + tipo.getNome() + ")";
        }

        BigDecimal subtotal() {
            return lote.getPreco().multiply(BigDecimal.valueOf(quantidade));
        }
    }

    private static class FormParticipante {
        final TextField txtCpf = new TextField();
        final TextField txtNome = new TextField();
        final TextField txtEmail = new TextField();
        final TextField txtTelefone = new TextField();

        FormParticipante() {
            txtCpf.setPromptText("000.000.000-00");
            txtNome.setPromptText("Nome do titular");
            txtEmail.setPromptText("email@exemplo.com");
            txtTelefone.setPromptText("(00) 00000-0000");
        }

        void copiarDe(FormParticipante outro) {
            txtCpf.setText(outro.txtCpf.getText());
            txtNome.setText(outro.txtNome.getText());
            txtEmail.setText(outro.txtEmail.getText());
            txtTelefone.setText(outro.txtTelefone.getText());
        }

        String cpf() { return texto(txtCpf); }
        String nome() { return texto(txtNome); }
        String email() { return texto(txtEmail); }
        String telefone() { return texto(txtTelefone); }

        private static String texto(TextField campo) {
            return campo.getText() != null ? campo.getText().trim() : "";
        }
    }

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
        spQuantidade.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1, 1));
        carregarEventosAbertos();
        atualizarCarrinho();
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
                lblDisponibilidade.setText("Nenhum evento com ingressos disponíveis no estoque.");
            }
        } catch (SQLException e) {
            AlertUtil.exibirErro("Erro ao carregar eventos: " + e.getMessage());
        }
    }

    @FXML
    public void aoSelecionarEvento() {
        if (revertendoEvento) return;
        Evento evento = cbEventos.getValue();
        if (evento == null) return;

        // Uma venda pertence a um único evento: trocar de evento esvazia o carrinho
        if (!carrinho.isEmpty() && !Objects.equals(evento.getId(), eventoDoCarrinho.getId())) {
            boolean trocar = AlertUtil.confirmar("Trocar de evento",
                    "Os ingressos já adicionados são de outro evento e serão removidos. Deseja continuar?");
            if (!trocar) {
                revertendoEvento = true;
                cbEventos.setValue(eventoDoCarrinho);
                revertendoEvento = false;
                return;
            }
            carrinho.clear();
            atualizarCarrinho();
        }

        try {
            List<Setor> setores = setorService.listarPorEvento(evento.getId());
            cbSetores.setItems(FXCollections.observableArrayList(setores));
            if (!setores.isEmpty()) {
                cbSetores.setValue(setores.get(0));
                aoSelecionarSetor();
            } else {
                cbSetores.setValue(null);
                cbTiposIngresso.getItems().clear();
                loteAtivo = null;
                atualizarDisponibilidade();
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
                loteAtivo = null;
                atualizarDisponibilidade();
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
            loteAtivo = lotes.isEmpty() ? null : lotes.get(0);
            atualizarDisponibilidade();
        } catch (SQLException e) {
            AlertUtil.exibirErro("Erro ao buscar lote: " + e.getMessage());
        }
    }

    /** Ingressos do lote ativo que ainda podem ser adicionados (estoque menos o que já está no carrinho). */
    private int disponivelParaAdicionar() {
        if (loteAtivo == null) return 0;
        int noCarrinho = carrinho.stream()
                .filter(i -> Objects.equals(i.lote.getId(), loteAtivo.getId()))
                .mapToInt(i -> i.quantidade)
                .sum();
        return Math.max(0, loteAtivo.getQuantidadeDisponivel() - noCarrinho);
    }

    private void atualizarDisponibilidade() {
        int disponivel = disponivelParaAdicionar();
        if (loteAtivo == null) {
            lblDisponibilidade.setText("0 ingressos disponíveis");
        } else {
            lblDisponibilidade.setText(disponivel + " ingressos disponíveis — " + moedaFormat.format(loteAtivo.getPreco()) + " cada");
        }
        int max = Math.max(1, disponivel);
        spQuantidade.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, max, 1));
        spQuantidade.setDisable(disponivel == 0);
    }

    @FXML
    public void adicionarAoCarrinho() {
        if (loteAtivo == null || cbEventos.getValue() == null || cbSetores.getValue() == null || cbTiposIngresso.getValue() == null) {
            AlertUtil.exibirAviso("Selecione um evento, setor e tipo de ingresso válidos.");
            return;
        }

        int quantidade;
        try {
            // O texto digitado no Spinner só é confirmado com Enter, então lemos direto do editor
            quantidade = Integer.parseInt(spQuantidade.getEditor().getText().trim());
        } catch (NumberFormatException e) {
            AlertUtil.exibirAviso("Informe uma quantidade válida.");
            return;
        }

        int disponivel = disponivelParaAdicionar();
        if (quantidade < 1) {
            AlertUtil.exibirAviso("A quantidade deve ser de pelo menos 1 ingresso.");
            return;
        }
        if (quantidade > disponivel) {
            AlertUtil.exibirAviso("Quantidade indisponível. Restam " + disponivel + " ingressos deste tipo para adicionar.");
            return;
        }

        ItemCarrinho existente = carrinho.stream()
                .filter(i -> Objects.equals(i.lote.getId(), loteAtivo.getId()))
                .findFirst()
                .orElse(null);
        if (existente != null) {
            existente.quantidade += quantidade;
        } else {
            carrinho.add(new ItemCarrinho(cbSetores.getValue(), cbTiposIngresso.getValue(), loteAtivo, quantidade));
        }
        eventoDoCarrinho = cbEventos.getValue();

        atualizarCarrinho();
        atualizarDisponibilidade();
    }

    /** Ajusta a quantidade de um item já no carrinho, entre 1 e o estoque do lote. */
    private void alterarQuantidade(ItemCarrinho item, int delta) {
        int novaQuantidade = item.quantidade + delta;
        if (novaQuantidade < 1 || novaQuantidade > item.lote.getQuantidadeDisponivel()) return;
        item.quantidade = novaQuantidade;
        atualizarCarrinho();
        atualizarDisponibilidade();
    }

    private void removerDoCarrinho(ItemCarrinho item) {
        carrinho.remove(item);
        atualizarCarrinho();
        atualizarDisponibilidade();
    }

    private BigDecimal totalCarrinho() {
        return carrinho.stream().map(ItemCarrinho::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Lista "achatada" com um item por ingresso, na ordem em que os titulares são pedidos. */
    private List<ItemCarrinho> ingressosDoCarrinho() {
        List<ItemCarrinho> ingressos = new ArrayList<>();
        for (ItemCarrinho item : carrinho) {
            for (int i = 0; i < item.quantidade; i++) {
                ingressos.add(item);
            }
        }
        return ingressos;
    }

    private void atualizarCarrinho() {
        vboxCarrinho.getChildren().clear();
        vboxResumoStep3.getChildren().clear();

        if (carrinho.isEmpty()) {
            Label vazio = new Label("Nenhum ingresso adicionado. Escolha setor, tipo e quantidade e clique em \"Adicionar\".");
            vazio.getStyleClass().add("subtitle-light");
            vazio.setWrapText(true);
            vboxCarrinho.getChildren().add(vazio);
        }

        for (ItemCarrinho item : carrinho) {
            String descItem = item.quantidade + "x " + item.descricao();

            Label lblDesc = new Label(item.descricao() + " — " + moedaFormat.format(item.lote.getPreco()) + " cada");
            lblDesc.getStyleClass().add("subtitle-light");
            lblDesc.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(lblDesc, Priority.ALWAYS);

            Button btnDiminuir = new Button("−");
            btnDiminuir.getStyleClass().add("btn-secondary");
            btnDiminuir.setDisable(item.quantidade <= 1);
            btnDiminuir.setOnAction(e -> alterarQuantidade(item, -1));
            Label lblQuantidade = new Label(String.valueOf(item.quantidade));
            lblQuantidade.getStyleClass().add("field-label");
            lblQuantidade.setMinWidth(28);
            lblQuantidade.setAlignment(Pos.CENTER);
            Button btnAumentar = new Button("+");
            btnAumentar.getStyleClass().add("btn-secondary");
            btnAumentar.setDisable(item.quantidade >= item.lote.getQuantidadeDisponivel());
            btnAumentar.setOnAction(e -> alterarQuantidade(item, +1));
            HBox controleQuantidade = new HBox(6, btnDiminuir, lblQuantidade, btnAumentar);
            controleQuantidade.setAlignment(Pos.CENTER);

            Label lblSubtotal = new Label(moedaFormat.format(item.subtotal()));
            lblSubtotal.getStyleClass().add("field-label");
            lblSubtotal.setMinWidth(90);
            lblSubtotal.setAlignment(Pos.CENTER_RIGHT);
            Button btnRemover = new Button("Remover");
            btnRemover.getStyleClass().add("btn-secondary");
            btnRemover.setOnAction(e -> removerDoCarrinho(item));
            HBox linha = new HBox(12, lblDesc, controleQuantidade, lblSubtotal, btnRemover);
            linha.setAlignment(Pos.CENTER_LEFT);
            vboxCarrinho.getChildren().add(linha);

            Label lblDescStep3 = new Label(descItem);
            lblDescStep3.getStyleClass().add("subtitle-light");
            lblDescStep3.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(lblDescStep3, Priority.ALWAYS);
            Label lblSubtotalStep3 = new Label(moedaFormat.format(item.subtotal()));
            lblSubtotalStep3.getStyleClass().add("field-label");
            HBox linhaStep3 = new HBox(lblDescStep3, lblSubtotalStep3);
            linhaStep3.setAlignment(Pos.CENTER_LEFT);
            vboxResumoStep3.getChildren().add(linhaStep3);
        }

        String total = moedaFormat.format(totalCarrinho());
        lblTotalResumo.setText(total);
        lblTotalStep3.setText(total);
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
                atualizarCarrinho();
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
        // Atalho: com o carrinho vazio, "Continuar" adiciona a seleção atual
        if (carrinho.isEmpty() && loteAtivo != null) {
            adicionarAoCarrinho();
        }
        if (carrinho.isEmpty()) {
            AlertUtil.exibirAviso("Adicione pelo menos um ingresso à venda antes de continuar.");
            return;
        }
        montarFormulariosParticipantes();
        mostrarStep(2);
    }

    /** Cria um formulário de titular por ingresso, preservando o que já foi digitado. */
    private void montarFormulariosParticipantes() {
        List<ItemCarrinho> ingressos = ingressosDoCarrinho();

        while (formsParticipantes.size() < ingressos.size()) {
            formsParticipantes.add(new FormParticipante());
        }
        while (formsParticipantes.size() > ingressos.size()) {
            formsParticipantes.remove(formsParticipantes.size() - 1);
        }

        vboxParticipantes.getChildren().clear();
        for (int i = 0; i < ingressos.size(); i++) {
            FormParticipante form = formsParticipantes.get(i);

            Label titulo = new Label("Ingresso " + (i + 1) + " de " + ingressos.size() + " — " + ingressos.get(i).descricao());
            titulo.getStyleClass().add("field-label");
            titulo.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(titulo, Priority.ALWAYS);
            HBox cabecalho = new HBox(12, titulo);
            cabecalho.setAlignment(Pos.CENTER_LEFT);
            if (i > 0) {
                FormParticipante anterior = formsParticipantes.get(i - 1);
                Button btnCopiar = new Button("Repetir dados do anterior");
                btnCopiar.getStyleClass().add("btn-secondary");
                btnCopiar.setOnAction(e -> form.copiarDe(anterior));
                cabecalho.getChildren().add(btnCopiar);
            }

            HBox linha1 = new HBox(16, campo("CPF", form.txtCpf), campo("Nome Completo", form.txtNome));
            HBox linha2 = new HBox(16, campo("E-mail", form.txtEmail), campo("Telefone", form.txtTelefone));

            VBox bloco = new VBox(12, cabecalho, linha1, linha2);
            bloco.getStyleClass().add("summary-panel");
            vboxParticipantes.getChildren().add(bloco);
        }
    }

    private VBox campo(String rotulo, TextField campo) {
        Label label = new Label(rotulo);
        label.getStyleClass().add("field-label");
        VBox box = new VBox(6, label, campo);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    @FXML
    public void avancarParaStep3() {
        for (int i = 0; i < formsParticipantes.size(); i++) {
            if (!validarParticipante(formsParticipantes.get(i), i + 1)) return;
        }
        mostrarStep(3);
    }

    private boolean validarParticipante(FormParticipante form, int numeroIngresso) {
        String prefixo = formsParticipantes.size() > 1 ? "Ingresso " + numeroIngresso + ": " : "";
        String cpf = form.cpf();
        String nome = form.nome();
        String email = form.email();
        String tel = form.telefone();

        if (cpf.isEmpty()) {
            return invalido(prefixo + "O campo CPF é obrigatório.", form.txtCpf);
        }
        if (!ValidacaoUtil.isCpfValido(cpf)) {
            return invalido(prefixo + "O CPF informado é inválido. Digite um CPF válido com 11 dígitos.", form.txtCpf);
        }
        if (nome.isEmpty() || nome.length() < 3) {
            return invalido(prefixo + "O campo Nome Completo é obrigatório e deve ter no mínimo 3 caracteres.", form.txtNome);
        }
        if (email.isEmpty()) {
            return invalido(prefixo + "O campo E-mail (Gmail/correio eletrônico) é obrigatório.", form.txtEmail);
        }
        if (!ValidacaoUtil.isEmailValido(email)) {
            return invalido(prefixo + "O E-mail informado é inválido. Exemplo: usuario@email.com", form.txtEmail);
        }
        if (tel.isEmpty()) {
            return invalido(prefixo + "O campo Telefone é obrigatório.", form.txtTelefone);
        }
        if (tel.length() > 20) {
            return invalido(prefixo + "O telefone deve ter no máximo 20 caracteres.", form.txtTelefone);
        }
        if (!tel.matches("\\d+")) {
            return invalido(prefixo + "O telefone deve conter apenas dígitos.", form.txtTelefone);
        }
        return true;
    }

    private boolean invalido(String mensagem, TextField campo) {
        AlertUtil.exibirAviso(mensagem);
        campo.requestFocus();
        return false;
    }

    @FXML
    public void confirmarPagamento() {
        try {
            List<ItemCarrinho> ingressos = ingressosDoCarrinho();
            if (ingressos.isEmpty() || ingressos.size() != formsParticipantes.size()) {
                AlertUtil.exibirAviso("Os ingressos da venda mudaram. Revise os titulares antes de confirmar.");
                avancarParaStep2();
                return;
            }
            for (int i = 0; i < formsParticipantes.size(); i++) {
                if (!validarParticipante(formsParticipantes.get(i), i + 1)) {
                    mostrarStep(2);
                    return;
                }
            }

            List<ItemVendaDTO> itens = new ArrayList<>();
            List<Participante> titulares = new ArrayList<>();
            for (int i = 0; i < ingressos.size(); i++) {
                FormParticipante form = formsParticipantes.get(i);
                Participante participante = participanteService.cadastrarOuAtualizar(
                        new ParticipanteDTO(form.nome(), form.cpf(), form.telefone(), form.email())
                );
                titulares.add(participante);

                Lote lote = ingressos.get(i).lote;
                ItemVendaDTO item = new ItemVendaDTO();
                item.setLoteId(lote.getId());
                item.setParticipanteId(participante.getId());
                item.setQuantidade(1);
                item.setPrecoUnitario(lote.getPreco());
                itens.add(item);
            }

            Long usuarioId = SessaoUsuario.getInstancia().getUsuarioLogado() != null
                    ? SessaoUsuario.getInstancia().getUsuarioLogado().getId() : 1L;

            Evento evento = eventoDoCarrinho;

            VendaDTO vendaDTO = new VendaDTO();
            vendaDTO.setUsuarioId(usuarioId);
            vendaDTO.setEventoId(evento.getId());
            vendaDTO.setFormaPagamento(formaPagamentoSelecionada);
            vendaDTO.setItens(itens);

            this.vendaConcluida = vendaService.registrarVenda(vendaDTO);

            montarComprovantes(evento, ingressos, titulares);
            mostrarStep(4);

        } catch (Exception e) {
            AlertUtil.exibirErro("Erro ao registrar venda: " + e.getMessage());
        }
    }

    /** Monta um cartão de comprovante por ingresso vendido (a venda devolve os ingressos na ordem dos itens). */
    private void montarComprovantes(Evento evento, List<ItemCarrinho> ingressosCarrinho, List<Participante> titulares) {
        String dataStr = evento.getDataEvento() != null ? evento.getDataEvento().format(dateFormatter) : "";
        String horaStr = evento.getHorario() != null ? evento.getHorario().format(timeFormatter) : "20:00";
        String localStr = evento.getLocal() != null ? evento.getLocal() : "Local do Evento";
        String detalhes = dataStr + " · " + horaStr + " · " + localStr;

        List<Ingresso> ingressosVendidos = vendaConcluida.getIngressos();
        int total = ingressosVendidos.size();
        lblVendaConcluida.setText("✓ Venda concluída — " + total + (total == 1 ? " ingresso" : " ingressos") +
                " · " + moedaFormat.format(vendaConcluida.getValorTotal()));

        flowComprovantes.getChildren().clear();
        statusComprovantes.clear();
        for (int i = 0; i < total; i++) {
            Ingresso ingresso = ingressosVendidos.get(i);

            Label lblNome = new Label(evento.getNome());
            lblNome.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");
            Label lblDetalhes = new Label(detalhes);
            lblDetalhes.getStyleClass().add("subtitle-light");

            Label lblStatus = new Label("Ativo");
            lblStatus.getStyleClass().setAll("badge-ativo");
            statusComprovantes.add(lblStatus);

            VBox card = new VBox(14,
                    lblNome,
                    lblDetalhes,
                    new Separator(),
                    linhaComprovante("Participante", titulares.get(i).getNome()),
                    linhaComprovante("Setor", ingressosCarrinho.get(i).descricao()),
                    linhaComprovante("Identificador", ingresso.getCodigo().toString()),
                    linhaComprovante("Status", lblStatus));
            card.getStyleClass().add("ticket-card");
            card.setPrefWidth(480);
            card.setMaxWidth(480);
            flowComprovantes.getChildren().add(card);
        }
    }

    private HBox linhaComprovante(String rotulo, String valor) {
        Label lblValor = new Label(valor);
        lblValor.getStyleClass().add("field-label");
        return linhaComprovante(rotulo, lblValor);
    }

    private HBox linhaComprovante(String rotulo, Label valor) {
        Label lblRotulo = new Label(rotulo);
        lblRotulo.getStyleClass().add("subtitle-light");
        lblRotulo.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(lblRotulo, Priority.ALWAYS);
        HBox linha = new HBox(lblRotulo, valor);
        linha.setAlignment(Pos.CENTER_LEFT);
        return linha;
    }

    private List<UUID> codigosVendidos() {
        return vendaConcluida.getIngressos().stream().map(Ingresso::getCodigo).toList();
    }

    private void marcarComprovantesComoEmitidos() throws SQLException {
        for (UUID codigo : codigosVendidos()) {
            ingressoService.marcarComoEmitido(codigo);
        }
        for (Label lblStatus : statusComprovantes) {
            lblStatus.setText("Emitido / Não Utilizado");
            lblStatus.getStyleClass().setAll("badge-ativo");
        }
    }

    @FXML
    public void imprimirComprovante() {
        if (vendaConcluida == null || vendaConcluida.getIngressos().isEmpty()) {
            AlertUtil.exibirSucesso("Comprovante enviado para a fila de impressão do sistema.");
            return;
        }

        try {
            boolean impresso = ingressoService.imprimirIngressosLocal(codigosVendidos(), formaPagamentoSelecionada);

            if (impresso) {
                AlertUtil.exibirSucesso(vendaConcluida.getIngressos().size() > 1
                        ? "Ingressos enviados para a impressora com sucesso!"
                        : "Ingresso enviado para a impressora com sucesso!");
            } else {
                AlertUtil.exibirAviso("Nenhuma impressora detectada no sistema.");
            }

            marcarComprovantesComoEmitidos();

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
                ingressoService.salvarComprovantePDF(codigosVendidos(), formaPagamentoSelecionada, file.toPath());
                marcarComprovantesComoEmitidos();
                AlertUtil.exibirSucesso("Comprovante salvo com sucesso em: " + file.getAbsolutePath());
            } catch (Exception e) {
                AlertUtil.exibirErro("Erro ao salvar PDF: " + e.getMessage());
            }
        }
    }

    @FXML
    public void reiniciarFluxoVenda() {
        carrinho.clear();
        eventoDoCarrinho = null;
        formsParticipantes.clear();
        vboxParticipantes.getChildren().clear();
        vendaConcluida = null;
        formaPagamentoSelecionada = FormaPagamento.CARTAO_CREDITO;
        atualizarCarrinho();
        carregarEventosAbertos();
        mostrarStep(1);
    }
}
