package model.viewmodel;

public class ClientSearchVM {
    private Integer id;
    private String nom;
    private String prenom;
    private Boolean isNew;

    public ClientSearchVM() {
    }

    public ClientSearchVM(Integer id, String nom, String prenom, Boolean isNew) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
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

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public Boolean getNew() {
        return isNew;
    }

    public void setNew(Boolean aNew) {
        isNew = aNew;
    }
}
