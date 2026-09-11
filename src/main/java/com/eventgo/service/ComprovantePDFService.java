package com.eventgo.service;

import com.eventgo.model.Ingresso;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ComprovantePDFService {

    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static byte[] gerarComprovantePDF(Ingresso ingresso) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        gerarDocumento(ingresso, baos);
        return baos.toByteArray();
    }

    public static void salvarComprovanteEmArquivo(Ingresso ingresso, Path caminhoDestino) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(caminhoDestino.toFile())) {
            gerarDocumento(ingresso, fos);
        }
    }

    private static void gerarDocumento(Ingresso ingresso, OutputStream out) throws Exception {
        Document document = new Document(PageSize.A6, 20, 20, 20, 20); // Tamanho compacto para ingresso
        PdfWriter.getInstance(document, out);
        document.open();

        // Estilos
        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(49, 46, 129));
        Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
        Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
        Font fontDestaque = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(15, 23, 42));
        Font fontCodigo = FontFactory.getFont(FontFactory.COURIER_BOLD, 10, new Color(99, 102, 241));
        Font fontRodape = FontFactory.getFont(FontFactory.HELVETICA, 7, Color.GRAY);

        // Header
        Paragraph pHeader = new Paragraph("🎟️ EVENTGO - INGRESSO OFICIAL", fontTitulo);
        pHeader.setAlignment(Element.ALIGN_CENTER);
        document.add(pHeader);

        Paragraph pSub = new Paragraph("Comprovante de Venda Presencial", fontSubtitulo);
        pSub.setAlignment(Element.ALIGN_CENTER);
        pSub.setSpacingAfter(10);
        document.add(pSub);

        // Tabela de Dados
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{35f, 65f});

        adicionarLinha(table, "Evento:", ingresso.getNomeEvento(), fontSubtitulo, fontDestaque);
        adicionarLinha(table, "Data & Hora:", ingresso.getDataEventoFormatada(), fontSubtitulo, fontNormal);
        adicionarLinha(table, "Local:", ingresso.getLocalEvento(), fontSubtitulo, fontNormal);
        adicionarLinha(table, "Setor:", ingresso.getNomeSetor(), fontSubtitulo, fontNormal);
        adicionarLinha(table, "Tipo:", ingresso.getNomeTipoIngresso(), fontSubtitulo, fontNormal);
        adicionarLinha(table, "Valor Pago:", MOEDA.format(ingresso.getPrecoPago()), fontSubtitulo, fontDestaque);

        if (ingresso.getNomeParticipante() != null && !ingresso.getNomeParticipante().isBlank()) {
            adicionarLinha(table, "Participante:", ingresso.getNomeParticipante(), fontSubtitulo, fontNormal);
            adicionarLinha(table, "CPF:", ingresso.getCpfParticipante(), fontSubtitulo, fontNormal);
        }

        document.add(table);

        // Caixa de Identificador Único (UUID)
        Paragraph pEspaco = new Paragraph(" ");
        document.add(pEspaco);

        PdfPTable boxCodigo = new PdfPTable(1);
        boxCodigo.setWidthPercentage(100);
        PdfPCell cellCodigo = new PdfPCell();
        cellCodigo.setBackgroundColor(new Color(241, 245, 249));
        cellCodigo.setBorderColor(new Color(203, 213, 225));
        cellCodigo.setPadding(8);
        cellCodigo.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph lblCod = new Paragraph("CÓDIGO DE IDENTIFICAÇÃO ÚNICO:", fontSubtitulo);
        lblCod.setAlignment(Element.ALIGN_CENTER);
        Paragraph valCod = new Paragraph(ingresso.getCodigo().toString(), fontCodigo);
        valCod.setAlignment(Element.ALIGN_CENTER);

        cellCodigo.addElement(lblCod);
        cellCodigo.addElement(valCod);
        boxCodigo.addCell(cellCodigo);
        document.add(boxCodigo);

        // Rodapé
        Paragraph pRodape = new Paragraph("\nEmitido localmente em " + LocalDateTime.now().format(DATA_HORA) +
                "\nApresente este documento na portaria para controle de entrada.", fontRodape);
        pRodape.setAlignment(Element.ALIGN_CENTER);
        document.add(pRodape);

        document.close();
    }

    private static void adicionarLinha(PdfPTable table, String rotulo, String valor, Font fRotulo, Font fValor) {
        PdfPCell c1 = new PdfPCell(new Phrase(rotulo, fRotulo));
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setPadding(3);

        PdfPCell c2 = new PdfPCell(new Phrase(valor != null ? valor : "-", fValor));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setPadding(3);

        table.addCell(c1);
        table.addCell(c2);
    }
}
