package com.eventgo.util;

import java.util.regex.Pattern;

public class ValidacaoUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public static boolean isCpfValido(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) return false;
        String limpo = cpf.replaceAll("\\D", "");
        if (limpo.length() != 11) return false;
        
        // Verifica dígitos repetidos tipo 11111111111
        if (limpo.matches("(\\d)\\1{10}")) return false;

        try {
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += Character.getNumericValue(limpo.charAt(i)) * (10 - i);
            }
            int r1 = 11 - (soma % 11);
            int dig1 = (r1 >= 10) ? 0 : r1;

            if (dig1 != Character.getNumericValue(limpo.charAt(9))) return false;

            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += Character.getNumericValue(limpo.charAt(i)) * (11 - i);
            }
            int r2 = 11 - (soma % 11);
            int dig2 = (r2 >= 10) ? 0 : r2;

            return dig2 == Character.getNumericValue(limpo.charAt(10));
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isEmailValido(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isPreenchido(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    public static String formatarCpf(String cpf) {
        if (cpf == null) return "";
        String limpo = cpf.replaceAll("\\D", "");
        if (limpo.length() == 11) {
            return String.format("%s.%s.%s-%s",
                    limpo.substring(0, 3),
                    limpo.substring(3, 6),
                    limpo.substring(6, 9),
                    limpo.substring(9, 11));
        }
        return cpf;
    }
}
