package com.smartmatcher.controller;

import com.smartmatcher.model.MatchResult;
import com.smartmatcher.service.MatchingEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import com.smartmatcher.service.RateLimitingService;
import io.github.bucket4j.Bucket;

import java.util.List;

/**
 * Contrôleur REST — Point d'entrée de l'API.
 * Expose les endpoints pour le matching et la consultation des résultats.
 */
@RestController
@RequestMapping("/api")
public class MatchController {

    private final MatchingEngine matchingEngine;
    private final RateLimitingService rateLimitingService;

    public MatchController(MatchingEngine matchingEngine, RateLimitingService rateLimitingService) {
        this.matchingEngine = matchingEngine;
        this.rateLimitingService = rateLimitingService;
    }

    /**
     * POST /api/match — Lance une analyse de matching CV vs Offre.
     */
    @PostMapping("/match")
    public ResponseEntity<?> match(
            @RequestParam("cv") MultipartFile cv,
            @RequestParam("description") String description,
            HttpServletRequest request) {
        try {
            // Rate Limiting check
            String clientIp = getClientIP(request);
            Bucket bucket = rateLimitingService.resolveBucket(clientIp);

            if (!bucket.tryConsume(1)) {
                return ResponseEntity.status(429)
                        .body("Erreur : Limite de 3 requêtes par 15 minutes atteinte. Veuillez patienter.");
            }
            // Vérifier si le fichier est vide
            if (cv.isEmpty()) {
                return ResponseEntity.badRequest().body("Erreur : Le fichier CV est vide.");
            }

            // Vérification pour s'assurer que c'est un vrai PDF
            try (java.io.InputStream is = cv.getInputStream()) {
                byte[] magic = new byte[4];
                if (is.read(magic) < 4 || magic[0] != 0x25 || magic[1] != 0x50 || magic[2] != 0x44
                        || magic[3] != 0x46) {
                    return ResponseEntity.badRequest()
                            .body("Erreur : Le fichier n'est pas un document PDF valide (signature incorrecte).");
                }
            }

            // Limiter la taille de la description pour éviter l'épuisement des tokens
            if (description == null || description.trim().isEmpty() || description.length() > 5000) {
                return ResponseEntity.badRequest().body(
                        "Erreur : La description de l'offre doit être non vide et ne doit pas dépasser 5000 caractères.");
            }

            MatchResult result = matchingEngine.processMatching(cv, description);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }

    private String getClientIP(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
