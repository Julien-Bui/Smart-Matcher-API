package com.smartmatcher.controller;

import com.smartmatcher.model.MatchResult;
import com.smartmatcher.service.MatchingEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Contrôleur REST — Point d'entrée de l'API.
 * Expose les endpoints pour le matching et la consultation des résultats.
 */
@RestController
@RequestMapping("/api")
public class MatchController
{

    private final MatchingEngine matchingEngine;

    public MatchController(MatchingEngine matchingEngine)
    {
        this.matchingEngine = matchingEngine;
    }

    /**
     * POST /api/match — Lance une analyse de matching CV vs Offre.
     */
    @PostMapping("/match")
    public ResponseEntity<?> match(
            @RequestParam("cv") MultipartFile cv,
            @RequestParam("description") String description)
            {
        try
        {
            MatchResult result = matchingEngine.processMatching(cv, description);
            return ResponseEntity.ok(result);
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }

    /**
     * GET /api/results — Récupère tous les résultats passés.
     */
    @GetMapping("/results")
    public ResponseEntity<?> getAllResults()
    {
        try
        {
            List<MatchResult> results = matchingEngine.getAllResults();
            return ResponseEntity.ok(results);
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }
}
