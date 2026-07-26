package com.smartmatcher.service;

import com.smartmatcher.model.JobOffer;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.List;
import java.util.ArrayList;
import java.net.URI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;

@Service
public class AdzunaJobSearchImpl implements JobSearchProvider {
    
    @Value("${adzuna.app.id:placeholder_id}")
    private String appId;
    
    @Value("${adzuna.app.key:placeholder_key}")
    private String appKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<JobOffer> searchJobs(String keywords, String location, String contractType) {
        return searchJobs(keywords, location, contractType, 1);
    }
    
    public List<JobOffer> searchJobs(String keywords, String location, String contractType, int page) {
        List<JobOffer> offers = new ArrayList<>();
        if ("placeholder_id".equals(appId)) {
            return offers;
        }

        try {
            String[] allKeywords = keywords != null ? keywords.split(",") : new String[]{"developer"};
            String contractSuffix = (contractType != null && !contractType.trim().isEmpty() && !contractType.equalsIgnoreCase("Tous")) 
                                    ? " " + contractType.trim() : "";
            
            for (String keyword : allKeywords) {
                String searchTerm = keyword.trim();
                if (searchTerm.isEmpty()) continue;
                
                searchTerm += contractSuffix;
                
                offers = queryAdzuna(searchTerm, location, page);
                if (!offers.isEmpty()) break;
            }
            
            if (offers.isEmpty()) {
                offers = queryAdzuna("développeur" + contractSuffix, location, page);
            }
            
        } catch (Exception e) {
            System.err.println("Error calling Adzuna API: " + e.getMessage());
        }
        
        return offers;
    }
    
    private List<JobOffer> queryAdzuna(String keyword, String location, int page) {
        List<JobOffer> offers = new ArrayList<>();
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl("https://api.adzuna.com/v1/api/jobs/fr/search/" + page)
                    .queryParam("app_id", appId)
                    .queryParam("app_key", appKey)
                    .queryParam("results_per_page", 10)
                    .queryParam("what", keyword);
            
            if (location != null && !location.trim().isEmpty()) {
                builder.queryParam("where", location);
            }
            
            URI uri = builder.build().encode().toUri();
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
            
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode results = root.path("results");
            
            for (JsonNode node : results) {
                JobOffer offer = new JobOffer();
                offer.setTitle(node.path("title").asText());
                offer.setCompany(node.path("company").path("display_name").asText());
                offer.setDescription(node.path("description").asText());
                offer.setUrl(node.path("redirect_url").asText());
                offer.setSource("Adzuna");
                
                String offerLocation = node.path("location").path("display_name").asText("");
                if (!offerLocation.isEmpty()) {
                    offer.setTitle(offer.getTitle() + " — " + offerLocation);
                }
                
                offers.add(offer);
            }
        } catch (Exception e) {
            System.err.println("[ADZUNA] Erreur pour '" + keyword + "': " + e.getMessage());
        }
        return offers;
    }
}
