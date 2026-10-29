package model.entity;

import java.io.Serializable;

public class Client implements Entity {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String nom;
    private String prenom;
    private String adresse;
    private Boolean isNew;

    public Client() {
    }

    public Client(Integer id, String last_name, String first_name, String adresse, Boolean isNew) {
        this.id = id;
        this.nom = last_name;
        this.prenom = first_name;
        this.adresse = adresse;
        this.isNew = isNew;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String first_name) {
        this.prenom = first_name;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public Boolean getNew() {
        return isNew;
    }

    public void setNew(Boolean aNew) {
        isNew = aNew;
    }
}
