package com.eventgo.service;

import com.eventgo.dao.UsuarioDAO;
import com.eventgo.dto.UsuarioDTO;
import com.eventgo.model.Usuario;
import com.eventgo.model.enums.Perfil;
import com.eventgo.util.SenhaUtil;
import com.eventgo.util.ValidacaoUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UsuarioService {

    private final UsuarioDAO usuarioDAO;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    public UsuarioService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public Usuario autenticar(String login, String senha) throws SQLException {
        if (!ValidacaoUtil.isPreenchido(login) || !ValidacaoUtil.isPreenchido(senha)) {
            throw new IllegalArgumentException("Login e senha são obrigatórios.");
        }

        Optional<Usuario> usuarioOpt = usuarioDAO.buscarPorLogin(login.trim());
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuário ou senha inválidos.");
        }

        Usuario usuario = usuarioOpt.get();
        if (!usuario.isAtivo()) {
            throw new IllegalStateException("Usuário está inativo no sistema.");
        }

        if (!SenhaUtil.verificarSenha(senha, usuario.getSenhaHash())) {
            throw new IllegalArgumentException("Usuário ou senha inválidos.");
        }

        return usuario;
    }

    public Usuario cadastrar(UsuarioDTO dto) throws SQLException {
        validarCamposObrigatorios(dto, true);

        Optional<Usuario> existente = usuarioDAO.buscarPorLogin(dto.getLogin().trim());
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com este login.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome().trim());
        usuario.setLogin(dto.getLogin().trim().toLowerCase());
        usuario.setSenhaHash(SenhaUtil.hashSenha(dto.getSenha()));
        usuario.setPerfil(dto.getPerfil() != null ? dto.getPerfil() : Perfil.OPERADOR_BILHETERIA);
        usuario.setAtivo(dto.isAtivo());

        return usuarioDAO.inserir(usuario);
    }

    public void atualizar(UsuarioDTO dto) throws SQLException {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório para atualização.");
        }
        validarCamposObrigatorios(dto, false);

        Optional<Usuario> usuarioOpt = usuarioDAO.buscarPorId(dto.getId());
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }

        // Se mudou de login, validar unicidade
        Optional<Usuario> porLogin = usuarioDAO.buscarPorLogin(dto.getLogin().trim());
        if (porLogin.isPresent() && !porLogin.get().getId().equals(dto.getId())) {
            throw new IllegalArgumentException("Já existe outro usuário com este login.");
        }

        Usuario usuario = usuarioOpt.get();
        usuario.setNome(dto.getNome().trim());
        usuario.setLogin(dto.getLogin().trim().toLowerCase());
        usuario.setPerfil(dto.getPerfil());
        usuario.setAtivo(dto.isAtivo());

        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            usuario.setSenhaHash(SenhaUtil.hashSenha(dto.getSenha()));
        }

        usuarioDAO.atualizar(usuario);
    }

    public void desativar(Long id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("ID inválido.");
        }
        usuarioDAO.desativar(id);
    }

    public List<Usuario> listarTodos() throws SQLException {
        return usuarioDAO.listarTodos();
    }

    public Optional<Usuario> buscarPorId(Long id) throws SQLException {
        if (id == null) return Optional.empty();
        return usuarioDAO.buscarPorId(id);
    }

    private void validarCamposObrigatorios(UsuarioDTO dto, boolean exigeSenha) {
        if (!ValidacaoUtil.isPreenchido(dto.getNome())) {
            throw new IllegalArgumentException("O nome do usuário é obrigatório.");
        }
        if (!ValidacaoUtil.isPreenchido(dto.getLogin())) {
            throw new IllegalArgumentException("O login é obrigatório.");
        }
        if (exigeSenha && !ValidacaoUtil.isPreenchido(dto.getSenha())) {
            throw new IllegalArgumentException("A senha é obrigatória.");
        }
        if (dto.getPerfil() == null) {
            throw new IllegalArgumentException("O perfil de acesso é obrigatório.");
        }
    }
}
