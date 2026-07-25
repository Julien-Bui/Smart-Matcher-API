package com.smartmatcher.controller;

import com.smartmatcher.model.JobOffer;
import com.smartmatcher.service.AggregatorJobSearchService;
import com.smartmatcher.service.FileParsingService;
import com.smartmatcher.service.MistralAiService;
import com.smartmatcher.service.PdfGenerationService;
import com.smartmatcher.service.RateLimitingService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/extension")
public class JobSearchExtensionController {

    private final FileParsingService fileParsingService;
    private final MistralAiService mistralAiService;
    private final AggregatorJobSearchService aggregatorJobSearchService;
    private final PdfGenerationService pdfGenerationService;
    private final RateLimitingService rateLimitingService;

    public JobSearchExtensionController(FileParsingService fileParsingService,
                                        MistralAiService mistralAiService,
                                        AggregatorJobSearchService aggregatorJobSearchService,
                                        PdfGenerationService pdfGenerationService,
                                        RateLimitingService rateLimitingService) {
        this.fileParsingService = fileParsingService;
        this.mistralAiService = mistralAiService;
        this.aggregatorJobSearchService = aggregatorJobSearchService;
        this.pdfGenerationService = pdfGenerationService;
        this.rateLimitingService = rateLimitingService;
    }

    @PostMapping("/search-jobs")
    public ResponseEntity<?> searchJobs(@RequestParam("cv") MultipartFile cv,
                                        @RequestParam(value = "location", required = false) String location,
                                        @RequestParam(value = "contractType", required = false) String contractType,
                                        @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                        HttpServletRequest request) {
        try {
            Bucket bucket = rateLimitingService.resolveBucket(request.getRemoteAddr());
            if (!bucket.tryConsume(1)) {
                return ResponseEntity.status(429)
                        .body("Erreur : Limite de requêtes atteinte. Veuillez patienter.");
            }
            if (cv.isEmpty()) {
                return ResponseEntity.badRequest().body("Erreur : Le fichier CV est vide.");
            }
            String cvText = fileParsingService.extractText(cv);
            String skills = mistralAiService.extractProfile(cvText);
            List<JobOffer> offers = aggregatorJobSearchService.searchAll(skills, location, contractType, page);
            
            if (offers.isEmpty()) {
                return ResponseEntity.badRequest().body("Aucune offre trouvée pour les compétences : " + skills);
            }
            return ResponseEntity.ok(offers);
        } catch (Exception e) {
            System.err.println("[search-jobs] Erreur interne: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Erreur : Une erreur interne est survenue.");
        }
    }

    @PostMapping("/generate-cover-letter")
    public ResponseEntity<?> generateCoverLetter(@RequestParam("cv") MultipartFile cv, 
                                                 @RequestParam("jobDescription") String jobDescription,
                                                 HttpServletRequest request) {
        try {
            Bucket bucket = rateLimitingService.resolveBucket(request.getRemoteAddr());
            if (!bucket.tryConsume(1)) {
                return ResponseEntity.status(429)
                        .body("Erreur : Limite de requêtes atteinte. Veuillez patienter.");
            }
            if (cv.isEmpty() || jobDescription == null || jobDescription.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Erreur : CV ou description manquant.");
            }
            String cvText = fileParsingService.extractText(cv);
            String coverLetterText = mistralAiService.generateCoverLetter(cvText, jobDescription);
            
            return ResponseEntity.ok(Map.of("coverLetter", coverLetterText));
        } catch (Exception e) {
            System.err.println("[generate-cover-letter] Erreur interne: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Erreur : Une erreur interne est survenue.");
        }
    }
    
    @PostMapping("/download-pdf")
    public ResponseEntity<?> downloadPdf(@RequestParam("coverLetterText") String coverLetterText,
                                         @RequestParam(value = "companyName", defaultValue = "Entreprise") String companyName,
                                         HttpServletRequest request) {
        try {
            Bucket bucket = rateLimitingService.resolveBucket(request.getRemoteAddr());
            if (!bucket.tryConsume(1)) {
                return ResponseEntity.status(429)
                        .body("Erreur : Limite de requêtes atteinte. Veuillez patienter.");
            }
            if (coverLetterText == null || coverLetterText.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Erreur : Texte de la lettre manquant.");
            }
            byte[] pdfBytes = pdfGenerationService.generatePdf(coverLetterText);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String safeCompany = companyName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
            headers.setContentDispositionFormData("attachment", "Lettre_Motivation_" + safeCompany + ".pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            System.err.println("[download-pdf] Erreur interne: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Erreur : Une erreur interne est survenue.");
        }
    }
}
