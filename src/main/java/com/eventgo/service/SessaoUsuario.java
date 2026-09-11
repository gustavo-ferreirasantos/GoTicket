package com.eventgo.service;

import com.eventgo.model.Usuario;
import com.eventgo.model.enums.Perfil;

public class SessaoUsuario {

    private static SessaoUsuario instancia;
    private Usuario usuarioLogado;

    private SessaoUsuario() {}

    public static synchronized SessaoUsuario getInstancia() {
        if (instancia == null) {
            instancia = new SessaoUsuario();
        }
        return instancia;
    }

    public void iniciarSessao(Usuario usuario) {
        this.usuarioLogado = usuario;
    }

    public void encerrarSessao() {
        this.usuarioLogado = null;
    }

    public Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    public boolean isAutenticado() {
        return usuarioLogado != null;
    }

    public boolean isAdmin() {
        return isAutenticado() && usuarioLogado.getPerfil() == Perfil.ADMIN;
    }

    public boolean isBilheteria() {
        return isAutenticado() && (usuarioLogado.getPerfil() == Perfil.OPERADOR_BILHETERIA || isAdmin());
    }

    public boolean isPortaria() {
        return isAutenticado() && (usuarioLogado.getPerfil() == Perfil.OPERADOR_PORTARIA || isAdmin());
    }
}
