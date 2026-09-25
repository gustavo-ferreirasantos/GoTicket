package com.eventgo.service;

import com.eventgo.dao.VendaDAO;
import com.eventgo.dto.VendaRelatorioDTO;
import com.eventgo.model.enums.FormaPagamento;
import com.eventgo.model.enums.StatusVenda;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

public class RelatorioService {

    private final VendaDAO vendaDAO;
    private final IngressoService ingressoService;

    public RelatorioService() {
        this.vendaDAO = new VendaDAO();
        this.ingressoService = new IngressoService();
    }

    public RelatorioService(VendaDAO vendaDAO, IngressoService ingressoService) {
        this.vendaDAO = vendaDAO;
        this.ingressoService = ingressoService;
    }

    public List<VendaRelatorioDTO> listarVendas(Long eventoId, StatusVenda status, FormaPagamento formaPagamento) throws SQLException {
        return vendaDAO.listarRelatorio(eventoId, status, formaPagamento);
    }

    public void salvarComprovante(Long vendaId, FormaPagamento formaPagamento, Path caminhoDestino) throws Exception {
        List<com.eventgo.model.Ingresso> ingressos = ingressoService.listarPorVenda(vendaId);
        if (ingressos.isEmpty()) {
            throw new IllegalStateException("Esta venda não possui ingressos associados.");
        }

        List<java.util.UUID> codigos = ingressos.stream()
                .map(com.eventgo.model.Ingresso::getCodigo)
                .toList();

        ingressoService.salvarComprovantePDF(codigos, formaPagamento, caminhoDestino);
    }

    public byte[] emitirComprovante(Long vendaId, FormaPagamento formaPagamento) throws Exception {
        List<VendaRelatorioDTO> vendas = vendaDAO.listarRelatorio(null, null, null);
        VendaRelatorioDTO venda = vendas.stream()
                .filter(v -> v.getVendaId().equals(vendaId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada."));

        if (venda.getPrimeiroIngressoCodigo() == null) {
            throw new IllegalStateException("Esta venda não possui ingressos associados.");
        }

        return ingressoService.emitirComprovantePDF(venda.getPrimeiroIngressoCodigo(), formaPagamento);
    }

    public boolean imprimirComprovante(Long vendaId, FormaPagamento formaPagamento) throws Exception {
        List<VendaRelatorioDTO> vendas = vendaDAO.listarRelatorio(null, null, null);
        VendaRelatorioDTO venda = vendas.stream()
                .filter(v -> v.getVendaId().equals(vendaId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada."));

        if (venda.getPrimeiroIngressoCodigo() == null) {
            throw new IllegalStateException("Esta venda não possui ingressos associados.");
        }

        return ingressoService.imprimirIngressoLocal(venda.getPrimeiroIngressoCodigo(), formaPagamento);
    }
}
