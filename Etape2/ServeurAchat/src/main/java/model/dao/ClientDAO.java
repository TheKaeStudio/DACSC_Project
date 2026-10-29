package model.dao;

import model.entity.Client;
import model.viewmodel.ClientSearchVM;

import java.sql.*;
import java.util.ArrayList;

public class ClientDAO {
    private final ConnectDB db;

    public ClientDAO(ConnectDB db) {
        this.db = db;
    }

    private static Client map(ResultSet rs) throws SQLException {
        return new Client(rs.getInt("id"), rs.getString("nom"), rs.getString("prenom"),
                rs.getString("adresse"), false);
    }

    public ArrayList<Client> load() throws SQLException {
        return load(null);
    }

    /** id, nom et prénom : égalité (insensible à la casse avec la collation MySQL par défaut). */
    public ArrayList<Client> load(ClientSearchVM vm) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT id, nom, prenom, adresse FROM clients WHERE 1=1");
        ArrayList<Object> params = new ArrayList<>();
        if (vm != null) {
            if (vm.getId() != null) {
                sql.append(" AND id = ?");
                params.add(vm.getId());
            }
            if (vm.getNom() != null && !vm.getNom().isEmpty()) {
                sql.append(" AND nom = ?");
                params.add(vm.getNom());
            }
            if (vm.getPrenom() != null && !vm.getPrenom().isEmpty()) {
                sql.append(" AND prenom = ?");
                params.add(vm.getPrenom());
            }
        }
        sql.append(" ORDER BY id");

        ArrayList<Client> result = new ArrayList<>();
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

    public Client getById(Integer id) throws SQLException {
        if (id == null) return null;
        ArrayList<Client> l = load(new ClientSearchVM(id, null, null, null));
        return l.isEmpty() ? null : l.get(0);
    }

    public boolean save(Client c) throws SQLException {
        if (c == null) return false;
        synchronized (db.getLock()) {
            Connection cn = db.getConnection();
            if (c.getId() == null) {
                try (PreparedStatement st = cn.prepareStatement(
                        "INSERT INTO clients (nom, prenom, adresse) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    st.setString(1, c.getNom());
                    st.setString(2, c.getPrenom());
                    st.setString(3, c.getAdresse());
                    if (st.executeUpdate() == 0) return false;
                    try (ResultSet k = st.getGeneratedKeys()) {
                        if (k.next()) c.setId(k.getInt(1));
                    }
                    return true;
                }
            }
            try (PreparedStatement st = cn.prepareStatement(
                    "UPDATE clients SET nom=?, prenom=?, adresse=? WHERE id=?")) {
                st.setString(1, c.getNom());
                st.setString(2, c.getPrenom());
                st.setString(3, c.getAdresse());
                st.setInt(4, c.getId());
                return st.executeUpdate() > 0;
            }
        }
    }

    public boolean delete(Integer id) throws SQLException {
        if (id == null) return false;
        synchronized (db.getLock()) {
            try (PreparedStatement st = db.getConnection().prepareStatement("DELETE FROM clients WHERE id=?")) {
                st.setInt(1, id);
                return st.executeUpdate() > 0;
            }
        }
    }
}
