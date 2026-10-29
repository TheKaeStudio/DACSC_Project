package model.dao;

import model.entity.Subject;
import model.viewmodel.SubjectSearchVM;

import java.sql.*;
import java.util.ArrayList;

public class SubjectDAO {
    private final ConnectDB db;

    public SubjectDAO(ConnectDB db) {
        this.db = db;
    }

    public ArrayList<Subject> load() throws SQLException {
        return load(null);
    }

    /** id et name : égalité exacte. */
    public ArrayList<Subject> load(SubjectSearchVM vm) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT id, name FROM subjects WHERE 1=1");
        ArrayList<Object> params = new ArrayList<>();
        if (vm != null) {
            if (vm.getId() != null) {
                sql.append(" AND id = ?");
                params.add(vm.getId());
            }
            if (vm.getName() != null && !vm.getName().isEmpty()) {
                sql.append(" AND name = ?");
                params.add(vm.getName());
            }
        }
        sql.append(" ORDER BY name");

        ArrayList<Subject> result = new ArrayList<>();
        synchronized (db.getLock()) {
            try (PreparedStatement st = db.getConnection().prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) st.setObject(i + 1, params.get(i));
                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) result.add(new Subject(rs.getInt("id"), rs.getString("name")));
                }
            }
        }
        return result;
    }

    public Subject getById(Integer id) throws SQLException {
        if (id == null) return null;
        ArrayList<Subject> l = load(new SubjectSearchVM(id, null));
        return l.isEmpty() ? null : l.get(0);
    }

    public boolean save(Subject s) throws SQLException {
        if (s == null) return false;
        synchronized (db.getLock()) {
            Connection c = db.getConnection();
            if (s.getId() == null) {
                try (PreparedStatement st = c.prepareStatement("INSERT INTO subjects (name) VALUES (?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    st.setString(1, s.getName());
                    if (st.executeUpdate() == 0) return false;
                    try (ResultSet k = st.getGeneratedKeys()) {
                        if (k.next()) s.setId(k.getInt(1));
                    }
                    return true;
                }
            }
            try (PreparedStatement st = c.prepareStatement("UPDATE subjects SET name=? WHERE id=?")) {
                st.setString(1, s.getName());
                st.setInt(2, s.getId());
                return st.executeUpdate() > 0;
            }
        }
    }

    public boolean delete(Integer id) throws SQLException {
        if (id == null) return false;
        synchronized (db.getLock()) {
            try (PreparedStatement st = db.getConnection().prepareStatement("DELETE FROM subjects WHERE id=?")) {
                st.setInt(1, id);
                return st.executeUpdate() > 0;
            }
        }
    }
}
