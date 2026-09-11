package com.eventgo.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Setor {
    private Long id;
    private Long eventoId;
    private String nome;
    private Integer capacidade;
    private LocalDateTime criadoEm;

    public Setor() {
        this.criadoEm = LocalDateTime.now();
    }

    public Setor(Long id, Long eventoId, String nome, Integer capacidade) {
        this.id = id;
        this.eventoId = eventoId;
        this.nome = nome;
        this.capacidade = capacidade;
        this.criadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventoId() {
        return eventoId;
    }

    public void setEventoId(Long eventoId) {
        this.eventoId = eventoId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(Integer capacidade) {
        this.capacidade = capacidade;
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
        Setor setor = (Setor) o;
        return Objects.equals(id, setor.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nome + " (Cap: " + capacidade + ")";
    }
}
