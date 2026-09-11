package com.eventgo.dto;

import java.math.BigDecimal;

public class ItemVendaDTO {
    private Long loteId;
    private Long participanteId;
    private int quantidade;
    private BigDecimal precoUnitario;

    // Metadados auxiliares para tela
    private String nomeSetor;
    private String nomeTipo;
    private Integer numeroLote;
    private String nomeParticipante;

    public ItemVendaDTO() {
        this.quantidade = 1;
        this.precoUnitario = BigDecimal.ZERO;
    }

    public ItemVendaDTO(Long loteId, Long participanteId, int quantidade, BigDecimal precoUnitario) {
        this.loteId = loteId;
        this.participanteId = participanteId;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    public Long getLoteId() {
        return loteId;
    }

    public void setLoteId(Long loteId) {
        this.loteId = loteId;
    }

    public Long getParticipanteId() {
        return participanteId;
    }

    public void setParticipanteId(Long participanteId) {
        this.participanteId = participanteId;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public BigDecimal getSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    public String getNomeSetor() {
        return nomeSetor;
    }

    public void setNomeSetor(String nomeSetor) {
        this.nomeSetor = nomeSetor;
    }

    public String getNomeTipo() {
        return nomeTipo;
    }

    public void setNomeTipo(String nomeTipo) {
        this.nomeTipo = nomeTipo;
    }

    public Integer getNumeroLote() {
        return numeroLote;
    }

    public void setNumeroLote(Integer numeroLote) {
        this.numeroLote = numeroLote;
    }

    public String getNomeParticipante() {
        return nomeParticipante;
    }

    public void setNomeParticipante(String nomeParticipante) {
        this.nomeParticipante = nomeParticipante;
    }
}
