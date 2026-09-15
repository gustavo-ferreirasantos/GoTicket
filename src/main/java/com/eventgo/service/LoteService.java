package com.eventgo.service;

import com.eventgo.dao.LoteDAO;
import com.eventgo.dao.SetorDAO;
import com.eventgo.dao.TipoIngressoDAO;
import com.eventgo.dto.LoteDTO;
import com.eventgo.model.Lote;
import com.eventgo.model.Setor;
import com.eventgo.model.TipoIngresso;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class LoteService {

    private final LoteDAO loteDAO;
    private final TipoIngressoDAO tipoIngressoDAO;
    private final SetorDAO setorDAO;

    public LoteService() {
        this.loteDAO = new LoteDAO();
        this.tipoIngressoDAO = new TipoIngressoDAO();
        this.setorDAO = new SetorDAO();
    }

    public LoteService(LoteDAO loteDAO, TipoIngressoDAO tipoIngressoDAO, SetorDAO setorDAO) {
        this.loteDAO = loteDAO;
        this.tipoIngressoDAO = tipoIngressoDAO;
        this.setorDAO = setorDAO;
    }

    public Lote cadastrar(LoteDTO dto) throws SQLException {
        validarCampos(dto);

        TipoIngresso tipo = tipoIngressoDAO.buscarPorId(dto.getTipoIngressoId())
                .orElseThrow(() -> new IllegalArgumentException("Tipo de ingresso não encontrado."));

        Setor setor = setorDAO.buscarPorId(tipo.getSetorId())
                .orElseThrow(() -> new IllegalArgumentException("Setor não encontrado."));

        // RN-02: Validação da quantidade total em relação à capacidade do setor
        int qtdExistente = loteDAO.somarQuantidadeTotalPorTipo(dto.getTipoIngressoId(), null);
        if (qtdExistente + dto.getQuantidadeTotal() > setor.getCapacidade()) {
            int restante = setor.getCapacidade() - qtdExistente;
            throw new IllegalArgumentException(
                    "A quantidade de ingressos deste lote excede a capacidade do setor (" + setor.getCapacidade() +
                    "). Quantidade restante disponível para lotes: " + Math.max(0, restante)
            );
        }

        Lote lote = new Lote();
        lote.setTipoIngressoId(dto.getTipoIngressoId());
        lote.setNumeroLote(dto.getNumeroLote());
        lote.setPreco(dto.getPreco());
        lote.setQuantidadeTotal(dto.getQuantidadeTotal());
        lote.setQuantidadeDisponivel(dto.getQuantidadeTotal()); // Inicialmente igual ao total
        lote.setDataInicio(dto.getDataInicio());
        lote.setDataFim(dto.getDataFim());
        lote.setAtivo(dto.isAtivo());

        return loteDAO.inserir(lote);
    }

    public void cadastrar(Connection conn, Long tipoIngressoId, BigDecimal preco, int quantidade,
                          LocalDate dataInicio, LocalDate dataFim) throws SQLException {
        if (preco == null || preco.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O preço do lote não pode ser negativo.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade total do lote deve ser maior que zero.");
        }
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("As datas de início e fim da vigência são obrigatórias.");
        }
        if (dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("A data final da vigência não pode ser anterior à data inicial.");
        }

        TipoIngresso tipo = tipoIngressoDAO.buscarPorId(conn, tipoIngressoId)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de ingresso não encontrado."));
        Setor setor = setorDAO.buscarPorId(conn, tipo.getSetorId())
                .orElseThrow(() -> new IllegalArgumentException("Setor não encontrado."));

        int qtdExistente = loteDAO.somarQuantidadeTotalPorTipo(conn, tipoIngressoId, null);
        if (qtdExistente + quantidade > setor.getCapacidade()) {
            int restante = setor.getCapacidade() - qtdExistente;
            throw new IllegalArgumentException(
                    "A quantidade de ingressos deste lote excede a capacidade do setor."
            );
        }

        Lote lote = new Lote();
        lote.setTipoIngressoId(tipoIngressoId);
        lote.setNumeroLote(1);
        lote.setPreco(preco);
        lote.setQuantidadeTotal(quantidade);
        lote.setQuantidadeDisponivel(quantidade);
        lote.setDataInicio(dataInicio);
        lote.setDataFim(dataFim);
        lote.setAtivo(true);

        loteDAO.inserir(conn, lote);
    }

    public void atualizar(LoteDTO dto) throws SQLException {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("ID do lote é obrigatório para atualização.");
        }
        validarCampos(dto);

        Lote loteAtual = loteDAO.buscarPorId(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Lote não encontrado."));

        TipoIngresso tipo = tipoIngressoDAO.buscarPorId(loteAtual.getTipoIngressoId())
                .orElseThrow(() -> new IllegalArgumentException("Tipo de ingresso não encontrado."));

        Setor setor = setorDAO.buscarPorId(tipo.getSetorId())
                .orElseThrow(() -> new IllegalArgumentException("Setor não encontrado."));

        int vendidos = loteAtual.getQuantidadeTotal() - loteAtual.getQuantidadeDisponivel();
        if (dto.getQuantidadeTotal() < vendidos) {
            throw new IllegalArgumentException(
                    "A nova quantidade total (" + dto.getQuantidadeTotal() + ") não pode ser menor que os ingressos já vendidos (" + vendidos + ")."
            );
        }

        // RN-02
        int qtdExistente = loteDAO.somarQuantidadeTotalPorTipo(loteAtual.getTipoIngressoId(), dto.getId());
        if (qtdExistente + dto.getQuantidadeTotal() > setor.getCapacidade()) {
            int restante = setor.getCapacidade() - qtdExistente;
            throw new IllegalArgumentException(
                    "A quantidade deste lote excede a capacidade do setor (" + setor.getCapacidade() +
                    "). Limite restante: " + Math.max(0, restante)
            );
        }

        loteAtual.setNumeroLote(dto.getNumeroLote());
        loteAtual.setPreco(dto.getPreco());
        loteAtual.setQuantidadeTotal(dto.getQuantidadeTotal());
        loteAtual.setDataInicio(dto.getDataInicio());
        loteAtual.setDataFim(dto.getDataFim());
        loteAtual.setAtivo(dto.isAtivo());

        loteDAO.atualizar(loteAtual);
    }

    public void ativarDesativar(Long id, boolean ativo) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("ID inválido.");
        }
        loteDAO.ativarDesativar(id, ativo);
    }

    public List<Lote> listarPorTipoIngresso(Long tipoIngressoId) throws SQLException {
        if (tipoIngressoId == null) {
            throw new IllegalArgumentException("ID do tipo de ingresso é obrigatório.");
        }
        return loteDAO.listarPorTipoIngresso(tipoIngressoId);
    }

    public List<Lote> buscarDisponiveis(Long tipoIngressoId) throws SQLException {
        if (tipoIngressoId == null) {
            throw new IllegalArgumentException("ID do tipo de ingresso é obrigatório.");
        }
        return loteDAO.buscarDisponiveisPorTipo(tipoIngressoId);
    }

    public Optional<Lote> buscarPorId(Long id) throws SQLException {
        if (id == null) return Optional.empty();
        return loteDAO.buscarPorId(id);
    }

    private void validarCampos(LoteDTO dto) {
        if (dto.getTipoIngressoId() == null && dto.getId() == null) {
            throw new IllegalArgumentException("O tipo de ingresso vinculado é obrigatório.");
        }
        if (dto.getNumeroLote() == null || dto.getNumeroLote() <= 0) {
            throw new IllegalArgumentException("O número do lote deve ser maior que zero.");
        }
        if (dto.getPreco() == null || dto.getPreco().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O preço do lote não pode ser negativo.");
        }
        if (dto.getQuantidadeTotal() == null || dto.getQuantidadeTotal() <= 0) {
            throw new IllegalArgumentException("A quantidade total do lote deve ser maior que zero.");
        }
        if (dto.getDataInicio() == null || dto.getDataFim() == null) {
            throw new IllegalArgumentException("As datas de início e fim da vigência são obrigatórias.");
        }
        if (dto.getDataFim().isBefore(dto.getDataInicio())) {
            throw new IllegalArgumentException("A data final da vigência não pode ser anterior à data inicial.");
        }
    }
}
