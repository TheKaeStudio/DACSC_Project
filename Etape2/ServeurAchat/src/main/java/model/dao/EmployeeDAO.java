package model.dao;

import model.entity.Employee;

import java.sql.*;

public class EmployeeDAO {
    private final ConnectDB db;

    public EmployeeDAO(ConnectDB db) {
        this.db = db;
    }

    public Employee getByLogin(String login) throws SQLException {
        synchronized (db.getLock()) {
            try (PreparedStatement st = db.getConnection().prepareStatement(
                    "SELECT id, identifiant, password FROM employees WHERE identifiant = ?")) {
                st.setString(1, login);
                try (ResultSet rs = st.executeQuery()) {
                    if (rs.next()) {
                        return new Employee(rs.getInt("id"), rs.getString("login"), rs.getString("password"));
                    }
                }
            }
        }
        return null;
    }

    //Vrai si le couple login / mot de passe existe.
    public boolean checkPassword(String login, String password) throws SQLException {
        Employee e = getByLogin(login);
        return e != null && e.getPassword() != null && e.getPassword().equals(password);
    }

    //Crée un employé (id null) ; la colonne identifiant est unique côté applicatif (voir BSPP).
    public boolean save(Employee e) throws SQLException {
        if (e == null || e.getId() != null) return false;
        synchronized (db.getLock()) {
            try (PreparedStatement st = db.getConnection().prepareStatement(
                    "INSERT INTO employees (identifiant, password) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                st.setString(1, e.getLog());
                st.setString(2, e.getPassword());
                if (st.executeUpdate() == 0) return false;
                try (ResultSet k = st.getGeneratedKeys()) {
                    if (k.next()) e.setId(k.getInt(1));
                }
                return true;
            }
        }
    }
}
