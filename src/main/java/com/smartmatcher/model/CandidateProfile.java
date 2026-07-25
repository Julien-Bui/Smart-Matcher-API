package com.smartmatcher.model;

import java.util.List;

public class CandidateProfile {
    private String fullName;
    private List<String> skills;
    private String summary;

    public CandidateProfile() {}

    public CandidateProfile(String fullName, List<String> skills, String summary) {
        this.fullName = fullName;
        this.skills = skills;
        this.summary = summary;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
