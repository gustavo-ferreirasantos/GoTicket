package com.eventgo.dao;

import com.eventgo.config.DatabaseConfig;
import com.eventgo.model.Participante;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ParticipanteDAO {

    public Participante inserir(Participante participante) throws SQLException {
        String sql = "INSERT INTO eventgo.participante (nome, cpf, telefone, email, criado_em, atualizado_em) " +
                     "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, participante.getNome());
            stmt.setString(2, participante.getCpf());
            stmt.setString(3, participante.getTelefone());
            stmt.setString(4, participante.getEmail());
            stmt.setTimestamp(5, Timestamp.valueOf(participante.getCriadoEm()));
            stmt.setTimestamp(6, Timestamp.valueOf(participante.getAtualizadoEm()));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    participante.setId(rs.getLong("id"));
                }
            }
            return participante;
        }
    }

    public void atualizar(Participante participante) throws SQLException {
        String sql = "UPDATE eventgo.participante SET nome = ?, cpf = ?, telefone = ?, email = ?, atualizado_em = NOW() " +
                     "WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, participante.getNome());
            stmt.setString(2, participante.getCpf());
            stmt.setString(3, participante.getTelefone());
            stmt.setString(4, participante.getEmail());
            stmt.setLong(5, participante.getId());

            stmt.executeUpdate();
        }
    }

    public Optional<Participante> buscarPorCpf(String cpf) throws SQLException {
        String sql = "SELECT id, nome, cpf, telefone, email, criado_em, atualizado_em FROM eventgo.participante " +
                     "WHERE cpf = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cpf);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearParticipante(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Participante> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT id, nome, cpf, telefone, email, criado_em, atualizado_em FROM eventgo.participante " +
                     "WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearParticipante(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Participante> buscarPorNome(String nome) throws SQLException {
        List<Participante> lista = new ArrayList<>();
        String sql = "SELECT id, nome, cpf, telefone, email, criado_em, atualizado_em FROM eventgo.participante " +
                     "WHERE LOWER(nome) LIKE LOWER(?) ORDER BY nome ASC LIMIT 50";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + (nome != null ? nome.trim() : "") + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearParticipante(rs));
                }
            }
        }
        return lista;
    }

    public List<Participante> listarTodos() throws SQLException {
        return buscarPorNome("");
    }

    private Participante mapearParticipante(ResultSet rs) throws SQLException {
        Participante p = new Participante();
        p.setId(rs.getLong("id"));
        p.setNome(rs.getString("nome"));
        p.setCpf(rs.getString("cpf"));
        p.setTelefone(rs.getString("telefone"));
        p.setEmail(rs.getString("email"));
        p.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        p.setAtualizadoEm(rs.getTimestamp("atualizado_em").toLocalDateTime());
        return p;
    }
}
