package com.eventgo.dto;

import com.eventgo.model.enums.FormaPagamento;

import java.util.ArrayList;
import java.util.List;

public class VendaDTO {
    private Long usuarioId;
    private Long eventoId;
    private FormaPagamento formaPagamento;
    private List<ItemVendaDTO> itens = new ArrayList<>();

    public VendaDTO() {
        this.formaPagamento = FormaPagamento.DINHEIRO;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getEventoId() {
        return eventoId;
    }

    public void setEventoId(Long eventoId) {
        this.eventoId = eventoId;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public List<ItemVendaDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemVendaDTO> itens) {
        this.itens = itens;
    }

    public void adicionarItem(ItemVendaDTO item) {
        this.itens.add(item);
    }
}
