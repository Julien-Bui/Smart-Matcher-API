package com.smartmatcher.repo;

import com.smartmatcher.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OfferRepo extends JpaRepository<Offer, Long> {
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM Offer o WHERE o.createdAt < :expiryDate")
    void deleteByCreatedAtBefore(@org.springframework.data.repository.query.Param("expiryDate") LocalDateTime expiryDate);
}
