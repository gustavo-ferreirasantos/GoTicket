package com.eventgo.model.enums;

public enum StatusIngresso {
    ATIVO("Ativo"),
    UTILIZADO("Utilizado / Entrou"),
    CANCELADO("Cancelado");

    private final String descricao;

    StatusIngresso(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
