package model.dao;

import model.entity.Book;
import model.viewmodel.BookSearchVM;

import java.sql.*;
import java.util.ArrayList;

public class BookDAO {
    private static final String SELECT =
            "SELECT b.*, CONCAT(a.first_name, ' ', a.last_name) AS author_name, s.name AS subject_name "
                    + "FROM books b "
                    + "INNER JOIN authors a ON b.author_id = a.id "
                    + "INNER JOIN subjects s ON b.subject_id = s.id WHERE 1=1";

    private final ConnectDB db;

    public BookDAO(ConnectDB db) {
        this.db = db;
    }

    private static Book map(ResultSet rs) throws SQLException {
        Book b = new Book(rs.getInt("id"), rs.getInt("author_id"), rs.getInt("subject_id"),
                rs.getString("title"), rs.getString("isbn"), rs.getInt("page_count"),
                rs.getInt("stock_quantity"), rs.getFloat("price"), rs.getInt("publish_year"));
        b.setAuthorName(rs.getString("author_name"));
        b.setSubjectName(rs.getString("subject_name"));
        return b;
    }

    public ArrayList<Book> load() throws SQLException {
        return load(null);
    }

    /**
     * id : égalité ; title, lastName/firstName de l'auteur, name du sujet : LIKE ;
     * priceMax : prix inférieur ou égal.
     * Les paramètres sont ajoutés dans le MÊME ordre que les conditions du WHERE.
     */
    public ArrayList<Book> load(BookSearchVM vm) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT);
        ArrayList<Object> params = new ArrayList<>();
        if (vm != null) {
            if (vm.getId() != null) {
                sql.append(" AND b.id = ?");
                params.add(vm.getId());
            }
            if (vm.getTitle() != null && !vm.getTitle().isEmpty()) {
                sql.append(" AND b.title LIKE ?");
                params.add("%" + vm.getTitle() + "%");
            }
            if (vm.getLastNameAuthor() != null && !vm.getLastNameAuthor().isEmpty()) {
                sql.append(" AND a.last_name LIKE ?");
                params.add("%" + vm.getLastNameAuthor() + "%");
            }
            if (vm.getFirstNameAuthor() != null && !vm.getFirstNameAuthor().isEmpty()) {
                sql.append(" AND a.first_name LIKE ?");
                params.add("%" + vm.getFirstNameAuthor() + "%");
            }
            if (vm.getSubjectName() != null && !vm.getSubjectName().isEmpty()) {
                sql.append(" AND s.name LIKE ?");
                params.add("%" + vm.getSubjectName() + "%");
            }
            if (vm.getPriceMax() != null) {
                sql.append(" AND b.price <= ?");
                params.add(vm.getPriceMax());
            }
        }
        sql.append(" ORDER BY b.id");

        ArrayList<Book> result = new ArrayList<>();
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

    public Book getById(Integer id) throws SQLException {
        if (id == null) return null;
        ArrayList<Book> l = load(new BookSearchVM(id, null, null, null, null, null));
        return l.isEmpty() ? null : l.get(0);
    }

    /** Insère (id null) ou met à jour TOUS les champs du livre. */
    public boolean save(Book b) throws SQLException {
        if (b == null) return false;
        synchronized (db.getLock()) {
            Connection c = db.getConnection();
            if (b.getId() == null) {
                try (PreparedStatement st = c.prepareStatement(
                        "INSERT INTO books (author_id, subject_id, title, isbn, page_count, stock_quantity, price, publish_year) "
                                + "VALUES (?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    fill(st, b);
                    if (st.executeUpdate() == 0) return false;
                    try (ResultSet k = st.getGeneratedKeys()) {
                        if (k.next()) b.setId(k.getInt(1));
                    }
                    return true;
                }
            }
            try (PreparedStatement st = c.prepareStatement(
                    "UPDATE books SET author_id=?, subject_id=?, title=?, isbn=?, page_count=?, stock_quantity=?, price=?, publish_year=? WHERE id=?")) {
                fill(st, b);
                st.setInt(9, b.getId());
                return st.executeUpdate() > 0;
            }
        }
    }

    private static void fill(PreparedStatement st, Book b) throws SQLException {
        st.setInt(1, b.getAuthor_id());
        st.setInt(2, b.getSubject_id());
        st.setString(3, b.getTitle());
        st.setString(4, b.getIsbn());
        st.setInt(5, b.getPage_count());
        st.setInt(6, b.getStock_quantity());
        st.setFloat(7, b.getPrice());
        st.setInt(8, b.getPublish_year());
    }

    /** Ajoute delta (positif ou négatif) au stock. Refuse un stock qui deviendrait négatif. */
    public boolean updateStock(Integer bookId, int delta) throws SQLException {
        synchronized (db.getLock()) {
            try (PreparedStatement st = db.getConnection().prepareStatement(
                    "UPDATE books SET stock_quantity = stock_quantity + ? WHERE id = ? AND stock_quantity + ? >= 0")) {
                st.setInt(1, delta);
                st.setInt(2, bookId);
                st.setInt(3, delta);
                return st.executeUpdate() > 0;
            }
        }
    }

    public boolean delete(Integer id) throws SQLException {
        if (id == null) return false;
        synchronized (db.getLock()) {
            try (PreparedStatement st = db.getConnection().prepareStatement("DELETE FROM books WHERE id=?")) {
                st.setInt(1, id);
                return st.executeUpdate() > 0;
            }
        }
    }
}
