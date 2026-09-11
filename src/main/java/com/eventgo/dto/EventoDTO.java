package com.eventgo.dto;

import com.eventgo.model.enums.SituacaoEvento;

import java.time.LocalDate;
import java.time.LocalTime;

public class EventoDTO {
    private Long id;
    private String nome;
    private String descricao;
    private LocalDate dataEvento;
    private LocalTime horario;
    private String local;
    private Integer capacidadeTotal;
    private SituacaoEvento situacao;

    public EventoDTO() {
        this.situacao = SituacaoEvento.PLANEJADO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getDataEvento() {
        return dataEvento;
    }

    public void setDataEvento(LocalDate dataEvento) {
        this.dataEvento = dataEvento;
    }

    public LocalTime getHorario() {
        return horario;
    }

    public void setHorario(LocalTime horario) {
        this.horario = horario;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public Integer getCapacidadeTotal() {
        return capacidadeTotal;
    }

    public void setCapacidadeTotal(Integer capacidadeTotal) {
        this.capacidadeTotal = capacidadeTotal;
    }

    public SituacaoEvento getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoEvento situacao) {
        this.situacao = situacao;
    }
}
