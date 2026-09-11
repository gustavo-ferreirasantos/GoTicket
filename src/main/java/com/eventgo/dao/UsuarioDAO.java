package com.eventgo.dao;

import com.eventgo.config.DatabaseConfig;
import com.eventgo.model.Usuario;
import com.eventgo.model.enums.Perfil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioDAO {

    public Usuario inserir(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO eventgo.usuario (nome, login, senha_hash, perfil, ativo, criado_em, atualizado_em) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getLogin());
            stmt.setString(3, usuario.getSenhaHash());
            stmt.setString(4, usuario.getPerfil().name());
            stmt.setBoolean(5, usuario.isAtivo());
            stmt.setTimestamp(6, Timestamp.valueOf(usuario.getCriadoEm()));
            stmt.setTimestamp(7, Timestamp.valueOf(usuario.getAtualizadoEm()));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    usuario.setId(rs.getLong("id"));
                }
            }
            return usuario;
        }
    }

    public Optional<Usuario> buscarPorLogin(String login) throws SQLException {
        String sql = "SELECT id, nome, login, senha_hash, perfil, ativo, criado_em, atualizado_em " +
                     "FROM eventgo.usuario WHERE LOWER(login) = LOWER(?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearUsuario(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Usuario> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT id, nome, login, senha_hash, perfil, ativo, criado_em, atualizado_em " +
                     "FROM eventgo.usuario WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearUsuario(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, nome, login, senha_hash, perfil, ativo, criado_em, atualizado_em " +
                     "FROM eventgo.usuario ORDER BY nome ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapearUsuario(rs));
            }
        }
        return lista;
    }

    public void atualizar(Usuario usuario) throws SQLException {
        String sql = "UPDATE eventgo.usuario SET nome = ?, login = ?, perfil = ?, ativo = ?, atualizado_em = NOW() " +
                     (usuario.getSenhaHash() != null && !usuario.getSenhaHash().isBlank() ? ", senha_hash = ? " : "") +
                     "WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getLogin());
            stmt.setString(3, usuario.getPerfil().name());
            stmt.setBoolean(4, usuario.isAtivo());

            if (usuario.getSenhaHash() != null && !usuario.getSenhaHash().isBlank()) {
                stmt.setString(5, usuario.getSenhaHash());
                stmt.setLong(6, usuario.getId());
            } else {
                stmt.setLong(5, usuario.getId());
            }

            stmt.executeUpdate();
        }
    }

    public void desativar(Long id) throws SQLException {
        String sql = "UPDATE eventgo.usuario SET ativo = false, atualizado_em = NOW() WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getLong("id"));
        u.setNome(rs.getString("nome"));
        u.setLogin(rs.getString("login"));
        u.setSenhaHash(rs.getString("senha_hash"));
        u.setPerfil(Perfil.valueOf(rs.getString("perfil")));
        u.setAtivo(rs.getBoolean("ativo"));
        u.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        u.setAtualizadoEm(rs.getTimestamp("atualizado_em").toLocalDateTime());
        return u;
    }
}
