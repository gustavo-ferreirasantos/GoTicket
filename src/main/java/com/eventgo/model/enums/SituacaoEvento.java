package com.eventgo.model.enums;

public enum SituacaoEvento {
    PLANEJADO("Planejado"),
    ABERTO("Aberto para Vendas"),
    ENCERRADO("Encerrado"),
    CANCELADO("Cancelado");

    private final String descricao;

    SituacaoEvento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
