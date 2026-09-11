package com.eventgo.util;

import org.mindrot.jbcrypt.BCrypt;

public class SenhaUtil {

    private static final int LOG_ROUNDS = 10;

    public static String hashSenha(String senhaPlana) {
        if (senhaPlana == null || senhaPlana.isBlank()) {
            throw new IllegalArgumentException("Senha não pode ser vazia.");
        }
        return BCrypt.hashpw(senhaPlana, BCrypt.gensalt(LOG_ROUNDS));
    }

    public static boolean verificarSenha(String senhaPlana, String senhaHash) {
        if (senhaPlana == null || senhaHash == null || senhaHash.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(senhaPlana, senhaHash);
        } catch (Exception e) {
            return false;
        }
    }
}
