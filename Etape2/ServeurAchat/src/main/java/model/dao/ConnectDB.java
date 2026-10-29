package model.dao;

import ServeurGeneriqueTCP.utils.ConfigLoader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Connexion UNIQUE à la base de données (singleton), partagée par tous les DAO
 * et donc par tous les threads du serveur.
 *
 * Les accès concurrents sont protégés par un verrou unique ({@link #getLock()}) :
 * chaque méthode de DAO prend ce verrou, et les opérations métier composées de
 * plusieurs requêtes (ex : ajout au caddie) le prennent aussi pour rester atomiques.
 */
public final class ConnectDB {
    private static ConnectDB instance;

    private final Connection connection;
    private final Object lock = new Object();

    private ConnectDB() throws SQLException {
        ConfigLoader config = new ConfigLoader();
        String url = "jdbc:mysql://" + config.get("DB_HOST") + "/" + config.get("DB_NAME");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL introuvable", e);
        }
        connection = DriverManager.getConnection(url, config.get("DB_USER"), config.get("DB_PASSWORD"));
    }

    public static synchronized ConnectDB getInstance() throws SQLException {
        if (instance == null) {
            instance = new ConnectDB();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public Object getLock() {
        return lock;
    }

    public synchronized void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {
        }
        instance = null;
    }
}
