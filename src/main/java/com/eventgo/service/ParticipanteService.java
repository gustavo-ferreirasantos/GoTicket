package com.eventgo.service;

import com.eventgo.dao.ParticipanteDAO;
import com.eventgo.dto.ParticipanteDTO;
import com.eventgo.model.Participante;
import com.eventgo.util.ValidacaoUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ParticipanteService {

    private final ParticipanteDAO participanteDAO;

    public ParticipanteService() {
        this.participanteDAO = new ParticipanteDAO();
    }

    public ParticipanteService(ParticipanteDAO participanteDAO) {
        this.participanteDAO = participanteDAO;
    }

    public Participante cadastrarOuAtualizar(ParticipanteDTO dto) throws SQLException {
        validarCampos(dto);

        String cpfLimpo = dto.getCpf().replaceAll("\\D", "");
        Optional<Participante> existente = participanteDAO.buscarPorCpf(cpfLimpo);

        if (existente.isPresent()) {
            Participante p = existente.get();
            p.setNome(dto.getNome().trim());
            p.setTelefone(dto.getTelefone() != null ? dto.getTelefone().trim() : null);
            p.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : null);
            participanteDAO.atualizar(p);
            return p;
        } else {
            Participante p = new Participante();
            p.setNome(dto.getNome().trim());
            p.setCpf(cpfLimpo);
            p.setTelefone(dto.getTelefone() != null ? dto.getTelefone().trim() : null);
            p.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : null);
            return participanteDAO.inserir(p);
        }
    }

    public Optional<Participante> buscarPorCpf(String cpf) throws SQLException {
        if (!ValidacaoUtil.isPreenchido(cpf)) return Optional.empty();
        String cpfLimpo = cpf.replaceAll("\\D", "");
        return participanteDAO.buscarPorCpf(cpfLimpo);
    }

    public List<Participante> buscarPorNome(String nome) throws SQLException {
        return participanteDAO.buscarPorNome(nome);
    }

    public List<Participante> listarTodos() throws SQLException {
        return participanteDAO.listarTodos();
    }

    private void validarCampos(ParticipanteDTO dto) {
        if (!ValidacaoUtil.isPreenchido(dto.getNome())) {
            throw new IllegalArgumentException("O nome do participante é obrigatório.");
        }
        if (!ValidacaoUtil.isPreenchido(dto.getCpf())) {
            throw new IllegalArgumentException("O CPF do participante é obrigatório.");
        }
        if (!ValidacaoUtil.isCpfValido(dto.getCpf())) {
            throw new IllegalArgumentException("CPF inválido. Verifique os dígitos informados.");
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank() && !ValidacaoUtil.isEmailValido(dto.getEmail())) {
            throw new IllegalArgumentException("Formato de e-mail inválido.");
        }
    }
}
