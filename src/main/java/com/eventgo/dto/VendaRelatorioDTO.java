package com.eventgo.dto;

import com.eventgo.model.enums.FormaPagamento;
import com.eventgo.model.enums.StatusVenda;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class VendaRelatorioDTO {
    private Long vendaId;
    private String eventoNome;
    private LocalDateTime dataVenda;
    private FormaPagamento formaPagamento;
    private BigDecimal valorTotal;
    private StatusVenda status;
    private int quantidadeIngressos;
    private UUID primeiroIngressoCodigo;

    public VendaRelatorioDTO() {}

    public Long getVendaId() { return vendaId; }
    public void setVendaId(Long vendaId) { this.vendaId = vendaId; }

    public String getEventoNome() { return eventoNome; }
    public void setEventoNome(String eventoNome) { this.eventoNome = eventoNome; }

    public LocalDateTime getDataVenda() { return dataVenda; }
    public void setDataVenda(LocalDateTime dataVenda) { this.dataVenda = dataVenda; }

    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public StatusVenda getStatus() { return status; }
    public void setStatus(StatusVenda status) { this.status = status; }

    public int getQuantidadeIngressos() { return quantidadeIngressos; }
    public void setQuantidadeIngressos(int quantidadeIngressos) { this.quantidadeIngressos = quantidadeIngressos; }

    public UUID getPrimeiroIngressoCodigo() { return primeiroIngressoCodigo; }
    public void setPrimeiroIngressoCodigo(UUID primeiroIngressoCodigo) { this.primeiroIngressoCodigo = primeiroIngressoCodigo; }
}
