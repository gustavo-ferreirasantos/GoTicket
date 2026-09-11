package com.eventgo.service;

import com.eventgo.dao.LoteDAO;
import com.eventgo.dao.SetorDAO;
import com.eventgo.dao.TipoIngressoDAO;
import com.eventgo.dto.LoteDTO;
import com.eventgo.model.Lote;
import com.eventgo.model.Setor;
import com.eventgo.model.TipoIngresso;
import com.eventgo.model.enums.CategoriaIngresso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class LoteServiceTest {

    @Mock private LoteDAO loteDAO;
    @Mock private TipoIngressoDAO tipoIngressoDAO;
    @Mock private SetorDAO setorDAO;

    @InjectMocks private LoteService loteService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Deve impedir cadastro de lote que exceda a capacidade do setor (RN-02)")
    void deveImpedirLoteExcedendoCapacidadeSetor() throws SQLException {
        TipoIngresso tipo = new TipoIngresso(1L, 10L, "Inteira", CategoriaIngresso.INTEIRA);
        Setor setor = new Setor(10L, 100L, "Pista", 500); // Capacidade máx: 500

        when(tipoIngressoDAO.buscarPorId(1L)).thenReturn(Optional.of(tipo));
        when(setorDAO.buscarPorId(10L)).thenReturn(Optional.of(setor));
        when(loteDAO.somarQuantidadeTotalPorTipo(1L, null)).thenReturn(300); // Já existem 300

        LoteDTO dto = new LoteDTO();
        dto.setTipoIngressoId(1L);
        dto.setNumeroLote(2);
        dto.setPreco(new BigDecimal("100.00"));
        dto.setQuantidadeTotal(300); // 300 + 300 = 600 > 500 !
        dto.setDataInicio(LocalDate.now());
        dto.setDataFim(LocalDate.now().plusDays(10));

        assertThrows(IllegalArgumentException.class, () -> loteService.cadastrar(dto));
        verify(loteDAO, never()).inserir(any(Lote.class));
    }

    @Test
    @DisplayName("Deve cadastrar lote quando quantidade estiver dentro do limite do setor")
    void deveCadastrarLoteComSucesso() throws SQLException {
        TipoIngresso tipo = new TipoIngresso(1L, 10L, "Inteira", CategoriaIngresso.INTEIRA);
        Setor setor = new Setor(10L, 100L, "Pista", 500);

        when(tipoIngressoDAO.buscarPorId(1L)).thenReturn(Optional.of(tipo));
        when(setorDAO.buscarPorId(10L)).thenReturn(Optional.of(setor));
        when(loteDAO.somarQuantidadeTotalPorTipo(1L, null)).thenReturn(200);

        LoteDTO dto = new LoteDTO();
        dto.setTipoIngressoId(1L);
        dto.setNumeroLote(2);
        dto.setPreco(new BigDecimal("120.00"));
        dto.setQuantidadeTotal(200); // 200 + 200 = 400 <= 500
        dto.setDataInicio(LocalDate.now());
        dto.setDataFim(LocalDate.now().plusDays(10));

        when(loteDAO.inserir(any(Lote.class))).thenAnswer(i -> {
            Lote l = i.getArgument(0);
            l.setId(5L);
            return l;
        });

        Lote criado = loteService.cadastrar(dto);

        assertNotNull(criado);
        assertEquals(5L, criado.getId());
        assertEquals(200, criado.getQuantidadeDisponivel());
        verify(loteDAO, times(1)).inserir(any(Lote.class));
    }
}
