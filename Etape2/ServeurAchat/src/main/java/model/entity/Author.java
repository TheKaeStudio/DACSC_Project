package model.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

public class Author implements Entity {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String last_name;

    private String first_name;

    private LocalDate birth_date;

    public Author() {
    }
    public Author(Integer id, String last_name, String first_name, LocalDate birt_date) {
        this.id = id;
        this.last_name = last_name;
        this.first_name = first_name;
        this.birth_date = birt_date;
    }



    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public LocalDate getBirt_date() {
        return birth_date;
    }

    public void setBirt_date(LocalDate birt_date) {
        this.birth_date = birt_date;
    }

    @Override
    public String toString() {
        return "Author{" +
                "id=" + id +
                ", last_name='" + last_name + '\'' +
                ", first_name='" + first_name + '\'' +
                ", birt_date='" + birth_date + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Author author = (Author) o;
        return Objects.equals(last_name, author.last_name) && Objects.equals(first_name, author.first_name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(last_name, first_name);
    }
}
