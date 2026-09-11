package com.eventgo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LoteDTO {
    private Long id;
    private Long tipoIngressoId;
    private Integer numeroLote;
    private BigDecimal preco;
    private Integer quantidadeTotal;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private boolean ativo;

    public LoteDTO() {
        this.preco = BigDecimal.ZERO;
        this.ativo = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTipoIngressoId() {
        return tipoIngressoId;
    }

    public void setTipoIngressoId(Long tipoIngressoId) {
        this.tipoIngressoId = tipoIngressoId;
    }

    public Integer getNumeroLote() {
        return numeroLote;
    }

    public void setNumeroLote(Integer numeroLote) {
        this.numeroLote = numeroLote;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public Integer getQuantidadeTotal() {
        return quantidadeTotal;
    }

    public void setQuantidadeTotal(Integer quantidadeTotal) {
        this.quantidadeTotal = quantidadeTotal;
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

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
