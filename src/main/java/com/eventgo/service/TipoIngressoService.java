package com.eventgo.service;

import com.eventgo.dao.SetorDAO;
import com.eventgo.dao.TipoIngressoDAO;
import com.eventgo.dto.TipoIngressoDTO;
import com.eventgo.model.TipoIngresso;
import com.eventgo.model.enums.CategoriaIngresso;
import com.eventgo.util.ValidacaoUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TipoIngressoService {

    private final TipoIngressoDAO tipoIngressoDAO;
    private final SetorDAO setorDAO;

    public TipoIngressoService() {
        this.tipoIngressoDAO = new TipoIngressoDAO();
        this.setorDAO = new SetorDAO();
    }

    public TipoIngressoService(TipoIngressoDAO tipoIngressoDAO, SetorDAO setorDAO) {
        this.tipoIngressoDAO = tipoIngressoDAO;
        this.setorDAO = setorDAO;
    }

    public TipoIngresso cadastrar(TipoIngressoDTO dto) throws SQLException {
        validarCampos(dto);

        setorDAO.buscarPorId(dto.getSetorId())
                .orElseThrow(() -> new IllegalArgumentException("Setor não encontrado."));

        TipoIngresso tipo = new TipoIngresso();
        tipo.setSetorId(dto.getSetorId());
        tipo.setNome(dto.getNome().trim());
        tipo.setCategoria(dto.getCategoria() != null ? dto.getCategoria() : CategoriaIngresso.INTEIRA);

        return tipoIngressoDAO.inserir(tipo);
    }

    public void cadastrar(Connection conn, TipoIngresso tipo) throws SQLException {
        tipoIngressoDAO.inserir(conn, tipo);
    }

    public void remover(Long id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("ID inválido.");
        }
        tipoIngressoDAO.remover(id);
    }

    public List<TipoIngresso> listarPorSetor(Long setorId) throws SQLException {
        if (setorId == null) {
            throw new IllegalArgumentException("ID do setor é obrigatório.");
        }
        return tipoIngressoDAO.listarPorSetor(setorId);
    }

    public Optional<TipoIngresso> buscarPorId(Long id) throws SQLException {
        if (id == null) return Optional.empty();
        return tipoIngressoDAO.buscarPorId(id);
    }

    private void validarCampos(TipoIngressoDTO dto) {
        if (dto.getSetorId() == null) {
            throw new IllegalArgumentException("O setor vinculado é obrigatório.");
        }
        if (!ValidacaoUtil.isPreenchido(dto.getNome())) {
            throw new IllegalArgumentException("O nome do tipo de ingresso é obrigatório.");
        }
        if (dto.getCategoria() == null) {
            throw new IllegalArgumentException("A categoria do tipo de ingresso é obrigatória.");
        }
    }
}
