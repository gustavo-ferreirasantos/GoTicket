package com.eventgo.dao;

import com.eventgo.config.DatabaseConfig;
import com.eventgo.model.Ingresso;
import com.eventgo.model.enums.StatusIngresso;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class IngressoDAO {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public void inserirBatch(Connection conn, List<Ingresso> ingressos) throws SQLException {
        String sql = "INSERT INTO eventgo.ingresso (codigo, venda_id, lote_id, participante_id, preco_pago, status, criado_em) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (Ingresso ing : ingressos) {
                stmt.setObject(1, ing.getCodigo());
                stmt.setLong(2, ing.getVendaId());
                stmt.setLong(3, ing.getLoteId());
                if (ing.getParticipanteId() != null) {
                    stmt.setLong(4, ing.getParticipanteId());
                } else {
                    stmt.setNull(4, Types.BIGINT);
                }
                stmt.setBigDecimal(5, ing.getPrecoPago());
                stmt.setString(6, ing.getStatus().name());
                stmt.setTimestamp(7, Timestamp.valueOf(ing.getCriadoEm()));
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public Optional<Ingresso> buscarPorCodigo(UUID codigo) throws SQLException {
        String sql = "SELECT i.id, i.codigo, i.venda_id, i.lote_id, i.participante_id, i.preco_pago, " +
                     "       i.status, i.data_checkin, i.motivo_cancelamento, i.cancelado_por, i.data_cancelamento, i.criado_em, " +
                     "       e.nome AS evento_nome, e.data_evento, e.horario, e.local AS evento_local, " +
                     "       s.nome AS setor_nome, t.nome AS tipo_nome, " +
                     "       p.nome AS participante_nome, p.cpf AS participante_cpf " +
                     "FROM eventgo.ingresso i " +
                     "JOIN eventgo.lote l ON i.lote_id = l.id " +
                     "JOIN eventgo.tipo_ingresso t ON l.tipo_ingresso_id = t.id " +
                     "JOIN eventgo.setor s ON t.setor_id = s.id " +
                     "JOIN eventgo.evento e ON s.evento_id = e.id " +
                     "LEFT JOIN eventgo.participante p ON i.participante_id = p.id " +
                     "WHERE i.codigo = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, codigo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearIngressoCompleto(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Ingresso> listarPorVenda(Long vendaId) throws SQLException {
        List<Ingresso> lista = new ArrayList<>();
        String sql = "SELECT i.id, i.codigo, i.venda_id, i.lote_id, i.participante_id, i.preco_pago, " +
                     "       i.status, i.data_checkin, i.motivo_cancelamento, i.cancelado_por, i.data_cancelamento, i.criado_em, " +
                     "       e.nome AS evento_nome, e.data_evento, e.horario, e.local AS evento_local, " +
                     "       s.nome AS setor_nome, t.nome AS tipo_nome, " +
                     "       p.nome AS participante_nome, p.cpf AS participante_cpf " +
                     "FROM eventgo.ingresso i " +
                     "JOIN eventgo.lote l ON i.lote_id = l.id " +
                     "JOIN eventgo.tipo_ingresso t ON l.tipo_ingresso_id = t.id " +
                     "JOIN eventgo.setor s ON t.setor_id = s.id " +
                     "JOIN eventgo.evento e ON s.evento_id = e.id " +
                     "LEFT JOIN eventgo.participante p ON i.participante_id = p.id " +
                     "WHERE i.venda_id = ? ORDER BY i.id ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, vendaId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearIngressoCompleto(rs));
                }
            }
        }
        return lista;
    }

    public void registrarCheckin(UUID codigo) throws SQLException {
        String sql = "UPDATE eventgo.ingresso SET status = 'UTILIZADO', data_checkin = NOW() " +
                     "WHERE codigo = ? AND status = 'ATIVO'";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, codigo);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Ingresso não está ATIVO ou código inexistente.");
            }
        }
    }

    public void marcarComoEmitido(UUID codigo) throws SQLException {
        String sql = "UPDATE eventgo.ingresso SET status = 'EMITIDO' WHERE codigo = ? AND status = 'ATIVO'";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, codigo);
            stmt.executeUpdate();
        }
    }

    public void cancelar(Connection conn, UUID codigo, Long usuarioId, String motivo) throws SQLException {
        String sql = "UPDATE eventgo.ingresso SET status = 'CANCELADO', cancelado_por = ?, " +
                     "motivo_cancelamento = ?, data_cancelamento = NOW() WHERE codigo = ? AND status IN ('ATIVO','EMITIDO')";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, usuarioId);
            stmt.setString(2, motivo);
            stmt.setObject(3, codigo);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Ingresso não está ATIVO para ser cancelado.");
            }
        }
    }

    private Ingresso mapearIngressoCompleto(ResultSet rs) throws SQLException {
        Ingresso i = new Ingresso();
        i.setId(rs.getLong("id"));
        i.setCodigo((UUID) rs.getObject("codigo"));
        i.setVendaId(rs.getLong("venda_id"));
        i.setLoteId(rs.getLong("lote_id"));
        long partId = rs.getLong("participante_id");
        if (!rs.wasNull()) {
            i.setParticipanteId(partId);
        }
        i.setPrecoPago(rs.getBigDecimal("preco_pago"));
        i.setStatus(StatusIngresso.valueOf(rs.getString("status")));

        Timestamp tsCheckin = rs.getTimestamp("data_checkin");
        if (tsCheckin != null) i.setDataCheckin(tsCheckin.toLocalDateTime());

        i.setMotivoCancelamento(rs.getString("motivo_cancelamento"));

        long cancPor = rs.getLong("cancelado_por");
        if (!rs.wasNull()) i.setCanceladoPor(cancPor);

        Timestamp tsCanc = rs.getTimestamp("data_cancelamento");
        if (tsCanc != null) i.setDataCancelamento(tsCanc.toLocalDateTime());

        i.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());

        // Metadados
        i.setNomeEvento(rs.getString("evento_nome"));
        Date dtEvento = rs.getDate("data_evento");
        Time hrEvento = rs.getTime("horario");
        if (dtEvento != null && hrEvento != null) {
            i.setDataEventoFormatada(dtEvento.toLocalDate().format(dateFormatter) + " às " + hrEvento.toLocalTime().format(timeFormatter));
        }
        i.setLocalEvento(rs.getString("evento_local"));
        i.setNomeSetor(rs.getString("setor_nome"));
        i.setNomeTipoIngresso(rs.getString("tipo_nome"));
        i.setNomeParticipante(rs.getString("participante_nome"));
        i.setCpfParticipante(rs.getString("participante_cpf"));

        return i;
    }
}
