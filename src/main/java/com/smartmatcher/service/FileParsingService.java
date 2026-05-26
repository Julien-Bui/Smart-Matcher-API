package com.smartmatcher.service;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FileParsingService
{

    private final Tika tika = new Tika();

    public String extractText(MultipartFile file)
    {
        try
        {
            String text = tika.parseToString(file.getInputStream());
            System.out.println("Taille du texte extrait du PDF : " + text.length() + " caractères.");
            
            if (text == null || text.trim().isEmpty()) {
                throw new RuntimeException("Le PDF semble vide ou est une image scannée sans texte reconnaissable.");
            }
            
            return text;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Erreur lors de l'extraction du texte", e);
        }
    }

    public boolean isPdf(MultipartFile file)
    {
        String contentType = file.getContentType();
        return "application/pdf".equals(contentType);
    }

    public String cleanText(String rawText)
    {
        return rawText.replaceAll("\\n{2,}", "\n").trim();
    }
}
