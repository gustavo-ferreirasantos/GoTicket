package com.eventgo.model;

import com.eventgo.model.enums.CategoriaIngresso;

import java.time.LocalDateTime;
import java.util.Objects;

public class TipoIngresso {
    private Long id;
    private Long setorId;
    private String nome;
    private CategoriaIngresso categoria;
    private LocalDateTime criadoEm;

    public TipoIngresso() {
        this.categoria = CategoriaIngresso.INTEIRA;
        this.criadoEm = LocalDateTime.now();
    }

    public TipoIngresso(Long id, Long setorId, String nome, CategoriaIngresso categoria) {
        this.id = id;
        this.setorId = setorId;
        this.nome = nome;
        this.categoria = categoria;
        this.criadoEm = LocalDateTime.now();
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

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TipoIngresso that = (TipoIngresso) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nome + " (" + categoria.getDescricao() + ")";
    }
}
