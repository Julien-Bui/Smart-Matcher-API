package com.smartmatcher.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

@Service
public class MistralAiService
{

    private final ChatClient chatClient;

    public MistralAiService(ChatClient.Builder builder)
    {
        this.chatClient = builder.build();
    }

    public String analyzeMatch(String cvText, String offerDescription)
    {
        String prompt = buildPrompt(cvText, offerDescription);

        // Utilisation de UserMessage pour éviter que Spring AI n'interprète les accolades du JSON

        return chatClient.prompt().messages(new UserMessage(prompt)).call().content();
    }

    public String buildPrompt(String cvText, String offerDescription)
    {
        return String.format(
                "Tu es un expert en recrutement. Analyse la correspondance entre\n" +
                        "le CV suivant et cette offre d'alternance.\n\n" +
                        "Évalue un score de pertinence entre 0 et 100 basé sur la correspondance des compétences.\n" +
                        "Retourne UNIQUEMENT un objet JSON valide avec cette structure exacte (sans bloc de code markdown) :\n"
                        +
                        "{\n" +
                        "    \"candidateName\": \"Nom complet du candidat trouvé dans le CV\",\n" +
                        "    \"score\": <entier entre 0 et 100>,\n" +
                        "    \"matchedSkills\": \"liste des compétences en commun\",\n" +
                        "    \"missingSkills\": \"liste des compétences manquantes\",\n" +
                        "    \"summary\": \"résumé de ton analyse expliquant le score\"\n" +
                        "}\n\n" +
                        "Ne retourne aucun autre texte que le JSON.\n\n" +
                        "=== CV ===\n" +
                        "%s\n\n" +
                        "=== OFFRE ===\n" +
                        "%s\n",
                cvText, offerDescription);
    }
}
