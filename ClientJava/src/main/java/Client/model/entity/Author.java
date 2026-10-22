package Client.model.entity;

import java.time.LocalDate;
import java.util.Objects;

public class Author {
    private Integer id;
    private String lastName;
    private String firstName;
    private String birthDate;

    public Author(Integer id, String lastName, String firstName, String date) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthDate = date;

    }

    public Author(Integer id, String lastName, String firstName) {  //pour l'affichage de l'auteur dans la comboBox sans date
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    @Override
    public String toString() {
        return getLastName() + " " + getFirstName();
    }
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Author author = (Author) o;
        return Objects.equals(id, author.id) && Objects.equals(lastName, author.lastName) && Objects.equals(firstName, author.firstName) && Objects.equals(birthDate, author.birthDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lastName, firstName, birthDate);
    }
}
