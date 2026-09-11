package com.eventgo.service;

import com.eventgo.dao.IngressoDAO;
import com.eventgo.dao.LoteDAO;
import com.eventgo.model.Ingresso;
import com.eventgo.model.enums.StatusIngresso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class IngressoServiceTest {

    @Mock private IngressoDAO ingressoDAO;
    @Mock private LoteDAO loteDAO;

    @InjectMocks private IngressoService ingressoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Deve impedir cancelamento de ingresso que já foi utilizado na entrada")
    void deveImpedirCancelamentoIngressoUtilizado() throws SQLException {
        UUID codigo = UUID.randomUUID();
        Ingresso ingresso = new Ingresso();
        ingresso.setCodigo(codigo);
        ingresso.setStatus(StatusIngresso.UTILIZADO); // Já entrou

        when(ingressoDAO.buscarPorCodigo(codigo)).thenReturn(Optional.of(ingresso));

        assertThrows(IllegalStateException.class, () ->
                ingressoService.cancelarIngresso(codigo, 1L, "Cliente solicitou"));
    }

    @Test
    @DisplayName("Deve impedir check-in duplo em ingresso já utilizado (RN-06)")
    void deveImpedirCheckinDuplo() throws SQLException {
        UUID codigo = UUID.randomUUID();
        Ingresso ingresso = new Ingresso();
        ingresso.setCodigo(codigo);
        ingresso.setStatus(StatusIngresso.UTILIZADO);

        when(ingressoDAO.buscarPorCodigo(codigo)).thenReturn(Optional.of(ingresso));

        assertThrows(IllegalStateException.class, () ->
                ingressoService.registrarCheckin(codigo));
    }

    @Test
    @DisplayName("Deve impedir check-in em ingresso CANCELADO")
    void deveImpedirCheckinIngressoCancelado() throws SQLException {
        UUID codigo = UUID.randomUUID();
        Ingresso ingresso = new Ingresso();
        ingresso.setCodigo(codigo);
        ingresso.setStatus(StatusIngresso.CANCELADO);

        when(ingressoDAO.buscarPorCodigo(codigo)).thenReturn(Optional.of(ingresso));

        assertThrows(IllegalStateException.class, () ->
                ingressoService.registrarCheckin(codigo));
    }
}
