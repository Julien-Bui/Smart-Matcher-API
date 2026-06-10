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

            // Vérifier le type MIME du fichier (doit être un PDF)
            String contentType = cv.getContentType();
            if (contentType == null || !contentType.equals("application/pdf")) {
                return ResponseEntity.badRequest()
                        .body("Erreur : Le fichier doit être au format PDF. Type reçu : " + contentType);
            }

            // Vérifier l'extension du fichier par précaution
            String originalFilename = cv.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
                return ResponseEntity.badRequest().body("Erreur : L'extension du fichier doit être .pdf");
            }

            MatchResult result = matchingEngine.processMatching(cv, description);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }

    /**
     * GET /api/results — Récupère tous les résultats passés.
     */
    @GetMapping("/results")
    public ResponseEntity<?> getAllResults() {
        try {
            List<MatchResult> results = matchingEngine.getAllResults();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
