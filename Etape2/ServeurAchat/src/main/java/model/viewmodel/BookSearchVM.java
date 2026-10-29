package model.viewmodel;

public class BookSearchVM {
    private Integer id;
    private String title;
    private String lastNameAuthor;
    private String firstNameAuthor;
    private String subjectName;
    private Float priceMax;


    public BookSearchVM() {
    }

    public BookSearchVM(Integer id, String title, String lastNameAuthor, String firstNameAuthor, String subjectName, Float priceMax) {
        this.id = id;
        this.title = title;
        this.lastNameAuthor = lastNameAuthor;
        this.firstNameAuthor = firstNameAuthor;
        this.subjectName = subjectName;
        this.priceMax = priceMax;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public Float getPriceMax() {
        return priceMax;
    }

    public void setPriceMax(Float price) {
        this.priceMax = price;
    }

    public String getLastNameAuthor() {
        return lastNameAuthor;
    }

    public void setLastNameAuthor(String lastNameAuthor) {
        this.lastNameAuthor = lastNameAuthor;
    }

    public String getFirstNameAuthor() {
        return firstNameAuthor;
    }

    public void setFirstNameAuthor(String firstNameAuthor) {
        this.firstNameAuthor = firstNameAuthor;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
}
