package com.eventgo.dto;

import com.eventgo.model.enums.SituacaoEvento;

import java.time.LocalDate;

public class FiltroEventoDTO {
    private String nome;
    private SituacaoEvento situacao;
    private LocalDate dataInicio;
    private LocalDate dataFim;

    public FiltroEventoDTO() {}

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public SituacaoEvento getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoEvento situacao) {
        this.situacao = situacao;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }
}
