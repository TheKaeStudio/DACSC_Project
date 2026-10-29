package controller;

import ProtocoleBSPP.Reponse.ReponseLOGIN;
import ProtocoleBSPP.Reponse.ReponseSELECT_BOOK;
import ProtocoleBSPP.Requete.Requete_SELECT_BOOK;
import model.ConnectServeur;
import model.entity.Book;
import view.GUI.ViewClientAchat;
import view.GUI.dialog.LoginDialog;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Contrôleur du client Achat : chaque bouton de la vue appelle une méthode ci-dessous,
 * qui envoie une requête BSPP au serveur (via ConnectServeur) puis met l'écran à jour.
 */
public class ControllerView implements ActionListener {

    private ViewClientAchat view;
    private ConnectServeur connectServeur;
    private LoginDialog loginDialog;

    private Integer clientId = null;                           // numéro du client en cours (null = aucun)
    private ArrayList<Book> livresAffiches = new ArrayList<>(); // livres de la table "recherche"
    private ArrayList<Book> panierLivres = new ArrayList<>();   // livres du panier ...
    private ArrayList<Integer> panierQuantites = new ArrayList<>(); // ... et leur quantité (même position)
    private Requete_SELECT_BOOK derniereRecherche = null;      // pour rafraîchir les stocks après un achat

    public ControllerView(ViewClientAchat view, ConnectServeur connectServeur) {
        this.view = view;
        this.connectServeur = connectServeur;
        this.loginDialog = new LoginDialog(view);
        this.loginDialog.setController(this);
    }

    public void run() {
        view.setVisible(true);
        afficherLogin();                 // le vendeur doit se connecter avant tout
    }

    // ------------------------------------------------------------ messages
    private Component parent() {
        if (loginDialog.isVisible()) {
            return loginDialog;
        }
        return view;
    }

