package com.smartmatcher.service;

import com.smartmatcher.model.JobOffer;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;

@Service
public class AggregatorJobSearchService {

    private final List<JobSearchProvider> providers;

    public AggregatorJobSearchService(List<JobSearchProvider> providers) {
        this.providers = providers;
    }

    public List<JobOffer> searchAll(String keywords, String location, String contractType, int page) {
        List<JobOffer> allOffers = new ArrayList<>();
        
        JobSearchProvider adzuna = providers.stream()
                .filter(p -> p instanceof AdzunaJobSearchImpl)
                .findFirst().orElse(null);
                
        if (adzuna != null) {
            List<JobOffer> adzunaOffers = ((AdzunaJobSearchImpl) adzuna).searchJobs(keywords, location, contractType, page);
            if (adzunaOffers != null) {
                allOffers.addAll(adzunaOffers);
            }
        }
        
        if (allOffers.isEmpty()) {
            JobSearchProvider scraper = providers.stream()
                    .filter(p -> p instanceof CustomScraperJobSearchImpl)
                    .findFirst().orElse(null);
            if (scraper != null) {
                List<JobOffer> scraperOffers = scraper.searchJobs(keywords, location, contractType);
                if (scraperOffers != null) {
                    allOffers.addAll(scraperOffers);
                }
            }
        }
        return allOffers;
    }
}
