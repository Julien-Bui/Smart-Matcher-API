package com.smartmatcher.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartmatcher.model.MatchResult;
import com.smartmatcher.repo.MatchRepo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Moteur d'orchestration du matching.
 * Coordonne le flux : Parsing PDF → Analyse IA → Sauvegarde en BDD.
 */
@Service
public class MatchingEngine
{

    private final FileParsingService fileParsingService;
    private final MistralAiService mistralAiService;
    private final MatchRepo matchRepo;
    private final ObjectMapper objectMapper;

    public MatchingEngine(FileParsingService fileParsingService, MistralAiService mistralAiService, MatchRepo matchRepo)
    {
        this.fileParsingService = fileParsingService;
        this.mistralAiService = mistralAiService;
        this.matchRepo = matchRepo;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Orchestre le processus complet de matching.
     * Parsing PDF → Analyse IA → Parsing JSON → Sauvegarde en BDD.
     */
    public MatchResult processMatching(MultipartFile cvFile, String offerDescription)
    {
        String cvText = fileParsingService.extractText(cvFile);
        String aiResponse = mistralAiService.analyzeMatch(cvText, offerDescription);
        MatchResult result = parseAiResponse(aiResponse, offerDescription);
        return matchRepo.save(result);
    }

    /**
     * Parse la réponse JSON de Mistral AI et crée un objet MatchResult.
     * En cas d'erreur de parsing, retourne un MatchResult par défaut.
     */
    public MatchResult parseAiResponse(String aiResponse, String offerDescription)
    {
        try
        {
            String cleanJson = aiResponse.trim();
            if (cleanJson.startsWith("```json"))
            {
                cleanJson = cleanJson.substring(7);
            }
            else if (cleanJson.startsWith("```"))
            {
                cleanJson = cleanJson.substring(3);
            }
            if (cleanJson.endsWith("```"))
            {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();

            JsonNode json = objectMapper.readTree(cleanJson);
            String candidateName = json.has("candidateName") ? json.get("candidateName").asText() : "Inconnu";
            int score = json.has("score") ? json.get("score").asInt() : 0;
            String matchedSkills = json.has("matchedSkills") ? json.get("matchedSkills").asText() : "";
            String missingSkills = json.has("missingSkills") ? json.get("missingSkills").asText() : "";
            String summary = json.has("summary") ? json.get("summary").asText() : "";
            return new MatchResult(candidateName, score, matchedSkills, missingSkills, summary, offerDescription);
        }
        catch (Exception e)
        {
            System.err.println("Erreur de parsing de la réponse de l'IA: " + e.getMessage());
            System.err.println("Réponse brute reçue :\n" + aiResponse);
            return new MatchResult("Inconnu", 0, "", "", aiResponse, offerDescription);
        }
    }

    /**
     * Récupère tous les résultats de matching depuis la base de données.
     */
    public List<MatchResult> getAllResults() {
        return matchRepo.findAll();
    }
}
