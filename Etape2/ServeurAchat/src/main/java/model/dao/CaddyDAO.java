package model.dao;

import model.entity.Caddy;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class CaddyDAO {
    private static final String SELECT = "SELECT id, clientId, `date`, amount, payed FROM caddies";

    private final ConnectDB db;

    public CaddyDAO(ConnectDB db) {
        this.db = db;
    }

    private static Caddy map(ResultSet rs) throws SQLException {
        Date d = rs.getDate("date");
        return new Caddy(rs.getInt("id"), rs.getInt("clientId"), d == null ? null : d.toLocalDate(),
                rs.getFloat("amount"), rs.getBoolean("payed"));
    }

    public ArrayList<Caddy> loadAll() throws SQLException {
        ArrayList<Caddy> result = new ArrayList<>();
        synchronized (db.getLock()) {
            try (PreparedStatement st = db.getConnection().prepareStatement(SELECT + " ORDER BY id");
                 ResultSet rs = st.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public Caddy getById(Integer id) throws SQLException {
        if (id == null) return null;
        synchronized (db.getLock()) {
            try (PreparedStatement st = db.getConnection().prepareStatement(SELECT + " WHERE id = ?")) {
                st.setInt(1, id);
                try (ResultSet rs = st.executeQuery()) {
                    return rs.next() ? map(rs) : null;
                }
            }
        }
    }

    /** Caddie non payé en cours d'un client (null s'il n'en a pas). */
    public Caddy getCurrentByClientId(Integer clientId) throws SQLException {
        if (clientId == null) return null;
        synchronized (db.getLock()) {
            try (PreparedStatement st = db.getConnection().prepareStatement(
                    SELECT + " WHERE clientId = ? AND payed = FALSE ORDER BY id DESC LIMIT 1")) {
                st.setInt(1, clientId);
                try (ResultSet rs = st.executeQuery()) {
                    return rs.next() ? map(rs) : null;
                }
            }
        }
    }

    /** Insère (id null) ou met à jour le caddie. */
    public boolean save(Caddy c) throws SQLException {
        if (c == null || c.getClientId() == null) return false;
        Date date = Date.valueOf(c.getDate_creation() != null ? c.getDate_creation() : LocalDate.now());
        float amount = c.getAmount() == null ? 0f : c.getAmount();
        boolean payed = c.getPayed() != null && c.getPayed();
        synchronized (db.getLock()) {
            Connection cn = db.getConnection();
            if (c.getId() == null) {
                try (PreparedStatement st = cn.prepareStatement(
                        "INSERT INTO caddies (clientId, `date`, amount, payed) VALUES (?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    st.setInt(1, c.getClientId());
                    st.setDate(2, date);
                    st.setFloat(3, amount);
                    st.setBoolean(4, payed);
                    if (st.executeUpdate() == 0) return false;
                    try (ResultSet k = st.getGeneratedKeys()) {
                        if (k.next()) c.setId(k.getInt(1));
                    }
                    return true;
                }
            }
            try (PreparedStatement st = cn.prepareStatement(
                    "UPDATE caddies SET clientId=?, `date`=?, amount=?, payed=? WHERE id=?")) {
                st.setInt(1, c.getClientId());
                st.setDate(2, date);
                st.setFloat(3, amount);
                st.setBoolean(4, payed);
                st.setInt(5, c.getId());
                return st.executeUpdate() > 0;
            }
        }
    }

    public boolean delete(Integer id) throws SQLException {
        if (id == null) return false;
        synchronized (db.getLock()) {
            try (PreparedStatement st = db.getConnection().prepareStatement("DELETE FROM caddies WHERE id=?")) {
                st.setInt(1, id);
                return st.executeUpdate() > 0;
            }
        }
    }
}
