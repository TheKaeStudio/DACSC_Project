package model.entity;

import java.io.Serializable;
import java.util.Objects;

public class Book implements Entity {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String title;
    private String isbn;
    private Integer page_count;

    private Integer stock_quantity;

    private Float price;

    private Integer publish_year;

    private Integer author_id;

    private Integer subject_id;
    // Informations d'affichage (remplies par BookDAO grâce aux jointures)
    private String authorName;
    private String subjectName;

    public Book() {
    }

    public Book(Integer id, Integer author_id, Integer subject_id, String title, String isbn, Integer page_count, Integer stock_quantity, Float price, Integer publish_year) {
        this.id = id;
        this.title = title;
        this.isbn = isbn;
        this.page_count = page_count;
        this.stock_quantity = stock_quantity;
        this.price = price;
        this.publish_year = publish_year;
        this.author_id = author_id;
        this.subject_id = subject_id;
    }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

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

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getPage_count() {
        return page_count;
    }

    public void setPage_count(Integer page_count) {
        this.page_count = page_count;
    }

    public Integer getStock_quantity() {
        return stock_quantity;
    }

    public void setStock_quantity(Integer stock_quantity) {
        this.stock_quantity = stock_quantity;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Integer getPublish_year() {
        return publish_year;
    }

    public void setPublish_year(Integer publish_year) {
        this.publish_year = publish_year;
    }

    public Integer getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(Integer author_id) {
        this.author_id = author_id;
    }

    public Integer getSubject_id() {
        return subject_id;
    }

    public void setSubject_id(Integer subject_id) {
        this.subject_id = subject_id;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", isbn='" + isbn + '\'' +
                ", page_count=" + page_count +
                ", stock_quantity=" + stock_quantity +
                ", price=" + price +
                ", publish_year='" + publish_year + '\'' +
                ", author_id=" + author_id +
                ", subject_id=" + subject_id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(title, book.title) && Objects.equals(isbn, book.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, isbn);
    }
}
