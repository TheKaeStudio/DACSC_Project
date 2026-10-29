package model.entity;

import java.time.LocalDate;
import java.util.Objects;

public class Caddy implements Entity {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private LocalDate date_creation;
    private Float amount;
    private Boolean payed;
    private Integer clientId;

    public Caddy() {
    }
    public Caddy(Integer id, Integer clientId, LocalDate date, Float amount,  Boolean payed) {
        this.id = id;
        this.clientId = clientId;
        this.date_creation = date;
        this.amount = amount;
        this.payed = payed;

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getDate_creation() {
        return date_creation;
    }

    public void setDate_creation(LocalDate date_creation) {
        this.date_creation = date_creation;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public Boolean getPayed() {
        return payed;
    }

    public void setPayed(Boolean payed) {
        this.payed = payed;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    @Override
    public String toString() {
        return "Caddie{" +
                "id=" + id +
                ", date='" + date_creation + '\'' +
                ", amount=" + amount +
                ", payed=" + payed +
                ", clientId=" + clientId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Caddy caddie = (Caddy) o;
        return Objects.equals(id, caddie.id) && Objects.equals(date_creation, caddie.date_creation) && Objects.equals(amount, caddie.amount) && Objects.equals(clientId, caddie.clientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date_creation, amount, clientId);
    }
}
