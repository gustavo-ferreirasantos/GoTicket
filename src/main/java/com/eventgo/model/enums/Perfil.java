package com.eventgo.model.enums;

public enum Perfil {
    ADMIN("Administrador"),
    FUNCIONARIO("Funcionário"),
    OPERADOR_BILHETERIA("Operador de Bilheteria"),
    OPERADOR_PORTARIA("Operador de Portaria");

    private final String descricao;

    Perfil(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static Perfil fromString(String text) {
        for (Perfil p : Perfil.values()) {
            if (p.name().equalsIgnoreCase(text) || p.descricao.equalsIgnoreCase(text)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Perfil desconhecido: " + text);
    }
}
