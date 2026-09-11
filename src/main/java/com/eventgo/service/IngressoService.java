package com.eventgo.service;

import com.eventgo.config.DatabaseConfig;
import com.eventgo.dao.IngressoDAO;
import com.eventgo.dao.LoteDAO;
import com.eventgo.model.Ingresso;
import com.eventgo.model.enums.StatusIngresso;
import com.eventgo.util.ValidacaoUtil;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class IngressoService {

    private final IngressoDAO ingressoDAO;
    private final LoteDAO loteDAO;

    public IngressoService() {
        this.ingressoDAO = new IngressoDAO();
        this.loteDAO = new LoteDAO();
    }

    public IngressoService(IngressoDAO ingressoDAO, LoteDAO loteDAO) {
        this.ingressoDAO = ingressoDAO;
        this.loteDAO = loteDAO;
    }

    public Optional<Ingresso> buscarPorCodigo(UUID codigo) throws SQLException {
        if (codigo == null) return Optional.empty();
        return ingressoDAO.buscarPorCodigo(codigo);
    }

    public List<Ingresso> listarPorVenda(Long vendaId) throws SQLException {
        if (vendaId == null) throw new IllegalArgumentException("ID da venda é obrigatório.");
        return ingressoDAO.listarPorVenda(vendaId);
    }

    public void cancelarIngresso(UUID codigo, Long usuarioId, String motivo) throws SQLException {
        if (codigo == null) throw new IllegalArgumentException("Código do ingresso é obrigatório.");
        if (usuarioId == null) throw new IllegalArgumentException("Usuário responsável pelo cancelamento é obrigatório.");
        if (!ValidacaoUtil.isPreenchido(motivo)) throw new IllegalArgumentException("O motivo do cancelamento é obrigatório.");

        Ingresso ingresso = ingressoDAO.buscarPorCodigo(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Ingresso não encontrado com o código fornecido."));

        if (ingresso.getStatus() == StatusIngresso.CANCELADO) {
            throw new IllegalStateException("Este ingresso já foi cancelado anteriormente.");
        }
        if (ingresso.getStatus() == StatusIngresso.UTILIZADO) {
            throw new IllegalStateException("Não é permitido cancelar um ingresso que já foi utilizado na entrada.");
        }

        // RN-05: Transação para cancelar o ingresso e devolver a quantidade ao estoque do lote
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                ingressoDAO.cancelar(conn, codigo, usuarioId, motivo.trim());
                loteDAO.incrementarEstoque(conn, ingresso.getLoteId(), 1);

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void registrarCheckin(UUID codigo) throws SQLException {
        if (codigo == null) throw new IllegalArgumentException("Código do ingresso é obrigatório.");

        Ingresso ingresso = ingressoDAO.buscarPorCodigo(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Ingresso não encontrado."));

        if (ingresso.getStatus() == StatusIngresso.CANCELADO) {
            throw new IllegalStateException("ACESSO NEGADO: Este ingresso está CANCELADO.");
        }
        if (ingresso.getStatus() == StatusIngresso.UTILIZADO) {
            throw new IllegalStateException("ACESSO NEGADO: Ingresso já UTILIZADO em " + ingresso.getDataCheckin());
        }

        // RN-06
        ingressoDAO.registrarCheckin(codigo);
    }

    public byte[] emitirComprovantePDF(UUID codigo) throws Exception {
        Ingresso ingresso = ingressoDAO.buscarPorCodigo(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Ingresso não encontrado para emissão."));

        return ComprovantePDFService.gerarComprovantePDF(ingresso);
    }

    public void salvarComprovantePDF(UUID codigo, Path caminhoDestino) throws Exception {
        Ingresso ingresso = ingressoDAO.buscarPorCodigo(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Ingresso não encontrado para emissão."));

        ComprovantePDFService.salvarComprovanteEmArquivo(ingresso, caminhoDestino);
    }
}