    private void erreur(String message) {
        JOptionPane.showMessageDialog(parent(), message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    private void info(String message) {
        JOptionPane.showMessageDialog(parent(), message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void erreurServeur(IOException e) {
        erreur("Communication avec le serveur impossible (" + connectServeur.getHost() + ":"
                + connectServeur.getPort() + ") :\n" + e.getMessage());
    }

    // ------------------------------------------------------------ clic sur un bouton
    @Override
    public void actionPerformed(ActionEvent e) {
        String commande = e.getActionCommand();

        if (commande.equals("LOGIN_CONNEXION")) {
            authentifier(false);
        } else if (commande.equals("LOGIN_INSCRIPTION")) {
            authentifier(true);
        } else if (commande.equals("LOGIN_QUITTER")) {
            System.exit(0);
        } else if (commande.equals("DECONNEXION")) {
            deconnecter();
        } else if (commande.equals("RECHERCHER_CLIENT")) {
            identifierClient(false);
        } else if (commande.equals("ENREGISTRER_CLIENT")) {
            identifierClient(true);
        } else if (commande.equals("RECHERCHER_LIVRES")) {
            rechercherLivres();
        } else if (commande.equals("AJOUTER_PANIER")) {
            ajouterAuPanier();
        } else if (commande.equals("SUPPRIMER_ARTICLE")) {
            supprimerArticle();
        } else if (commande.equals("VIDER_PANIER")) {
            viderPanier();
        } else if (commande.equals("PAYER")) {
            payer();
        } else if (commande.equals("ANNULER")) {
            annuler();
        } else if (commande.equals("QUITTER")) {
            quitter();
        }
    }

    // ------------------------------------------------------------ affichage
    private void afficherLogin() {
        view.setVendeur(null);
        loginDialog.vider();
        loginDialog.setBoutonsActifs(true);
        loginDialog.setVisible(true);    // fenêtre modale : bloque la fenêtre principale
    }

    /** Réaffiche le panier dans la table et le total. */
    private void rafraichirPanier() {
        view.getPanierModel().setRowCount(0);
        double total = 0;
        for (int i = 0; i < panierLivres.size(); i++) {
            Book livre = panierLivres.get(i);
            int quantite = panierQuantites.get(i);
            double sousTotal = livre.getPrice() * quantite;
            total = total + sousTotal;
            view.getPanierModel().addRow(new Object[]{
                    livre.getId(), livre.getTitle(), String.format("%.2f", livre.getPrice()),
                    quantite, String.format("%.2f", sousTotal)});
        }
        view.setTotal(total);
    }

    /** Prêt pour un nouveau client. */
    private void reinitialiserClient() {
        clientId = null;
        panierLivres.clear();
        panierQuantites.clear();
        rafraichirPanier();
        view.setClientCourant(null);
        view.viderChampsClient();
    }

    private boolean clientRequis() {
        if (clientId == null) {
            erreur("Sélectionnez d'abord un client (Rechercher / Enregistrer client).");
            return false;
        }
        return true;
    }

    // ------------------------------------------------------------ LOGIN
    private void authentifier(boolean nouveau) {
        String login = loginDialog.getLogin();
        String password = loginDialog.getPassword();
        System.out.println("recu: " + login +"  " + password);
        if (login.isEmpty() || password.isEmpty()) {
            erreur("Veuillez encoder le login et le mot de passe.");
            return;
        }
        try {
            ReponseLOGIN reponse = connectServeur.login(login, password, nouveau);
            if (!reponse.isValide()) {
                erreur(reponse.getMessage());
                return;
            }
            view.setVendeur(login);
            loginDialog.setVisible(false);
            if (nouveau) {
                info("Compte créé : bienvenue " + login + " !");
            }
        } catch (IOException e) {
            erreurServeur(e);
        }
    }

    private void deconnecter() {
        // si un client était en cours, on rend le stock de son panier avant de partir
        if (clientId != null && !panierLivres.isEmpty()) {
            try {
                connectServeur.cancelCaddy(clientId);
            } catch (IOException e) {
                erreurServeur(e);
                return;
            }
        }
        reinitialiserClient();
        view.viderLivresAffiches();
        livresAffiches.clear();
        afficherLogin();
    }

    // ------------------------------------------------------------ CLIENT
    private void identifierClient(boolean nouveau) {
        String nom = view.getNomClient();
        String prenom = view.getPrenomClient();
        if (nom.isEmpty() || prenom.isEmpty()) {
            erreur("Veuillez encoder le nom et le prénom du client.");
            return;
        }
        if (!panierLivres.isEmpty()) {
            erreur("Le panier du client en cours n'est pas terminé (payez ou annulez d'abord).");
            return;
        }
        try {
            int id = connectServeur.client(nom, prenom, nouveau);
            if (id < 0) {
                if (nouveau) {
                    erreur("Impossible d'enregistrer ce client.");
                } else {
                    erreur("Client inconnu : utilisez « Enregistrer client » pour un nouveau client.");
                }
                return;
            }
            clientId = id;
            view.setClientCourant(id);
            info("Client n° " + id);
        } catch (IOException e) {
            erreurServeur(e);
        }
    }

    // ------------------------------------------------------------ SELECT_BOOK
    private void rechercherLivres() {
        double prixMax = view.getPrixMaximum();
        Float prixMaxOuNull = null;
        if (prixMax > 0) {
            prixMaxOuNull = (float) prixMax;
        }
        derniereRecherche = new Requete_SELECT_BOOK(null,
                vide(view.getTitreRecherche()),
                vide(view.getAuteurNomRecherche()),
                vide(view.getAuteurPrenomRecherche()),
                vide(view.getSujetRecherche()),
                prixMaxOuNull);
        lancerRecherche();
    }

    /** Un champ vide n'est pas un critère : on envoie null. */
    private String vide(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        return s;
    }

    private void lancerRecherche() {
        if (derniereRecherche == null) {
            return;
        }
        try {
            ReponseSELECT_BOOK reponse = connectServeur.selectBook(derniereRecherche);
            livresAffiches = reponse.getListBook();
            view.getLivresModel().setRowCount(0);
            for (Book b : livresAffiches) {
                view.getLivresModel().addRow(new Object[]{
                        b.getId(), b.getTitle(), b.getAuthorName(), b.getSubjectName(),
                        String.format("%.2f", b.getPrice()), b.getStock_quantity()});
            }
        } catch (IOException e) {
            erreurServeur(e);
        }
    }

    // ------------------------------------------------------------ ADD_CADDY_ITEM
    private void ajouterAuPanier() {
        if (!clientRequis()) {
            return;
        }
        int ligne = view.getLigneLivreSelectionnee();
        if (ligne < 0 || ligne >= livresAffiches.size()) {
            erreur("Sélectionnez un livre dans la liste.");
            return;
        }
        Book livre = livresAffiches.get(ligne);
        int quantite = view.getQuantite();

        try {
            boolean ok = connectServeur.addCaddyItem(clientId, livre.getId(), quantite);
            if (!ok) {
                erreur("Stock insuffisant pour « " + livre.getTitle() + " ».");
            } else {
                // le livre est-il déjà dans le panier ?
                int position = -1;
                for (int i = 0; i < panierLivres.size(); i++) {
                    if (panierLivres.get(i).getId().equals(livre.getId())) {
                        position = i;
                    }
                }
                if (position == -1) {
                    panierLivres.add(livre);
                    panierQuantites.add(quantite);
                } else {
                    panierQuantites.set(position, panierQuantites.get(position) + quantite);
                }
                rafraichirPanier();
            }
            lancerRecherche();           // met à jour les stocks affichés
        } catch (IOException e) {
            erreurServeur(e);
        }
    }

    // ------------------------------------------------------------ DEL_CADDY_ITEM
    private void supprimerArticle() {
        if (!clientRequis()) {
            return;
        }
        int ligne = view.getLignePanierSelectionnee();
        if (ligne < 0 || ligne >= panierLivres.size()) {
            erreur("Sélectionnez un article dans le panier.");
            return;
        }
        Book livre = panierLivres.get(ligne);
        int quantite = panierQuantites.get(ligne);

        try {
            boolean ok = connectServeur.delCaddyItem(clientId, livre.getId(), quantite);
            if (!ok) {
                erreur("Le serveur a refusé la suppression de cet article.");
                return;
            }
            panierLivres.remove(ligne);
            panierQuantites.remove(ligne);
            rafraichirPanier();
            lancerRecherche();
        } catch (IOException e) {
            erreurServeur(e);
        }
    }

    ///// CANCEL_CADDY///////////
    private void viderPanier() {
        if (!clientRequis()) {
            return;
        }
        try {
            boolean ok = connectServeur.cancelCaddy(clientId);
            if (!ok) {
                erreur("Le serveur n'a pas pu vider le panier.");
                return;
            }
            panierLivres.clear();
            panierQuantites.clear();
            rafraichirPanier();
            lancerRecherche();
        } catch (IOException e) {
            erreurServeur(e);
        }
    }

    private void annuler() {
        if (!clientRequis()) {
            return;
        }
        try {
            boolean ok = connectServeur.cancelCaddy(clientId);
            if (!ok) {
                erreur("Le serveur n'a pas pu annuler le panier.");
                return;
            }
            reinitialiserClient();
            lancerRecherche();
        } catch (IOException e) {
            erreurServeur(e);
        }
    }

    // ------------------------------------------------------------ PAY_CADDY
    private void payer() {
        if (!clientRequis()) {
            return;
        }
        if (panierLivres.isEmpty()) {
            erreur("Le panier est vide.");
            return;
        }
        try {
            boolean ok = connectServeur.payCaddy(clientId);
            if (!ok) {
                erreur("Le paiement a été refusé par le serveur.");
                return;
            }
            info("Paiement enregistré. Prêt pour un nouveau client.");
            reinitialiserClient();
        } catch (IOException e) {
            erreurServeur(e);
        }
    }

    // ------------------------------------------------------------ QUITTER
    private void quitter() {
        // on rend le stock du panier en cours avant de quitter
        if (clientId != null && !panierLivres.isEmpty()) {
            try {
                connectServeur.cancelCaddy(clientId);
            } catch (IOException e) {
                // serveur injoignable : on quitte quand même
            }
        }
        System.exit(0);
    }
}