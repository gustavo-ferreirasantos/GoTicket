package com.eventgo;

/**
 * Ponto de entrada (Launcher) padrão para inicialização do JavaFX.
 * Evita a exigência de argumentos de módulo JavaFX da VM ao executar diretamente em IDEs ou JARs executáveis.
 */
public class Main {
    public static void main(String[] args) {
        App.main(args);
    }
}
