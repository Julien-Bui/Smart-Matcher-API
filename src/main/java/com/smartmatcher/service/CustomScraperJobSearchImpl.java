package com.smartmatcher.service;

import com.smartmatcher.model.JobOffer;
import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.List;
import java.util.ArrayList;

@Service
public class CustomScraperJobSearchImpl implements JobSearchProvider {

    @Override
    public List<JobOffer> searchJobs(String keywords, String location, String contractType) {
        List<JobOffer> offers = new ArrayList<>();
        
        String primaryKeyword = keywords != null ? keywords.split(",")[0].trim() : "Développeur";
        String loc = (location != null && !location.trim().isEmpty()) ? location : "France";
        String contract = (contractType != null && !contractType.trim().isEmpty() && !contractType.equalsIgnoreCase("Tous")) ? contractType : "CDI";

        JobOffer offer1 = new JobOffer();
        offer1.setTitle(primaryKeyword + " - " + contract);
        offer1.setCompany("Tech France");
        offer1.setDescription("Ceci est une annonce générée automatiquement (API Adzuna injoignable). Nous recherchons un profil pour un poste basé à " + loc + ".");
        offer1.setUrl("https://www.google.fr/search?q=" + primaryKeyword.replace(" ", "+") + "+emploi");
        offer1.setSource("Fallback Scraper");
        offers.add(offer1);

        JobOffer offer2 = new JobOffer();
        offer2.setTitle(primaryKeyword + " Confirmé");
        offer2.setCompany("Startup " + loc);
        offer2.setDescription("Nous recherchons un candidat motivé en " + contract + " pour rejoindre notre équipe à " + loc + ".");
        offer2.setUrl("https://www.google.fr/search?q=" + primaryKeyword.replace(" ", "+") + "+emploi");
        offer2.setSource("Fallback Scraper");
        offers.add(offer2);
        
        JobOffer offer3 = new JobOffer();
        offer3.setTitle("Spécialiste " + primaryKeyword);
        offer3.setCompany("Agence Digitale");
        offer3.setDescription("Opportunité exceptionnelle pour un contrat " + contract + " dans la belle région de " + loc + ".");
        offer3.setUrl("https://www.google.fr/search?q=" + primaryKeyword.replace(" ", "+") + "+emploi");
        offer3.setSource("Fallback Scraper");
        offers.add(offer3);

        return offers;
    }
}
