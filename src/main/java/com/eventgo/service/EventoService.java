package com.eventgo.service;

import com.eventgo.dao.EventoDAO;
import com.eventgo.dto.EventoDTO;
import com.eventgo.dto.FiltroEventoDTO;
import com.eventgo.model.Evento;
import com.eventgo.model.enums.SituacaoEvento;
import com.eventgo.util.ValidacaoUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EventoService {

    private final EventoDAO eventoDAO;

    public EventoService() {
        this.eventoDAO = new EventoDAO();
    }

    public EventoService(EventoDAO eventoDAO) {
        this.eventoDAO = eventoDAO;
    }

    public Evento cadastrar(EventoDTO dto) throws SQLException {
        validarCampos(dto);

        Evento evento = new Evento();
        evento.setNome(dto.getNome().trim());
        evento.setDescricao(dto.getDescricao() != null ? dto.getDescricao().trim() : "");
        evento.setDataEvento(dto.getDataEvento());
        evento.setHorario(dto.getHorario());
        evento.setLocal(dto.getLocal().trim());
        evento.setCapacidadeTotal(dto.getCapacidadeTotal());
        evento.setSituacao(dto.getSituacao() != null ? dto.getSituacao() : SituacaoEvento.PLANEJADO);

        return eventoDAO.inserir(evento);
    }

    public void atualizar(EventoDTO dto) throws SQLException {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("ID do evento é obrigatório para atualização.");
        }
        validarCampos(dto);

        Optional<Evento> eventoOpt = eventoDAO.buscarPorId(dto.getId());
        if (eventoOpt.isEmpty()) {
            throw new IllegalArgumentException("Evento não encontrado.");
        }

        Evento evento = eventoOpt.get();
        if (evento.getSituacao() == SituacaoEvento.ENCERRADO) {
            throw new IllegalStateException("Não é permitido editar dados de um evento encerrado.");
        }

        evento.setNome(dto.getNome().trim());
        evento.setDescricao(dto.getDescricao() != null ? dto.getDescricao().trim() : "");
        evento.setDataEvento(dto.getDataEvento());
        evento.setHorario(dto.getHorario());
        evento.setLocal(dto.getLocal().trim());
        evento.setCapacidadeTotal(dto.getCapacidadeTotal());
        if (dto.getSituacao() != null) {
            evento.setSituacao(dto.getSituacao());
        }

        eventoDAO.atualizar(evento);
    }

    public void alterarSituacao(Long id, SituacaoEvento novaSituacao) throws SQLException {
        if (id == null || novaSituacao == null) {
            throw new IllegalArgumentException("ID e nova situação são obrigatórios.");
        }

        Optional<Evento> eventoOpt = eventoDAO.buscarPorId(id);
        if (eventoOpt.isEmpty()) {
            throw new IllegalArgumentException("Evento não encontrado.");
        }

        Evento evento = eventoOpt.get();
        SituacaoEvento atual = evento.getSituacao();

        // Validação de transição de status
        if (atual == SituacaoEvento.ENCERRADO && novaSituacao != SituacaoEvento.ENCERRADO) {
            throw new IllegalStateException("Evento encerrado não pode ter sua situação alterada.");
        }
        if (atual == SituacaoEvento.CANCELADO && novaSituacao != SituacaoEvento.CANCELADO) {
            throw new IllegalStateException("Evento cancelado não pode ter sua situação alterada.");
        }

        eventoDAO.alterarSituacao(id, novaSituacao);
    }

    public Optional<Evento> buscarPorId(Long id) throws SQLException {
        if (id == null) return Optional.empty();
        return eventoDAO.buscarPorId(id);
    }

    public List<Evento> listar(FiltroEventoDTO filtro) throws SQLException {
        return eventoDAO.listar(filtro);
    }

    public List<Evento> listarAbertosParaVenda() throws SQLException {
        return eventoDAO.listarAbertosParaVenda();
    }

    private void validarCampos(EventoDTO dto) {
        if (!ValidacaoUtil.isPreenchido(dto.getNome())) {
            throw new IllegalArgumentException("O nome do evento é obrigatório.");
        }
        if (dto.getDataEvento() == null) {
            throw new IllegalArgumentException("A data do evento é obrigatória.");
        }
        if (dto.getHorario() == null) {
            throw new IllegalArgumentException("O horário do evento é obrigatório.");
        }
        if (!ValidacaoUtil.isPreenchido(dto.getLocal())) {
            throw new IllegalArgumentException("O local do evento é obrigatório.");
        }
        if (dto.getCapacidadeTotal() == null || dto.getCapacidadeTotal() <= 0) {
            throw new IllegalArgumentException("A capacidade total deve ser maior que zero.");
        }
    }
}
