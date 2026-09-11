package com.eventgo.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ValidacaoUtilTest {

    @Test
    @DisplayName("Deve validar CPF válido com ou sem máscara")
    void deveValidarCpfValido() {
        // CPFs válidos conhecidos de teste
        assertTrue(ValidacaoUtil.isCpfValido("11144477735"));
        assertTrue(ValidacaoUtil.isCpfValido("111.444.777-35"));
        assertTrue(ValidacaoUtil.isCpfValido("52998224725"));
    }

    @Test
    @DisplayName("Deve rejeitar CPF com dígitos inválidos ou todos iguais")
    void deveRejeitarCpfInvalido() {
        assertFalse(ValidacaoUtil.isCpfValido("11111111111"));
        assertFalse(ValidacaoUtil.isCpfValido("00000000000"));
        assertFalse(ValidacaoUtil.isCpfValido("12345678900"));
        assertFalse(ValidacaoUtil.isCpfValido(""));
        assertFalse(ValidacaoUtil.isCpfValido(null));
        assertFalse(ValidacaoUtil.isCpfValido("123"));
    }

    @Test
    @DisplayName("Deve validar e-mail corretamente")
    void deveValidarEmail() {
        assertTrue(ValidacaoUtil.isEmailValido("contato@eventgo.com"));
        assertTrue(ValidacaoUtil.isEmailValido("usuario.teste@empresa.com.br"));
        assertTrue(ValidacaoUtil.isEmailValido("")); // Vazio é aceito pois é opcional
        assertTrue(ValidacaoUtil.isEmailValido(null));

        assertFalse(ValidacaoUtil.isEmailValido("email_sem_arroba.com"));
        assertFalse(ValidacaoUtil.isEmailValido("@dominio.com"));
        assertFalse(ValidacaoUtil.isEmailValido("usuario@"));
    }

    @Test
    @DisplayName("Deve verificar preenchimento de string não vazia")
    void deveVerificarPreenchimento() {
        assertTrue(ValidacaoUtil.isPreenchido("Texto"));
        assertFalse(ValidacaoUtil.isPreenchido("   "));
        assertFalse(ValidacaoUtil.isPreenchido(""));
        assertFalse(ValidacaoUtil.isPreenchido(null));
    }
}
