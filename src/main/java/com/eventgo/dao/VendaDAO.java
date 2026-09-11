package com.eventgo.dao;

import com.eventgo.config.DatabaseConfig;
import com.eventgo.model.Venda;
import com.eventgo.model.enums.FormaPagamento;
import com.eventgo.model.enums.StatusVenda;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VendaDAO {

    public Venda inserir(Connection conn, Venda venda) throws SQLException {
        String sql = "INSERT INTO eventgo.venda (usuario_id, evento_id, forma_pagamento, valor_total, status, data_venda, criado_em) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, venda.getUsuarioId());
            stmt.setLong(2, venda.getEventoId());
            stmt.setString(3, venda.getFormaPagamento().name());
            stmt.setBigDecimal(4, venda.getValorTotal());
            stmt.setString(5, venda.getStatus().name());
            stmt.setTimestamp(6, Timestamp.valueOf(venda.getDataVenda()));
            stmt.setTimestamp(7, Timestamp.valueOf(venda.getCriadoEm()));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    venda.setId(rs.getLong("id"));
                }
            }
            return venda;
        }
    }

    public Optional<Venda> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT id, usuario_id, evento_id, forma_pagamento, valor_total, status, data_venda, criado_em " +
                     "FROM eventgo.venda WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearVenda(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Venda> listarPorEvento(Long eventoId) throws SQLException {
        List<Venda> lista = new ArrayList<>();
        String sql = "SELECT id, usuario_id, evento_id, forma_pagamento, valor_total, status, data_venda, criado_em " +
                     "FROM eventgo.venda WHERE evento_id = ? ORDER BY data_venda DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, eventoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearVenda(rs));
                }
            }
        }
        return lista;
    }

    private Venda mapearVenda(ResultSet rs) throws SQLException {
        Venda v = new Venda();
        v.setId(rs.getLong("id"));
        v.setUsuarioId(rs.getLong("usuario_id"));
        v.setEventoId(rs.getLong("evento_id"));
        v.setFormaPagamento(FormaPagamento.valueOf(rs.getString("forma_pagamento")));
        v.setValorTotal(rs.getBigDecimal("valor_total"));
        v.setStatus(StatusVenda.valueOf(rs.getString("status")));
        v.setDataVenda(rs.getTimestamp("data_venda").toLocalDateTime());
        v.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        return v;
    }
}
