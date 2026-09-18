package com.eventgo.dao;

import com.eventgo.config.DatabaseConfig;
import com.eventgo.dto.VendaRelatorioDTO;
import com.eventgo.model.Venda;
import com.eventgo.model.enums.FormaPagamento;
import com.eventgo.model.enums.StatusIngresso;
import com.eventgo.model.enums.StatusVenda;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public List<VendaRelatorioDTO> listarRelatorio(Long eventoId, StatusVenda status, FormaPagamento formaPagamento) throws SQLException {
        List<VendaRelatorioDTO> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT v.id AS venda_id, e.nome AS evento_nome, v.data_venda, v.forma_pagamento, " +
            "COALESCE((SELECT SUM(i0.preco_pago) FROM eventgo.ingresso i0 " +
            "          WHERE i0.venda_id = v.id AND i0.status <> 'CANCELADO'), 0) AS valor_total, " +
            "v.status, " +
            "(SELECT COUNT(*) FROM eventgo.ingresso i WHERE i.venda_id = v.id) AS qtd_ingressos, " +
            "(SELECT i2.codigo FROM eventgo.ingresso i2 WHERE i2.venda_id = v.id ORDER BY i2.id ASC LIMIT 1) AS primeiro_codigo, " +
            "(SELECT i3.status FROM eventgo.ingresso i3 WHERE i3.venda_id = v.id ORDER BY i3.id ASC LIMIT 1) AS status_ingresso " +
            "FROM eventgo.venda v " +
            "JOIN eventgo.evento e ON v.evento_id = e.id " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (eventoId != null) {
            sql.append("AND v.evento_id = ? ");
            params.add(eventoId);
        }
        if (status != null) {
            sql.append("AND v.status = ? ");
            params.add(status.name());
        }
        if (formaPagamento != null) {
            sql.append("AND v.forma_pagamento = ? ");
            params.add(formaPagamento.name());
        }

        sql.append("ORDER BY v.data_venda DESC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof Long l) stmt.setLong(i + 1, l);
                else if (param instanceof String s) stmt.setString(i + 1, s);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    VendaRelatorioDTO dto = new VendaRelatorioDTO();
                    dto.setVendaId(rs.getLong("venda_id"));
                    dto.setEventoNome(rs.getString("evento_nome"));
                    dto.setDataVenda(rs.getTimestamp("data_venda").toLocalDateTime());
                    dto.setFormaPagamento(FormaPagamento.valueOf(rs.getString("forma_pagamento")));
                    dto.setValorTotal(rs.getBigDecimal("valor_total"));
                    dto.setStatus(StatusVenda.valueOf(rs.getString("status")));
                    dto.setQuantidadeIngressos(rs.getInt("qtd_ingressos"));
                    UUID codigo = (UUID) rs.getObject("primeiro_codigo");
                    dto.setPrimeiroIngressoCodigo(codigo);
                    String statusIngressoStr = rs.getString("status_ingresso");
                    if (statusIngressoStr != null) {
                        dto.setStatusIngresso(StatusIngresso.valueOf(statusIngressoStr));
                    }
                    lista.add(dto);
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

    public void atualizarStatus(Connection conn, Long vendaId, StatusVenda status) throws SQLException {
        String sql = "UPDATE eventgo.venda SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setLong(2, vendaId);
            stmt.executeUpdate();
        }
    }

    public int contarIngressosAtivos(Long vendaId) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return contarIngressosAtivos(conn, vendaId);
        }
    }

    public int contarIngressosAtivos(Connection conn, Long vendaId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM eventgo.ingresso WHERE venda_id = ? AND status IN ('ATIVO','EMITIDO')";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, vendaId);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
