package com.eventgo.service;

import com.eventgo.config.DatabaseConfig;
import com.eventgo.dao.EventoDAO;
import com.eventgo.dao.IngressoDAO;
import com.eventgo.dao.LoteDAO;
import com.eventgo.dao.VendaDAO;
import com.eventgo.dto.ItemVendaDTO;
import com.eventgo.dto.VendaDTO;
import com.eventgo.model.Evento;
import com.eventgo.model.Ingresso;
import com.eventgo.model.Lote;
import com.eventgo.model.Venda;
import com.eventgo.model.enums.SituacaoEvento;
import com.eventgo.model.enums.StatusVenda;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VendaService {

    private final VendaDAO vendaDAO;
    private final IngressoDAO ingressoDAO;
    private final LoteDAO loteDAO;
    private final EventoDAO eventoDAO;

    public VendaService() {
        this.vendaDAO = new VendaDAO();
        this.ingressoDAO = new IngressoDAO();
        this.loteDAO = new LoteDAO();
        this.eventoDAO = new EventoDAO();
    }

    public VendaService(VendaDAO vendaDAO, IngressoDAO ingressoDAO, LoteDAO loteDAO, EventoDAO eventoDAO) {
        this.vendaDAO = vendaDAO;
        this.ingressoDAO = ingressoDAO;
        this.loteDAO = loteDAO;
        this.eventoDAO = eventoDAO;
    }

    public Venda registrarVenda(VendaDTO dto) throws SQLException {
        validarVendaDTO(dto);

        for (ItemVendaDTO item : dto.getItens()) {
            if (item.getLoteId() == null) {
                throw new IllegalArgumentException("Todo item da venda deve ter um lote.");
            }
            if (item.getQuantidade() < 1) {
                throw new IllegalArgumentException("A quantidade de cada item deve ser de pelo menos 1 ingresso.");
            }
        }

        // RN-03: Evento deve estar com situação ABERTO
        Evento evento = eventoDAO.buscarPorId(dto.getEventoId())
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado."));

        if (evento.getSituacao() != SituacaoEvento.ABERTO) {
            throw new IllegalStateException("Vendas só são permitidas para eventos com situação 'ABERTO'. Situação atual: " + evento.getSituacao().getDescricao());
        }

        BigDecimal valorTotalVenda = BigDecimal.ZERO;
        List<Ingresso> ingressosParaCriar = new ArrayList<>();
        // Quantidade acumulada por lote, pois vários itens podem usar o mesmo lote
        Map<Long, Integer> solicitadoPorLote = new HashMap<>();

        // Abre conexão e inicia transação
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 1. Processa cada item e decrementa estoque
                for (ItemVendaDTO item : dto.getItens()) {
                    Lote lote = loteDAO.buscarPorId(item.getLoteId())
                            .orElseThrow(() -> new IllegalArgumentException("Lote ID " + item.getLoteId() + " não encontrado."));

                    // RN-07: Validação de vigência e disponibilidade
                    if (!lote.isVigente()) {
                        throw new IllegalStateException("O lote #" + lote.getNumeroLote() + " não está vigente ou não possui estoque suficiente.");
                    }
                    int solicitado = solicitadoPorLote.merge(lote.getId(), item.getQuantidade(), Integer::sum);
                    if (lote.getQuantidadeDisponivel() < solicitado) {
                        throw new IllegalStateException("Estoque insuficiente para o lote #" + lote.getNumeroLote() +
                                ". Disponível: " + lote.getQuantidadeDisponivel() + ", Solicitado: " + solicitado);
                    }

                    // Decrementa estoque atomicamente (RN-04)
                    loteDAO.decrementarEstoque(conn, lote.getId(), item.getQuantidade());

                    // Cria os ingressos individuais
                    for (int i = 0; i < item.getQuantidade(); i++) {
                        Ingresso ing = new Ingresso();
                        ing.setLoteId(lote.getId());
                        ing.setParticipanteId(item.getParticipanteId());
                        ing.setPrecoPago(lote.getPreco());
                        ingressosParaCriar.add(ing);
                    }

                    BigDecimal subtotal = lote.getPreco().multiply(BigDecimal.valueOf(item.getQuantidade()));
                    valorTotalVenda = valorTotalVenda.add(subtotal);
                }

                // 2. Cria o registro da Venda
                Venda venda = new Venda();
                venda.setUsuarioId(dto.getUsuarioId());
                venda.setEventoId(dto.getEventoId());
                venda.setFormaPagamento(dto.getFormaPagamento());
                venda.setValorTotal(valorTotalVenda);
                venda.setStatus(StatusVenda.CONFIRMADA);

                venda = vendaDAO.inserir(conn, venda);

                // 3. Vincula vendaId aos ingressos e persiste em lote
                for (Ingresso ing : ingressosParaCriar) {
                    ing.setVendaId(venda.getId());
                }
                ingressoDAO.inserirBatch(conn, ingressosParaCriar);

                // Confirma a transação
                conn.commit();

                venda.setIngressos(ingressosParaCriar);
                return venda;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<Venda> listarPorEvento(Long eventoId) throws SQLException {
        if (eventoId == null) throw new IllegalArgumentException("ID do evento é obrigatório.");
        return vendaDAO.listarPorEvento(eventoId);
    }

    private void validarVendaDTO(VendaDTO dto) {
        if (dto.getUsuarioId() == null) {
            throw new IllegalArgumentException("Usuário operador da venda é obrigatório.");
        }
        if (dto.getEventoId() == null) {
            throw new IllegalArgumentException("Evento é obrigatório.");
        }
        if (dto.getFormaPagamento() == null) {
            throw new IllegalArgumentException("Forma de pagamento presencial é obrigatória.");
        }
        if (dto.getItens() == null || dto.getItens().isEmpty()) {
            throw new IllegalArgumentException("A venda deve conter pelo menos um item de ingresso.");
        }
    }
}
