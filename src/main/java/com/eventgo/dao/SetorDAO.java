package com.eventgo.dao;

import com.eventgo.config.DatabaseConfig;
import com.eventgo.model.Setor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SetorDAO {

    public Setor inserir(Setor setor) throws SQLException {
        String sql = "INSERT INTO eventgo.setor (evento_id, nome, capacidade, criado_em) " +
                     "VALUES (?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, setor.getEventoId());
            stmt.setString(2, setor.getNome());
            stmt.setInt(3, setor.getCapacidade());
            stmt.setTimestamp(4, Timestamp.valueOf(setor.getCriadoEm()));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    setor.setId(rs.getLong("id"));
                }
            }
            return setor;
        }
    }

    public void atualizar(Setor setor) throws SQLException {
        String sql = "UPDATE eventgo.setor SET nome = ?, capacidade = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, setor.getNome());
            stmt.setInt(2, setor.getCapacidade());
            stmt.setLong(3, setor.getId());

            stmt.executeUpdate();
        }
    }

    public void remover(Long id) throws SQLException {
        String sql = "DELETE FROM eventgo.setor WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    public Optional<Setor> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT id, evento_id, nome, capacidade, criado_em FROM eventgo.setor WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearSetor(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Setor> listarPorEvento(Long eventoId) throws SQLException {
        List<Setor> lista = new ArrayList<>();
        String sql = "SELECT id, evento_id, nome, capacidade, criado_em FROM eventgo.setor " +
                     "WHERE evento_id = ? ORDER BY nome ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, eventoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearSetor(rs));
                }
            }
        }
        return lista;
    }

    public int somarCapacidadePorEvento(Long eventoId, Long excetoSetorId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(capacidade), 0) AS total FROM eventgo.setor WHERE evento_id = ?" +
                     (excetoSetorId != null ? " AND id <> ?" : "");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, eventoId);
            if (excetoSetorId != null) {
                stmt.setLong(2, excetoSetorId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    private Setor mapearSetor(ResultSet rs) throws SQLException {
        Setor s = new Setor();
        s.setId(rs.getLong("id"));
        s.setEventoId(rs.getLong("evento_id"));
        s.setNome(rs.getString("nome"));
        s.setCapacidade(rs.getInt("capacidade"));
        s.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        return s;
    }
}
