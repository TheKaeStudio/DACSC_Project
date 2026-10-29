package model.dao;

import model.entity.Caddy_items;
import model.viewmodel.CaddyItemSearchVM;

import java.sql.*;
import java.util.ArrayList;

public class Caddy_ItemDAO {
    private final ConnectDB db;

    public Caddy_ItemDAO(ConnectDB db) {
        this.db = db;
    }

    private static Caddy_items map(ResultSet rs) throws SQLException {
        return new Caddy_items(rs.getInt("id"), rs.getInt("quantity"), rs.getInt("caddyId"), rs.getInt("bookId"));
    }

    public ArrayList<Caddy_items> loadAll() throws SQLException {
        return load(null);
    }

    /** Filtres optionnels sur le caddie, le livre et la quantité. */
    public ArrayList<Caddy_items> load(CaddyItemSearchVM vm) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT id, caddyId, bookId, quantity FROM caddy_items WHERE 1=1");
        ArrayList<Object> params = new ArrayList<>();
        if (vm != null) {
            if (vm.getIdCaddy() != null) {
                sql.append(" AND caddyId = ?");
                params.add(vm.getIdCaddy());
            }
            if (vm.getIdLivre() != null) {
                sql.append(" AND bookId = ?");
                params.add(vm.getIdLivre());
            }
            if (vm.getQuantite() != null) {
                sql.append(" AND quantity = ?");
                params.add(vm.getQuantite());
            }
        }
        sql.append(" ORDER BY id");

        ArrayList<Caddy_items> result = new ArrayList<>();
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

    public ArrayList<Caddy_items> getByCaddyId(Integer caddyId) throws SQLException {
        return load(new CaddyItemSearchVM(caddyId, null, null));
    }

    public Caddy_items getByCaddyAndBook(Integer caddyId, Integer bookId) throws SQLException {
        ArrayList<Caddy_items> l = load(new CaddyItemSearchVM(caddyId, bookId, null));
        return l.isEmpty() ? null : l.get(0);
    }

    /** Insère (id null) ou met à jour la ligne. */
    public boolean save(Caddy_items it) throws SQLException {
        if (it == null) return false;
        synchronized (db.getLock()) {
            Connection cn = db.getConnection();
            if (it.getId() == null) {
                try (PreparedStatement st = cn.prepareStatement(
                        "INSERT INTO caddy_items (caddyId, bookId, quantity) VALUES (?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    st.setInt(1, it.getCaddyId());
                    st.setInt(2, it.getBookId());
                    st.setInt(3, it.getQuantity());
                    if (st.executeUpdate() == 0) return false;
                    try (ResultSet k = st.getGeneratedKeys()) {
                        if (k.next()) it.setId(k.getInt(1));
                    }
                    return true;
                }
            }
            try (PreparedStatement st = cn.prepareStatement(
                    "UPDATE caddy_items SET caddyId=?, bookId=?, quantity=? WHERE id=?")) {
                st.setInt(1, it.getCaddyId());
                st.setInt(2, it.getBookId());
                st.setInt(3, it.getQuantity());
                st.setInt(4, it.getId());
                return st.executeUpdate() > 0;
            }
        }
    }

    public boolean delete(Integer id) throws SQLException {
        if (id == null) return false;
        synchronized (db.getLock()) {
            try (PreparedStatement st = db.getConnection().prepareStatement("DELETE FROM caddy_items WHERE id=?")) {
                st.setInt(1, id);
                return st.executeUpdate() > 0;
            }
        }
    }

    public boolean deleteByCaddyId(Integer caddyId) throws SQLException {
        synchronized (db.getLock()) {
            try (PreparedStatement st = db.getConnection().prepareStatement("DELETE FROM caddy_items WHERE caddyId=?")) {
                st.setInt(1, caddyId);
                st.executeUpdate();
                return true;
            }
        }
    }
}
