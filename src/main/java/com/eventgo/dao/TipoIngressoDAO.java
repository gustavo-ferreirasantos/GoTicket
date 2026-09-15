package com.eventgo.dao;

import com.eventgo.config.DatabaseConfig;
import com.eventgo.model.TipoIngresso;
import com.eventgo.model.enums.CategoriaIngresso;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TipoIngressoDAO {

    public TipoIngresso inserir(TipoIngresso tipo) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return inserir(conn, tipo);
        }
    }

    public TipoIngresso inserir(Connection conn, TipoIngresso tipo) throws SQLException {
        String sql = "INSERT INTO eventgo.tipo_ingresso (setor_id, nome, categoria, criado_em) " +
                     "VALUES (?, ?, ?, ?) RETURNING id";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, tipo.getSetorId());
            stmt.setString(2, tipo.getNome());
            stmt.setString(3, tipo.getCategoria().name());
            stmt.setTimestamp(4, Timestamp.valueOf(tipo.getCriadoEm()));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    tipo.setId(rs.getLong("id"));
                }
            }
            return tipo;
        }
    }

    public void remover(Long id) throws SQLException {
        String sql = "DELETE FROM eventgo.tipo_ingresso WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    public Optional<TipoIngresso> buscarPorId(Long id) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return buscarPorId(conn, id);
        }
    }

    public Optional<TipoIngresso> buscarPorId(Connection conn, Long id) throws SQLException {
        String sql = "SELECT id, setor_id, nome, categoria, criado_em FROM eventgo.tipo_ingresso WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearTipoIngresso(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<TipoIngresso> listarPorSetor(Long setorId) throws SQLException {
        List<TipoIngresso> lista = new ArrayList<>();
        String sql = "SELECT id, setor_id, nome, categoria, criado_em FROM eventgo.tipo_ingresso " +
                     "WHERE setor_id = ? ORDER BY nome ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, setorId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearTipoIngresso(rs));
                }
            }
        }
        return lista;
    }

    private TipoIngresso mapearTipoIngresso(ResultSet rs) throws SQLException {
        TipoIngresso ti = new TipoIngresso();
        ti.setId(rs.getLong("id"));
        ti.setSetorId(rs.getLong("setor_id"));
        ti.setNome(rs.getString("nome"));
        ti.setCategoria(CategoriaIngresso.valueOf(rs.getString("categoria")));
        ti.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        return ti;
    }
}
