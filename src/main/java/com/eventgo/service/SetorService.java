package com.eventgo.service;

import com.eventgo.dao.EventoDAO;
import com.eventgo.dao.SetorDAO;
import com.eventgo.dto.SetorDTO;
import com.eventgo.model.Evento;
import com.eventgo.model.Setor;
import com.eventgo.util.ValidacaoUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SetorService {

    private final SetorDAO setorDAO;
    private final EventoDAO eventoDAO;

    public SetorService() {
        this.setorDAO = new SetorDAO();
        this.eventoDAO = new EventoDAO();
    }

    public SetorService(SetorDAO setorDAO, EventoDAO eventoDAO) {
        this.setorDAO = setorDAO;
        this.eventoDAO = eventoDAO;
    }

    public Setor cadastrar(SetorDTO dto) throws SQLException {
        validarCampos(dto);

        Evento evento = eventoDAO.buscarPorId(dto.getEventoId())
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado."));

        // RN-01: Soma das capacidades dos setores <= capacidade total do evento
        int capacidadeExistente = setorDAO.somarCapacidadePorEvento(dto.getEventoId(), null);
        if (capacidadeExistente + dto.getCapacidade() > evento.getCapacidadeTotal()) {
            int disponivel = evento.getCapacidadeTotal() - capacidadeExistente;
            throw new IllegalArgumentException(
                    "A capacidade deste setor excede o limite disponível do evento. Limite restante: " + Math.max(0, disponivel)
            );
        }

        Setor setor = new Setor();
        setor.setEventoId(dto.getEventoId());
        setor.setNome(dto.getNome().trim());
        setor.setCapacidade(dto.getCapacidade());

        return setorDAO.inserir(setor);
    }

    public void cadastrar(Connection conn, Setor setor) throws SQLException {
        setorDAO.inserir(conn, setor);
    }

    public void atualizar(SetorDTO dto) throws SQLException {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("ID do setor é obrigatório para atualização.");
        }
        validarCampos(dto);

        Setor setor = setorDAO.buscarPorId(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Setor não encontrado."));

        Evento evento = eventoDAO.buscarPorId(setor.getEventoId())
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado."));

        // RN-01: Valida capacidade desconsiderando o setor atual
        int capacidadeExistente = setorDAO.somarCapacidadePorEvento(setor.getEventoId(), dto.getId());
        if (capacidadeExistente + dto.getCapacidade() > evento.getCapacidadeTotal()) {
            int disponivel = evento.getCapacidadeTotal() - capacidadeExistente;
            throw new IllegalArgumentException(
                    "A capacidade deste setor excede o limite disponível do evento. Limite restante: " + Math.max(0, disponivel)
            );
        }

        setor.setNome(dto.getNome().trim());
        setor.setCapacidade(dto.getCapacidade());

        setorDAO.atualizar(setor);
    }

    public void remover(Long id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("ID inválido.");
        }
        setorDAO.remover(id);
    }

    public List<Setor> listarPorEvento(Long eventoId) throws SQLException {
        if (eventoId == null) {
            throw new IllegalArgumentException("ID do evento é obrigatório.");
        }
        return setorDAO.listarPorEvento(eventoId);
    }

    public Optional<Setor> buscarPorId(Long id) throws SQLException {
        if (id == null) return Optional.empty();
        return setorDAO.buscarPorId(id);
    }

    private void validarCampos(SetorDTO dto) {
        if (dto.getEventoId() == null) {
            throw new IllegalArgumentException("O evento vinculado é obrigatório.");
        }
        if (!ValidacaoUtil.isPreenchido(dto.getNome())) {
            throw new IllegalArgumentException("O nome do setor é obrigatório.");
        }
        if (dto.getCapacidade() == null || dto.getCapacidade() <= 0) {
            throw new IllegalArgumentException("A capacidade do setor deve ser maior que zero.");
        }
    }
}
