package com.eventgo.dao;

import com.eventgo.config.DatabaseConfig;
import com.eventgo.model.Lote;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoteDAO {

    public Lote inserir(Lote lote) throws SQLException {
        String sql = "INSERT INTO eventgo.lote (tipo_ingresso_id, numero_lote, preco, quantidade_total, " +
                     "quantidade_disponivel, data_inicio, data_fim, ativo, criado_em, atualizado_em) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, lote.getTipoIngressoId());
            stmt.setInt(2, lote.getNumeroLote());
            stmt.setBigDecimal(3, lote.getPreco());
            stmt.setInt(4, lote.getQuantidadeTotal());
            stmt.setInt(5, lote.getQuantidadeDisponivel());
            stmt.setDate(6, Date.valueOf(lote.getDataInicio()));
            stmt.setDate(7, Date.valueOf(lote.getDataFim()));
            stmt.setBoolean(8, lote.isAtivo());
            stmt.setTimestamp(9, Timestamp.valueOf(lote.getCriadoEm()));
            stmt.setTimestamp(10, Timestamp.valueOf(lote.getAtualizadoEm()));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    lote.setId(rs.getLong("id"));
                }
            }
            return lote;
        }
    }

    public void atualizar(Lote lote) throws SQLException {
        String sql = "UPDATE eventgo.lote SET numero_lote = ?, preco = ?, quantidade_total = ?, " +
                     "data_inicio = ?, data_fim = ?, ativo = ?, atualizado_em = NOW() WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, lote.getNumeroLote());
            stmt.setBigDecimal(2, lote.getPreco());
            stmt.setInt(3, lote.getQuantidadeTotal());
            stmt.setDate(4, Date.valueOf(lote.getDataInicio()));
            stmt.setDate(5, Date.valueOf(lote.getDataFim()));
            stmt.setBoolean(6, lote.isAtivo());
            stmt.setLong(7, lote.getId());

            stmt.executeUpdate();
        }
    }

    public void ativarDesativar(Long id, boolean ativo) throws SQLException {
        String sql = "UPDATE eventgo.lote SET ativo = ?, atualizado_em = NOW() WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, ativo);
            stmt.setLong(2, id);

            stmt.executeUpdate();
        }
    }

    public Optional<Lote> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT id, tipo_ingresso_id, numero_lote, preco, quantidade_total, " +
                     "quantidade_disponivel, data_inicio, data_fim, ativo, criado_em, atualizado_em " +
                     "FROM eventgo.lote WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearLote(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Lote> listarPorTipoIngresso(Long tipoIngressoId) throws SQLException {
        List<Lote> lista = new ArrayList<>();
        String sql = "SELECT id, tipo_ingresso_id, numero_lote, preco, quantidade_total, " +
                     "quantidade_disponivel, data_inicio, data_fim, ativo, criado_em, atualizado_em " +
                     "FROM eventgo.lote WHERE tipo_ingresso_id = ? ORDER BY numero_lote ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, tipoIngressoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearLote(rs));
                }
            }
        }
        return lista;
    }

    public List<Lote> buscarDisponiveisPorTipo(Long tipoIngressoId) throws SQLException {
        List<Lote> lista = new ArrayList<>();
        String sql = "SELECT id, tipo_ingresso_id, numero_lote, preco, quantidade_total, " +
                     "quantidade_disponivel, data_inicio, data_fim, ativo, criado_em, atualizado_em " +
                     "FROM eventgo.lote WHERE tipo_ingresso_id = ? AND ativo = true " +
                     "AND quantidade_disponivel > 0 AND (data_fim >= CURRENT_DATE OR data_fim IS NULL) " +
                     "ORDER BY numero_lote ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, tipoIngressoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearLote(rs));
                }
            }
        }
        return lista;
    }

    public int somarQuantidadeTotalPorTipo(Long tipoIngressoId, Long excetoLoteId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantidade_total), 0) AS total FROM eventgo.lote WHERE tipo_ingresso_id = ?" +
                     (excetoLoteId != null ? " AND id <> ?" : "");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, tipoIngressoId);
            if (excetoLoteId != null) {
                stmt.setLong(2, excetoLoteId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    public void decrementarEstoque(Connection conn, Long loteId, int quantidade) throws SQLException {
        String sql = "UPDATE eventgo.lote SET quantidade_disponivel = quantidade_disponivel - ?, " +
                     "atualizado_em = NOW() WHERE id = ? AND quantidade_disponivel >= ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantidade);
            stmt.setLong(2, loteId);
            stmt.setInt(3, quantidade);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Estoque insuficiente ou lote inválido para o lote ID: " + loteId);
            }
        }
    }

    public void incrementarEstoque(Connection conn, Long loteId, int quantidade) throws SQLException {
        String sql = "UPDATE eventgo.lote SET quantidade_disponivel = LEAST(quantidade_total, quantidade_disponivel + ?), " +
                     "atualizado_em = NOW() WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantidade);
            stmt.setLong(2, loteId);

            stmt.executeUpdate();
        }
    }

    private Lote mapearLote(ResultSet rs) throws SQLException {
        Lote l = new Lote();
        l.setId(rs.getLong("id"));
        l.setTipoIngressoId(rs.getLong("tipo_ingresso_id"));
        l.setNumeroLote(rs.getInt("numero_lote"));
        l.setPreco(rs.getBigDecimal("preco"));
        l.setQuantidadeTotal(rs.getInt("quantidade_total"));
        l.setQuantidadeDisponivel(rs.getInt("quantidade_disponivel"));
        l.setDataInicio(rs.getDate("data_inicio").toLocalDate());
        l.setDataFim(rs.getDate("data_fim").toLocalDate());
        l.setAtivo(rs.getBoolean("ativo"));
        l.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        l.setAtualizadoEm(rs.getTimestamp("atualizado_em").toLocalDateTime());
        return l;
    }
}
