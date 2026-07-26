package com.smartmatcher.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String clientIp, String actionName) {
        String key = clientIp + "_" + actionName;
        return cache.computeIfAbsent(key, this::newBucket);
    }

    private Bucket newBucket(String key) {
        // If it's a PDF download, give a very high limit (e.g., 20 per minute) since it's cheap
        if (key.endsWith("_pdf_download")) {
            Refill refill = Refill.intervally(20, Duration.ofMinutes(1));
            Bandwidth limit = Bandwidth.classic(20, refill);
            return Bucket.builder().addLimit(limit).build();
        }
        
        // Otherwise, standard AI limit: 3 requêtes toutes les 5 minutes
        Refill refill = Refill.intervally(3, Duration.ofMinutes(5));
        Bandwidth limit = Bandwidth.classic(3, refill);
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
