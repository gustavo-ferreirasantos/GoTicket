package com.eventgo.service;

import com.eventgo.dao.EventoDAO;
import com.eventgo.dto.EventoDTO;
import com.eventgo.model.Evento;
import com.eventgo.model.enums.SituacaoEvento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EventoServiceTest {

    @Mock
    private EventoDAO eventoDAO;

    @InjectMocks
    private EventoService eventoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Deve cadastrar evento válido com sucesso")
    void deveCadastrarEventoValido() throws SQLException {
        EventoDTO dto = new EventoDTO();
        dto.setNome("Show de Rock");
        dto.setDataEvento(LocalDate.now().plusDays(30));
        dto.setHorario(LocalTime.of(21, 0));
        dto.setLocal("Estádio Municipal");
        dto.setCapacidadeTotal(10000);
        dto.setSituacao(SituacaoEvento.PLANEJADO);

        when(eventoDAO.inserir(any(Evento.class))).thenAnswer(i -> {
            Evento e = i.getArgument(0);
            e.setId(1L);
            return e;
        });

        Evento cadastrado = eventoService.cadastrar(dto);

        assertNotNull(cadastrado);
        assertEquals("Show de Rock", cadastrado.getNome());
        assertEquals(SituacaoEvento.PLANEJADO, cadastrado.getSituacao());
        verify(eventoDAO, times(1)).inserir(any(Evento.class));
    }

    @Test
    @DisplayName("Deve impedir alteração de evento quando situação for ENCERRADO")
    void deveImpedirAlteracaoEventoEncerrado() throws SQLException {
        Evento eventoEncerrado = new Evento();
        eventoEncerrado.setId(2L);
        eventoEncerrado.setSituacao(SituacaoEvento.ENCERRADO);

        when(eventoDAO.buscarPorId(2L)).thenReturn(Optional.of(eventoEncerrado));

        EventoDTO dto = new EventoDTO();
        dto.setId(2L);
        dto.setNome("Tentativa de Edição");
        dto.setDataEvento(LocalDate.now());
        dto.setHorario(LocalTime.of(20, 0));
        dto.setLocal("Local");
        dto.setCapacidadeTotal(500);

        assertThrows(IllegalStateException.class, () -> eventoService.atualizar(dto));
    }
}
