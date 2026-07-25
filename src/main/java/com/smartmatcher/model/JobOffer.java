package com.smartmatcher.model;

public class JobOffer {
    private String title;
    private String company;
    private String description;
    private String url;
    private String source;

    public JobOffer() {}

    public JobOffer(String title, String company, String description, String url, String source) {
        this.title = title;
        this.company = company;
        this.description = description;
        this.url = url;
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
