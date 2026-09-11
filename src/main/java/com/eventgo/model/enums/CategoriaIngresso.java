package com.eventgo.model.enums;

public enum CategoriaIngresso {
    INTEIRA("Inteira"),
    MEIA("Meia-Entrada"),
    CORTESIA("Cortesia");

    private final String descricao;

    CategoriaIngresso(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
