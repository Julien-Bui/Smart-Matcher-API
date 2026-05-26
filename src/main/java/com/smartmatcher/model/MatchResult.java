package com.smartmatcher.model;

import jakarta.persistence.*;

@Entity
@Table(name = "match_results")
public class MatchResult
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String candidateName;

    private int score;

    @Lob
    private String matchedSkills;

    @Lob
    private String missingSkills;

    @Lob
    private String summary;

    @Lob
    private String offerDescription;

    public MatchResult()
    {

    }

    public MatchResult(String candidateName, int score, String matchedSkills, String missingSkills, String summary,String offerDescription)
    {
        this.candidateName = candidateName;
        this.score = score;
        this.matchedSkills = matchedSkills;
        this.missingSkills = missingSkills;
        this.summary = summary;
        this.offerDescription = offerDescription;
    }

    public Long getId()
    {
        return this.id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getCandidateName()
    {
        return this.candidateName;
    }

    public void setCandidateName(String candidateName)
    {
        this.candidateName = candidateName;
    }

    public int getScore()
    {
        return this.score;
    }

    public void setScore(int score)
    {
        this.score = score;
    }

    public String getMatchedSkills()
    {
        return this.matchedSkills;
    }

    public void setMatchedSkills(String matchedSkills)
    {
        this.matchedSkills = matchedSkills;
    }

    public String getMissingSkills()
    {
        return this.missingSkills;
    }

    public void setMissingSkills(String missingSkills)
    {
        this.missingSkills = missingSkills;
    }

    public String getSummary()
    {
        return this.summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public String getOfferDescription()
    {
        return this.offerDescription;
    }

    public void setOfferDescription(String offerDescription)
    {
        this.offerDescription = offerDescription;
    }

}
