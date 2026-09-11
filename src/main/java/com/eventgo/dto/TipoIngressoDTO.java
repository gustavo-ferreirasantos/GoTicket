package com.eventgo.dto;

import com.eventgo.model.enums.CategoriaIngresso;

public class TipoIngressoDTO {
    private Long id;
    private Long setorId;
    private String nome;
    private CategoriaIngresso categoria;

    public TipoIngressoDTO() {
        this.categoria = CategoriaIngresso.INTEIRA;
    }

    public TipoIngressoDTO(Long setorId, String nome, CategoriaIngresso categoria) {
        this.setorId = setorId;
        this.nome = nome;
        this.categoria = categoria;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSetorId() {
        return setorId;
    }

    public void setSetorId(Long setorId) {
        this.setorId = setorId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public CategoriaIngresso getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaIngresso categoria) {
        this.categoria = categoria;
    }
}
