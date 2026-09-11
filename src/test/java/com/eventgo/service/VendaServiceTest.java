package com.eventgo.service;

import com.eventgo.dao.EventoDAO;
import com.eventgo.dao.IngressoDAO;
import com.eventgo.dao.LoteDAO;
import com.eventgo.dao.VendaDAO;
import com.eventgo.dto.ItemVendaDTO;
import com.eventgo.dto.VendaDTO;
import com.eventgo.model.Evento;
import com.eventgo.model.enums.FormaPagamento;
import com.eventgo.model.enums.SituacaoEvento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class VendaServiceTest {

    @Mock private VendaDAO vendaDAO;
    @Mock private IngressoDAO ingressoDAO;
    @Mock private LoteDAO loteDAO;
    @Mock private EventoDAO eventoDAO;

    @InjectMocks private VendaService vendaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Deve impedir venda se a situação do evento não for ABERTO (RN-03)")
    void deveImpedirVendaEventoNaoAberto() throws SQLException {
        Evento evento = new Evento();
        evento.setId(1L);
        evento.setSituacao(SituacaoEvento.PLANEJADO); // Não está ABERTO

        when(eventoDAO.buscarPorId(1L)).thenReturn(Optional.of(evento));

        VendaDTO dto = new VendaDTO();
        dto.setUsuarioId(1L);
        dto.setEventoId(1L);
        dto.setFormaPagamento(FormaPagamento.DINHEIRO);
        dto.adicionarItem(new ItemVendaDTO(10L, 20L, 1, new BigDecimal("100.00")));

        assertThrows(IllegalStateException.class, () -> vendaService.registrarVenda(dto));
    }
}
