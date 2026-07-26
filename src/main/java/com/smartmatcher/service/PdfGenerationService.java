package com.smartmatcher.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfGenerationService {

    public byte[] generatePdf(String content) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            
            document.addTitle("Lettre de Motivation");
            document.addCreationDate();
            
            String[] paragraphs = content.split("\n");
            for (String p : paragraphs) {
                if (p.trim().isEmpty()) {
                    document.add(new Paragraph(" "));
                } else {
                    document.add(new Paragraph(p.trim()));
                }
            }

            document.close();
            return outputStream.toByteArray();

        } catch (DocumentException e) {
            throw new RuntimeException("Erreur de génération du PDF : " + e.getMessage(), e);
        }
    }
}
