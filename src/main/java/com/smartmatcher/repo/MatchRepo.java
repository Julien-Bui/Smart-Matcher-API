package com.smartmatcher.repo;

import com.smartmatcher.model.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepo extends JpaRepository<MatchResult, Long>
{

    List<MatchResult> findByCandidateName(String candidateName);

    List<MatchResult> findByScoreGreaterThanEqual(int minScore);
}
