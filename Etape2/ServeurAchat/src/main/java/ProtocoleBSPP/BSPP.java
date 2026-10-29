package ProtocoleBSPP;

import ProtocoleBSPP.Reponse.*;
import ProtocoleBSPP.Requete.*;
import ServeurGeneriqueTCP.logg.Logger;
import model.dao.*;
import model.entity.*;
import model.viewmodel.BookSearchVM;
import model.viewmodel.ClientSearchVM;
import protocol.FinConnexionException;
import protocol.Protocole;
import protocol.Reponse;
import protocol.Requete;

import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Books Shopping Payment Protocol (BSPP).
 *
 * Le serveur est un serveur de REQUÊTES : chaque requête est traitée indépendamment,
 * aucune information de session n'est conservée (le numéro de client voyage dans la requête).
 * Une seule instance de BSPP (et donc de chaque DAO) est partagée par tous les threads du pool ;
 * l'atomicité des opérations sur le caddie est garantie par le verrou de la connexion unique.
 */
public class BSPP implements Protocole {

    private final Logger logger;
    private final ConnectDB db;
    private final BookDAO bookdao;
    private final EmployeeDAO employeedao;
    private final ClientDAO clientdao;
    private final CaddyDAO caddydao;
    private final Caddy_ItemDAO caddyItemdao;

    public BSPP(Logger logger) throws SQLException {
        this.logger = logger;
        this.db = ConnectDB.getInstance();      // connexion unique
        this.bookdao = new BookDAO(db);          // un seul exemplaire de chaque DAO
        this.employeedao = new EmployeeDAO(db);
        this.clientdao = new ClientDAO(db);
        this.caddydao = new CaddyDAO(db);
        this.caddyItemdao = new Caddy_ItemDAO(db);
    }

    @Override
    public String getNom() {
        return "BSPP";
    }

    @Override
    public Reponse TraiteRequete(Requete requete, Socket socket) throws FinConnexionException {
        if (requete == null) {
            logger.Trace("Requête reçue est null !");
            return new ReponseERROR("Requête vide ou non reconnue.");
        }
        try {
            //le test et le cast
            if (requete instanceof RequeteLOGIN r) return traiteLOGIN(r);
            if (requete instanceof Requete_CLIENT r) return traiteCLIENT(r);
            if (requete instanceof Requete_SELECT_BOOK r) return traiteSELECT_BOOK(r);
            if (requete instanceof Req_ADD_CADDY_ITEM r) return traiteADD_CADDY_ITEM(r);
            if (requete instanceof Req_DEL_CADDY_ITEM r) return traiteDEL_CADDY_ITEM(r);
            if (requete instanceof Requete_CANCEL_CADDY r) return traiteCANCEL_CADDY(r);
            if (requete instanceof Requete_PAY_CADDY r) return traitePAY_CADDY(r);
        } catch (SQLException e) {
            logger.Trace("Erreur SQL : " + e.getMessage());
            return new ReponseERROR("Erreur base de données : " + e.getMessage());
        }
        logger.Trace("Requête de type inconnu reçue : " + requete.getClass().getName());
        return new ReponseERROR("Type de requête non reconnu : " + requete.getClass().getSimpleName());
    }

    // ------------------------------------------------------------------ LOGIN
    private static final int longueur_chaine = 20;      // colonnes login / password : VARCHAR(20)


    private Reponse traiteLOGIN(RequeteLOGIN requete) throws SQLException {

        logger.Trace("LOGIN : " + requete);
        String login = requete.getLogin() == null ? "" : requete.getLogin().trim();
        String password = requete.getPassword() == null ? "" : requete.getPassword();
        System.out.println("je suis dans traitement login : " + login + login);
        if (login.isEmpty() || password.isEmpty()) {
            return new ReponseLOGIN(false, "Login et mot de passe obligatoires.");
        }
        if (login.length() > longueur_chaine || password.length() > longueur_chaine) {
            return new ReponseLOGIN(false, "Login et mot de passe : " + longueur_chaine + " caractères maximum.");
        }

        synchronized (db.getLock()) // vérification + création atomiques (pas de doublon)
        {
            Employee existant = employeedao.getByLogin(login);
            if (requete.isNewUser()) {
                if (existant != null) {
                    return new ReponseLOGIN(false, "Ce login existe déjà.");
                }
                boolean ok = employeedao.save(new Employee(null, login, password));
                return new ReponseLOGIN(ok, ok ? "Compte créé." : "Création du compte impossible.");
            }
            if (existant == null || !password.equals(existant.getPassword())) {
                return new ReponseLOGIN(false, "Login ou mot de passe incorrect.");
            }
            return new ReponseLOGIN(true, "Connecté.");
        }
    }

