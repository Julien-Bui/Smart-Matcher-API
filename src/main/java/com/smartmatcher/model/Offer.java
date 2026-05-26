package com.smartmatcher.model;

import jakarta.persistence.*;

@Entity
@Table(name = "offers")
public class Offer
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    private String description;

    private String companyName;

    public Offer()
    {

    }

    /**
     * Constructeur avec paramètres.
     *
     * @param title       intitulé du poste
     * @param description description complète de l'offre
     * @param companyName nom de l'entreprise
     */
    public Offer(String title, String description, String companyName)
    {
        this.title = title;
        this.description = description;
        this.companyName = companyName;
    }

    public Long getId()
    {
        return this.id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return this.title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDescription()
    {
        return this.description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getCompanyName()
    {
        return this.companyName;
    }

    public void setCompanyName(String companyName)
    {
        this.companyName = companyName;
    }

}
