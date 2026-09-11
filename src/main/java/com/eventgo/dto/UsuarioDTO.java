package com.eventgo.dto;

import com.eventgo.model.enums.Perfil;

public class UsuarioDTO {
    private Long id;
    private String nome;
    private String login;
    private String senha;
    private Perfil perfil;
    private boolean ativo;

    public UsuarioDTO() {
        this.ativo = true;
    }

    public UsuarioDTO(String nome, String login, String senha, Perfil perfil) {
        this.nome = nome;
        this.login = login;
        this.senha = senha;
        this.perfil = perfil;
        this.ativo = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
