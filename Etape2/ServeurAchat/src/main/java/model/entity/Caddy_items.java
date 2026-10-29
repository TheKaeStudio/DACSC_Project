package model.entity;

import java.io.Serializable;
import java.util.Objects;

public class Caddy_items implements Entity {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer quantity;
    private Integer caddyId;
    private Integer bookId;

    public Caddy_items() {
    }

    public Caddy_items(Integer id, Integer quantity, Integer caddyId, Integer bookid) {
        this.id = id;
        this.quantity = quantity;
        this.caddyId = caddyId;
        this.bookId = bookid;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getCaddyId() {
        return caddyId;
    }

    public void setCaddyId(Integer caddyId) {
        this.caddyId = caddyId;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    @Override
    public String toString() {
        return "Caddy_items{" +
                "id=" + id +
                ", quantity=" + quantity +
                ", caddyId=" + caddyId +
                ", bookid=" + bookId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Caddy_items that = (Caddy_items) o;
        return Objects.equals(id, that.id) && Objects.equals(quantity, that.quantity) && Objects.equals(caddyId, that.caddyId) && Objects.equals(bookId, that.bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, quantity, caddyId, bookId);
    }
}
