package ProtocoleBSPP.Requete;

import protocol.RequeteBSPP;

public class Requete_SELECT_BOOK extends RequeteBSPP {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String title;
    private String lastNameAuthor;
    private String firstNameAuthor;
    private String subjectName;
    private Float priceMax;



    public Requete_SELECT_BOOK(Integer id, String title,String lastName, String firstName,String nameSub, Float price) {
        this.id = id;
        this.title = title;
        this.lastNameAuthor= lastName;
        this.firstNameAuthor= firstName;
        this.subjectName = nameSub;
        this.priceMax = price;

    }

    /// ///////////////////////SETTERS//////////////////

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setLastNameAuthor(String lastNameAuthor) {
        this.lastNameAuthor = lastNameAuthor;
    }
    public void setFirstNameAuthor(String firstNameAuthor) {
        this.firstNameAuthor = firstNameAuthor;
    }
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
    public void setPriceMax(Float priceMax) {
        this.priceMax = priceMax;
    }


    /// /////////////////////////GETTERs////////////////////////
    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLastNameAuthor() {
        return lastNameAuthor;
    }

    public String getFirstNameAuthor() {
        return firstNameAuthor;
    }


    public String getSubjectName() {
        return subjectName;
    }

    public Float getPriceMax() {
        return priceMax;
    }


    /// //////////////////////////////////////////////

    @Override
    public String toString() {
        return "Requete_SELECT_BOOK{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", lastNameAuthor='" + lastNameAuthor + '\'' +
                ", firstNameAuthor='" + firstNameAuthor + '\'' +
                ", subjectName='" + subjectName + '\'' +
                ", priceMax=" + priceMax +
                '}';
    }

}
