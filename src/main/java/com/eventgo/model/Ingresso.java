package com.eventgo.model;

import com.eventgo.model.enums.StatusIngresso;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Ingresso {
    private Long id;
    private UUID codigo;
    private Long vendaId;
    private Long loteId;
    private Long participanteId;
    private BigDecimal precoPago;
    private StatusIngresso status;
    private LocalDateTime dataCheckin;
    private String motivoCancelamento;
    private Long canceladoPor;
    private LocalDateTime dataCancelamento;
    private LocalDateTime criadoEm;

    // Campos enriquecidos para visualização em telas / emissão de comprovantes
    private String nomeEvento;
    private String dataEventoFormatada;
    private String localEvento;
    private String nomeSetor;
    private String nomeTipoIngresso;
    private String nomeParticipante;
    private String cpfParticipante;

    public Ingresso() {
        this.codigo = UUID.randomUUID();
        this.status = StatusIngresso.ATIVO;
        this.precoPago = BigDecimal.ZERO;
        this.criadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getCodigo() {
        return codigo;
    }

    public void setCodigo(UUID codigo) {
        this.codigo = codigo;
    }

    public Long getVendaId() {
        return vendaId;
    }

    public void setVendaId(Long vendaId) {
        this.vendaId = vendaId;
    }

    public Long getLoteId() {
        return loteId;
    }

    public void setLoteId(Long loteId) {
        this.loteId = loteId;
    }

    public Long getParticipanteId() {
        return participanteId;
    }

    public void setParticipanteId(Long participanteId) {
        this.participanteId = participanteId;
    }

    public BigDecimal getPrecoPago() {
        return precoPago;
    }

    public void setPrecoPago(BigDecimal precoPago) {
        this.precoPago = precoPago;
    }

    public StatusIngresso getStatus() {
        return status;
    }

    public void setStatus(StatusIngresso status) {
        this.status = status;
    }

    public LocalDateTime getDataCheckin() {
        return dataCheckin;
    }

    public void setDataCheckin(LocalDateTime dataCheckin) {
        this.dataCheckin = dataCheckin;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public void setMotivoCancelamento(String motivoCancelamento) {
        this.motivoCancelamento = motivoCancelamento;
    }

    public Long getCanceladoPor() {
        return canceladoPor;
    }

    public void setCanceladoPor(Long canceladoPor) {
        this.canceladoPor = canceladoPor;
    }

    public LocalDateTime getDataCancelamento() {
        return dataCancelamento;
    }

    public void setDataCancelamento(LocalDateTime dataCancelamento) {
        this.dataCancelamento = dataCancelamento;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public String getNomeEvento() {
        return nomeEvento;
    }

    public void setNomeEvento(String nomeEvento) {
        this.nomeEvento = nomeEvento;
    }

    public String getDataEventoFormatada() {
        return dataEventoFormatada;
    }

    public void setDataEventoFormatada(String dataEventoFormatada) {
        this.dataEventoFormatada = dataEventoFormatada;
    }

    public String getLocalEvento() {
        return localEvento;
    }

    public void setLocalEvento(String localEvento) {
        this.localEvento = localEvento;
    }

    public String getNomeSetor() {
        return nomeSetor;
    }

    public void setNomeSetor(String nomeSetor) {
        this.nomeSetor = nomeSetor;
    }

    public String getNomeTipoIngresso() {
        return nomeTipoIngresso;
    }

    public void setNomeTipoIngresso(String nomeTipoIngresso) {
        this.nomeTipoIngresso = nomeTipoIngresso;
    }

    public String getNomeParticipante() {
        return nomeParticipante;
    }

    public void setNomeParticipante(String nomeParticipante) {
        this.nomeParticipante = nomeParticipante;
    }

    public String getCpfParticipante() {
        return cpfParticipante;
    }

    public void setCpfParticipante(String cpfParticipante) {
        this.cpfParticipante = cpfParticipante;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingresso ingresso = (Ingresso) o;
        return Objects.equals(id, ingresso.id) && Objects.equals(codigo, ingresso.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, codigo);
    }
}
