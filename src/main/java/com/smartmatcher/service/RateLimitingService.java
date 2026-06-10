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

    public Bucket resolveBucket(String clientIp) {
        return cache.computeIfAbsent(clientIp, this::newBucket);
    }

    private Bucket newBucket(String apiKey) {
        // 3 requêtes toutes les 15 minutes
        Refill refill = Refill.intervally(3, Duration.ofMinutes(15));
        Bandwidth limit = Bandwidth.classic(3, refill);
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
