package ProtocoleBSPP.Requete;

import protocol.RequeteBSPP;

public class Requete_CLIENT extends RequeteBSPP {
    private static final long serialVersionUID = 1L;
    private String nom;
    private String prenom;
    private boolean nouveau;

    public Requete_CLIENT(String nom, String prenom, boolean nouveau) {
        this.nom = nom;
        this.prenom = prenom;
        this.nouveau = nouveau;
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

    public boolean isNouveau() {
        return nouveau;
    }

    public void setNouveau(boolean nouveau) {
        this.nouveau = nouveau;
    }

    @Override
    public String toString() {
        return "Requete_CLIENT{" +
                "nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", nouveau=" + nouveau +
                '}';
    }
}
