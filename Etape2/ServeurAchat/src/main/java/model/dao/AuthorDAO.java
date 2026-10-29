package model.dao;

import model.entity.Author;
import model.viewmodel.AuthorSearchVM;

import java.sql.*;
import java.util.ArrayList;

public class AuthorDAO {
    private final ConnectDB db;

    public AuthorDAO(ConnectDB db) {
        this.db = db;
    }

    private static Author map(ResultSet rs) throws SQLException {
        Date d = rs.getDate("birth_date");
        return new Author(rs.getInt("id"), rs.getString("last_name"), rs.getString("first_name"),
                d == null ? null : d.toLocalDate());
    }

    public ArrayList<Author> load() throws SQLException {
        return load(null);
    }

    /** id : égalité ; last_name / first_name : LIKE. */
    public ArrayList<Author> load(AuthorSearchVM vm) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM authors WHERE 1=1");
        ArrayList<Object> params = new ArrayList<>();
        if (vm != null) {
            if (vm.getId() != null) {
                sql.append(" AND id = ?");
                params.add(vm.getId());
            }
            if (vm.getLast_name() != null && !vm.getLast_name().isEmpty()) {
                sql.append(" AND last_name LIKE ?");
                params.add("%" + vm.getLast_name() + "%");
            }
            if (vm.getFirst_name() != null && !vm.getFirst_name().isEmpty()) {
                sql.append(" AND first_name LIKE ?");
                params.add("%" + vm.getFirst_name() + "%");
            }
        }
        sql.append(" ORDER BY last_name, first_name");

        ArrayList<Author> result = new ArrayList<>();
        synchronized (db.getLock()) {
            try (PreparedStatement st = db.getConnection().prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) st.setObject(i + 1, params.get(i));
                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) result.add(map(rs));
                }
            }
        }
        return result;
    }

    public Author getById(Integer id) throws SQLException {
        if (id == null) return null;
        ArrayList<Author> l = load(new AuthorSearchVM(id, null, null));
        return l.isEmpty() ? null : l.get(0);
    }

    /** Insère (id null) ou met à jour l'auteur. */
    public boolean save(Author a) throws SQLException {
        if (a == null) return false;
        synchronized (db.getLock()) {
            Connection c = db.getConnection();
            Date bd = a.getBirt_date() == null ? null : Date.valueOf(a.getBirt_date());
            if (a.getId() == null) {
                try (PreparedStatement st = c.prepareStatement(
                        "INSERT INTO authors (last_name, first_name, birth_date) VALUES (?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    st.setString(1, a.getLast_name());
                    st.setString(2, a.getFirst_name());
                    st.setDate(3, bd);
                    if (st.executeUpdate() == 0) return false;
                    try (ResultSet k = st.getGeneratedKeys()) {
                        if (k.next()) a.setId(k.getInt(1));
                    }
                    return true;
                }
            }
            try (PreparedStatement st = c.prepareStatement(
                    "UPDATE authors SET last_name=?, first_name=?, birth_date=? WHERE id=?")) {
                st.setString(1, a.getLast_name());
                st.setString(2, a.getFirst_name());
                st.setDate(3, bd);
                st.setInt(4, a.getId());
                return st.executeUpdate() > 0;
            }
        }
    }

    public boolean delete(Integer id) throws SQLException {
        if (id == null) return false;
        synchronized (db.getLock()) {
            try (PreparedStatement st = db.getConnection().prepareStatement("DELETE FROM authors WHERE id=?")) {
                st.setInt(1, id);
                return st.executeUpdate() > 0;
            }
        }
    }
}
