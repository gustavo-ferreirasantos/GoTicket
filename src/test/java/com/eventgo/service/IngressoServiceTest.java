package com.eventgo.service;

import com.eventgo.config.DatabaseConfig;
import com.eventgo.dao.IngressoDAO;
import com.eventgo.dao.LoteDAO;
import com.eventgo.model.Ingresso;
import com.eventgo.model.enums.StatusIngresso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
    @DisplayName("Deve impedir cancelamento de ingresso que já foi cancelado anteriormente")
    void deveImpedirCancelamentoIngressoJaCancelado() throws SQLException {
        UUID codigo = UUID.randomUUID();
        Ingresso ingresso = new Ingresso();
        ingresso.setCodigo(codigo);
        ingresso.setStatus(StatusIngresso.CANCELADO);

        when(ingressoDAO.buscarPorCodigo(codigo)).thenReturn(Optional.of(ingresso));

        assertThrows(IllegalStateException.class, () ->
                ingressoService.cancelarIngresso(codigo, 1L, "Cliente solicitou"));
    }

    @Test
    @DisplayName("Deve exigir o código do ingresso para cancelar")
    void deveExigirCodigoParaCancelar() {
        assertThrows(IllegalArgumentException.class, () ->
                ingressoService.cancelarIngresso(null, 1L, "Motivo qualquer"));
    }

    @Test
    @DisplayName("Deve exigir o usuário responsável pelo cancelamento")
    void deveExigirUsuarioResponsavelParaCancelar() {
        UUID codigo = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () ->
                ingressoService.cancelarIngresso(codigo, null, "Motivo qualquer"));
    }

    @Test
    @DisplayName("Deve exigir motivo do cancelamento")
    void deveExigirMotivoParaCancelar() {
        UUID codigo = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () ->
                ingressoService.cancelarIngresso(codigo, 1L, "   "));
    }

    @Test
    @DisplayName("Deve cancelar o ingresso, registrar o responsável e devolver 1 unidade ao estoque do lote")
    void deveCancelarIngressoERestituirEstoque() throws SQLException {
        UUID codigo = UUID.randomUUID();
        Long usuarioId = 7L;
        Long loteId = 42L;
        String motivo = "Cliente desistiu da compra";

        Ingresso ingresso = new Ingresso();
        ingresso.setCodigo(codigo);
        ingresso.setStatus(StatusIngresso.EMITIDO);
        ingresso.setLoteId(loteId);

        when(ingressoDAO.buscarPorCodigo(codigo)).thenReturn(Optional.of(ingresso));

        Connection connMock = mock(Connection.class);
        try (MockedStatic<DatabaseConfig> dbConfigMock = Mockito.mockStatic(DatabaseConfig.class)) {
            dbConfigMock.when(DatabaseConfig::getConnection).thenReturn(connMock);

            ingressoService.cancelarIngresso(codigo, usuarioId, motivo);

            verify(ingressoDAO).cancelar(connMock, codigo, usuarioId, motivo);
            verify(loteDAO).incrementarEstoque(connMock, loteId, 1);
            verify(connMock).commit();
        }
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