    // ------------------------------------------------------------------ CLIENT
    private Reponse traiteCLIENT(Requete_CLIENT requete) throws SQLException {
        logger.Trace("CLIENT : " + requete);
        String nom = requete.getNom() == null ? "" : requete.getNom().trim();
        String prenom = requete.getPrenom() == null ? "" : requete.getPrenom().trim();
        if (nom.isEmpty() || prenom.isEmpty()) {
            return new ReponseCLIENT(-1);
        }

        synchronized (db.getLock()) {   // recherche + création atomiques (pas de doublon)
            ArrayList<Client> clients = clientdao.load(new ClientSearchVM(null, nom, prenom, null));
            if (!clients.isEmpty()) {
                return new ReponseCLIENT(clients.get(0).getId());   // client existant
            }
            if (!requete.isNouveau()) {
                return new ReponseCLIENT(-1);                       // annoncé comme existant mais inconnu
            }
            Client nouveau = new Client(null, nom, prenom, null, true);
            if (!clientdao.save(nouveau)) {
                return new ReponseCLIENT(-1);
            }
            return new ReponseCLIENT(nouveau.getId());
        }
    }

    // ------------------------------------------------------------------ SELECT_BOOK
    private Reponse traiteSELECT_BOOK(Requete_SELECT_BOOK requete) throws SQLException {
        logger.Trace("SELECT_BOOK : " + requete);
        BookSearchVM vm = new BookSearchVM(requete.getId(), requete.getTitle(),
                requete.getLastNameAuthor(), requete.getFirstNameAuthor(),
                requete.getSubjectName(), requete.getPriceMax());
        ReponseSELECT_BOOK reponse = new ReponseSELECT_BOOK();
        for (Book b : bookdao.load(vm)) {
            reponse.addBook(b);
        }
        return reponse;
    }

