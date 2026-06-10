package com.smartmatcher.service;

import com.smartmatcher.repo.MatchRepo;
import com.smartmatcher.repo.OfferRepo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DataCleanupService {

    private final MatchRepo matchRepo;
    private final OfferRepo offerRepo;

    public DataCleanupService(MatchRepo matchRepo, OfferRepo offerRepo) {
        this.matchRepo = matchRepo;
        this.offerRepo = offerRepo;
    }

    // Exécuté toutes les heures
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupOldData() {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        
        System.out.println("Début du nettoyage des données plus anciennes que : " + twentyFourHoursAgo);
        
        matchRepo.deleteByCreatedAtBefore(twentyFourHoursAgo);
        offerRepo.deleteByCreatedAtBefore(twentyFourHoursAgo);
        
        System.out.println("Nettoyage des données terminé.");
    }
}
