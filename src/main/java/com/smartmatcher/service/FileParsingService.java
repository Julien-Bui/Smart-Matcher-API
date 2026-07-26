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
            // Vérification de sécurité stricte : Magic Bytes du PDF
            try (java.io.InputStream is = file.getInputStream()) {
                byte[] magic = new byte[4];
                if (is.read(magic) < 4 || magic[0] != 0x25 || magic[1] != 0x50 || magic[2] != 0x44 || magic[3] != 0x46) {
                    throw new RuntimeException("Erreur de sécurité : Le fichier uploadé n'est pas un document PDF valide (signature incorrecte).");
                }
            }

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
