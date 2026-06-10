package com.smartmatcher.repo;

import com.smartmatcher.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OfferRepo extends JpaRepository<Offer, Long> {
    void deleteByCreatedAtBefore(LocalDateTime expiryDate);
}
