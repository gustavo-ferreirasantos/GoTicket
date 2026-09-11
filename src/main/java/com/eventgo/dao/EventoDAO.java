package com.eventgo.dao;

import com.eventgo.config.DatabaseConfig;
import com.eventgo.dto.FiltroEventoDTO;
import com.eventgo.model.Evento;
import com.eventgo.model.enums.SituacaoEvento;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventoDAO {

    public Evento inserir(Evento evento) throws SQLException {
        String sql = "INSERT INTO eventgo.evento (nome, descricao, data_evento, horario, local, capacidade_total, situacao, criado_em, atualizado_em) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, evento.getNome());
            stmt.setString(2, evento.getDescricao());
            stmt.setDate(3, Date.valueOf(evento.getDataEvento()));
            stmt.setTime(4, Time.valueOf(evento.getHorario()));
            stmt.setString(5, evento.getLocal());
            stmt.setInt(6, evento.getCapacidadeTotal());
            stmt.setString(7, evento.getSituacao().name());
            stmt.setTimestamp(8, Timestamp.valueOf(evento.getCriadoEm()));
            stmt.setTimestamp(9, Timestamp.valueOf(evento.getAtualizadoEm()));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    evento.setId(rs.getLong("id"));
                }
            }
            return evento;
        }
    }

    public void atualizar(Evento evento) throws SQLException {
        String sql = "UPDATE eventgo.evento SET nome = ?, descricao = ?, data_evento = ?, horario = ?, " +
                     "local = ?, capacidade_total = ?, situacao = ?, atualizado_em = NOW() WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, evento.getNome());
            stmt.setString(2, evento.getDescricao());
            stmt.setDate(3, Date.valueOf(evento.getDataEvento()));
            stmt.setTime(4, Time.valueOf(evento.getHorario()));
            stmt.setString(5, evento.getLocal());
            stmt.setInt(6, evento.getCapacidadeTotal());
            stmt.setString(7, evento.getSituacao().name());
            stmt.setLong(8, evento.getId());

            stmt.executeUpdate();
        }
    }

    public void alterarSituacao(Long id, SituacaoEvento novaSituacao) throws SQLException {
        String sql = "UPDATE eventgo.evento SET situacao = ?, atualizado_em = NOW() WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novaSituacao.name());
            stmt.setLong(2, id);

            stmt.executeUpdate();
        }
    }

    public Optional<Evento> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT id, nome, descricao, data_evento, horario, local, capacidade_total, situacao, criado_em, atualizado_em " +
                     "FROM eventgo.evento WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearEvento(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Evento> listar(FiltroEventoDTO filtro) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT id, nome, descricao, data_evento, horario, local, capacidade_total, situacao, criado_em, atualizado_em " +
                                              "FROM eventgo.evento WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (filtro != null) {
            if (filtro.getNome() != null && !filtro.getNome().isBlank()) {
                sql.append(" AND LOWER(nome) LIKE LOWER(?) ");
                params.add("%" + filtro.getNome().trim() + "%");
            }
            if (filtro.getSituacao() != null) {
                sql.append(" AND situacao = ? ");
                params.add(filtro.getSituacao().name());
            }
            if (filtro.getDataInicio() != null) {
                sql.append(" AND data_evento >= ? ");
                params.add(Date.valueOf(filtro.getDataInicio()));
            }
            if (filtro.getDataFim() != null) {
                sql.append(" AND data_evento <= ? ");
                params.add(Date.valueOf(filtro.getDataFim()));
            }
        }

        sql.append(" ORDER BY data_evento ASC, horario ASC");

        List<Evento> lista = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearEvento(rs));
                }
            }
        }
        return lista;
    }

    public List<Evento> listarAbertosParaVenda() throws SQLException {
        FiltroEventoDTO filtro = new FiltroEventoDTO();
        filtro.setSituacao(SituacaoEvento.ABERTO);
        return listar(filtro);
    }

    private Evento mapearEvento(ResultSet rs) throws SQLException {
        Evento e = new Evento();
        e.setId(rs.getLong("id"));
        e.setNome(rs.getString("nome"));
        e.setDescricao(rs.getString("descricao"));
        e.setDataEvento(rs.getDate("data_evento").toLocalDate());
        e.setHorario(rs.getTime("horario").toLocalTime());
        e.setLocal(rs.getString("local"));
        e.setCapacidadeTotal(rs.getInt("capacidade_total"));
        e.setSituacao(SituacaoEvento.valueOf(rs.getString("situacao")));
        e.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        e.setAtualizadoEm(rs.getTimestamp("atualizado_em").toLocalDateTime());
        return e;
    }
}