    // ------------------------------------------------------------------ ADD_CADDY_ITEM
    private Reponse traiteADD_CADDY_ITEM(Req_ADD_CADDY_ITEM requete) throws SQLException {
        logger.Trace("ADD_CADDY_ITEM : " + requete);
        int qte = requete.getQuantity() == null ? 0 : requete.getQuantity();
        if (qte <= 0 || requete.getClientId() == null || requete.getLivreId() == null) {
            return new ReponseADD_CADDY_ITEM(false);
        }

        synchronized (db.getLock()) {
            Connection cn = db.getConnection();
            cn.setAutoCommit(false);
            try {
                if (clientdao.getById(requete.getClientId()) == null) {
                    cn.rollback();
                    return new ReponseADD_CADDY_ITEM(false);
                }
                Book book = bookdao.getById(requete.getLivreId());
                if (book == null || book.getStock_quantity() < qte) {      // stock insuffisant
                    cn.rollback();
                    return new ReponseADD_CADDY_ITEM(false);
                }

                // 1. le stock diminue
                if (!bookdao.updateStock(book.getId(), -qte)) {
                    cn.rollback();
                    return new ReponseADD_CADDY_ITEM(false);
                }

                // 2. caddie en cours (créé si c'est le premier caddy_item)
                Caddy caddy = caddydao.getCurrentByClientId(requete.getClientId());
                if (caddy == null) {
                    caddy = new Caddy(null, requete.getClientId(), LocalDate.now(), 0f, false);
                    if (!caddydao.save(caddy)) {
                        cn.rollback();
                        return new ReponseADD_CADDY_ITEM(false);
                    }
                }

                // 3. une ligne par livre : on additionne les quantités
                Caddy_items item = caddyItemdao.getByCaddyAndBook(caddy.getId(), book.getId());
                if (item == null) {
                    item = new Caddy_items(null, qte, caddy.getId(), book.getId());
                } else {
                    item.setQuantity(item.getQuantity() + qte);
                }
                if (!caddyItemdao.save(item)) {
                    cn.rollback();
                    return new ReponseADD_CADDY_ITEM(false);
                }

                // 4. montant du caddie
                caddy.setAmount(arrondi(caddy.getAmount() + book.getPrice() * qte));
                caddydao.save(caddy);

                cn.commit();
                return new ReponseADD_CADDY_ITEM(true);
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    // ------------------------------------------------------------------ DEL_CADDY_ITEM
    private Reponse traiteDEL_CADDY_ITEM(Req_DEL_CADDY_ITEM requete) throws SQLException {
        logger.Trace("DEL_CADDY_ITEM : " + requete);
        int qte = requete.getQuantity() == null ? 0 : requete.getQuantity();
        if (qte <= 0 || requete.getClientId() == null || requete.getLivreId() == null) {
            return new ReponseDEL_CADDY_ITEM(false);
        }

        synchronized (db.getLock()) {
            Connection cn = db.getConnection();
            cn.setAutoCommit(false);
            try {
                Caddy caddy = caddydao.getCurrentByClientId(requete.getClientId());
                Book book = bookdao.getById(requete.getLivreId());
                Caddy_items item = (caddy == null || book == null) ? null
                        : caddyItemdao.getByCaddyAndBook(caddy.getId(), book.getId());
                if (item == null || item.getQuantity() < qte) {
                    cn.rollback();
                    return new ReponseDEL_CADDY_ITEM(false);
                }

                // mise à jour ou suppression de la ligne
                if (item.getQuantity() == qte) {
                    caddyItemdao.delete(item.getId());
                } else {
                    item.setQuantity(item.getQuantity() - qte);
                    caddyItemdao.save(item);
                }
                // le stock est restitué
                bookdao.updateStock(book.getId(), qte);

                // montant ; un caddie vide disparaît
                if (caddyItemdao.getByCaddyId(caddy.getId()).isEmpty()) {
                    caddydao.delete(caddy.getId());
                } else {
                    caddy.setAmount(Math.max(0f, arrondi(caddy.getAmount() - book.getPrice() * qte)));
                    caddydao.save(caddy);
                }

                cn.commit();
                return new ReponseDEL_CADDY_ITEM(true);
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    // ------------------------------------------------------------------ CANCEL_CADDY
    private Reponse traiteCANCEL_CADDY(Requete_CANCEL_CADDY requete) throws SQLException {
        logger.Trace("CANCEL_CADDY : " + requete);
        if (requete.getClientId() == null) {
            return new ReponseCANCEL_CADDY(false);
        }

        synchronized (db.getLock()) {
            Connection cn = db.getConnection();
            cn.setAutoCommit(false);
            try {
                Caddy caddy = caddydao.getCurrentByClientId(requete.getClientId());
                if (caddy == null) {                       // rien à annuler : le client repart sans caddie
                    cn.rollback();
                    return new ReponseCANCEL_CADDY(true);
                }

                // restitution du stock, puis suppression des articles et du caddie
                for (Caddy_items item : caddyItemdao.getByCaddyId(caddy.getId())) {
                    bookdao.updateStock(item.getBookId(), item.getQuantity());
                }
                caddyItemdao.deleteByCaddyId(caddy.getId());
                boolean ok = caddydao.delete(caddy.getId());

                cn.commit();
                return new ReponseCANCEL_CADDY(ok);
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    // ------------------------------------------------------------------ PAY_CADDY
    private Reponse traitePAY_CADDY(Requete_PAY_CADDY requete) throws SQLException {
        logger.Trace("PAY_CADDY : " + requete);
        if (requete.getClientId() == null) {
            return new ReponsePay_CADDY(false);
        }

        synchronized (db.getLock()) {
            Caddy caddy = caddydao.getCurrentByClientId(requete.getClientId());
            if (caddy == null || caddyItemdao.getByCaddyId(caddy.getId()).isEmpty()) {
                return new ReponsePay_CADDY(false);        // pas de caddie (ou vide) à payer
            }
            caddy.setPayed(true);
            return new ReponsePay_CADDY(caddydao.save(caddy));
        }
    }

    private static float arrondi(float v) {
        return Math.round(v * 100f) / 100f;
    }
}
