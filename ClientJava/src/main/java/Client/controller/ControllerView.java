package Client.controller;

import Client.Protocol.OBEPClient;
import Client.model.entity.Author;
import Client.model.entity.Subject;
import Client.model.entity.User;
import Client.view.GUI.ViewClientEncodage;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.foreign.StructLayout;

public class ControllerView implements ActionListener {
    private final ViewClientEncodage view;
    private  String ip;

    private int port;
    private  User user;
    boolean verif;
    private OBEPClient metier;

    public ControllerView(ViewClientEncodage view, String ipaddr, int portServ) {

        this.view = view;
        ip=ipaddr;
        port= portServ;
        metier= new OBEPClient(ip, port);
    }
    public void run() {

        ((JFrame) view).setVisible(true); //on fait un casting parceque la vue est declaré comme obj de la classe ViewClientEncodage
    }
    @Override
    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();
        switch (cmd)
        {
            case "LOGIN":
                ouvrirLogin();
                break;

            case "LOGOUT":
                deconnecter();
                break;

            case "ADD_AUTHOR":
                if(!ajouterAuteur())
                {
                    view.showError("Erreur lors de l'ajout d'auteur");
                }
                break;

            case "ADD_SUBJECT":
                if(!ajouterSujet())
                {
                    view.showError("Erreur lors de l'ajout du sujet");
                }
                break;

            case "ADD_BOOK":

                if(!ajouterLivre())
                {
                    view.showError("Erreur lors de l'ajout du livre.");
                }
                break;

            case "VIDER":
                view.viderChamps();
                break;

            case "QUITTER":
                deconnecter();
                break;

            default:
                view.showError("Commande inconnue : " + cmd);
                break;
        }
    }

    public  boolean connecter()
    {
        if (user == null) {
            return false;
        }
        return metier.OBEP_Login(user.getName(), user.getPassword());
    }
    public void inscrire()
    {

    }

    private void ouvrirLogin() {
       user= view.promptUser();
        if (user == null) {
            return;
        }

        if (connecter()) {
            view.showMessage("Connexion réussie !");

            chargerAuteurs();
            chargerSujets();
            view.activerBoutons(true);

        } else {
            view.showError("Login ou mot de passe incorrect.");
        }

    }

    public void deconnecter() {
        metier.OBEP_Logout();
        view.close();
    }

    private boolean ajouterAuteur() {
        String lName = view.demanderLastnameAuteur();
        String fName = view.demanderFirstnameAuteur();
        String date= view.demanderBirthdayAuteur();
        if(lName==null || fName==null || date ==null)
        {
            return false; //Annulation par l'utilisateur
        }

        if (lName.isEmpty() ||fName.isEmpty() || date.isEmpty() ) {
            view.showError("Tout les champs sont obligatoire.");
            return false;
        }
        int idAuteur = metier.OBEP_add_author(lName,fName , date);

        System.out.println(idAuteur);
        if(idAuteur<0)
        {
            return false;
        }

        view.ajouterAuteurDansListe(new Author(idAuteur, lName, fName, date));
        view.showMessage("Auteur ajouté avec succès !");
        return true;
    }

    private boolean ajouterSujet() {
        String nom = view.demanderNomSujet();

        if (nom == null) {
            return true; // Annulation par l'utilisateur
        }

        nom = nom.trim();

        if (nom.isEmpty()) {
            view.showError("Le nom du sujet est obligatoire.");
            return false;
        }

        int idSujet = metier.OBEP_add_subject(nom);

        if (idSujet < 0) {
            return false;
        }

        view.ajouterSujetDansListe(new Subject(idSujet, nom));
        view.showMessage("Sujet ajouté avec succès !");

        return true;
    }

    private boolean ajouterLivre() {
        Author auteur = view.getAuteurSelectionne();
        Subject sujet = view.getSujetSelectionne();

        System.out.println(auteur.toString());
        System.out.println(sujet.toString());

        if (auteur == null || sujet == null) {
            view.showError("Sélectionne un auteur et un sujet.");
            return false;
        }

        try {
            String titre = view.getTitre();
            String isbn = view.getIsbn();
            System.out.println(titre);
            System.out.println(isbn);
            if (titre.isEmpty() || isbn.isEmpty()) {
                view.showError("Le titre et l'ISBN sont obligatoires.");
                return false;
            }

            int pages = view.getPages();
            int stock = view.getStock();
            System.out.println(pages);
            System.out.println(stock);

            float prix = view.getPrix();
            int annee = view.getAnnee();

            System.out.println(prix);
            System.out.println(annee);

            if (pages <= 0 || stock < 0 || prix < 0 || annee <= 0) {
                view.showError("Vérifie les valeurs numériques saisies.");
                return false;
            }

            int idBook = metier.OBEP_add_book(
                    auteur.getId(), sujet.getId(), titre,
                    isbn, pages, stock,prix, annee );

            if (idBook < 0) {
                return false;
            }

            // Ajouter le livre dans la JTable avec l'identifiant du serveur
            view.ajouterLivreDansTableau(  idBook, titre, auteur.toString(), sujet.toString(),
                    isbn, pages, annee, prix, stock);

            view.showMessage("Livre ajouté avec succès !");
            view.viderChamps();
            return true;

        } catch (NumberFormatException e) {
            view.showError( "Pages, stock, année et prix doivent contenir des valeurs valides.");
            return false;
        }
    }

    private void chargerAuteurs() {
        int nombre = metier.OBEP_Get_nb_authors();

        if (nombre < 0) {
            view.showError("Impossible de récupérer les auteurs.");
            return;
        }

        Author[] auteurs = new Author[nombre];

        int resultat = metier.OBEP_Get_authors(auteurs, nombre);

        if (resultat < 0) {
            view.showError("Erreur lors du chargement des auteurs.");
            return;
        }

        view.viderListeAuteurs();

        for (int i = 0; i < resultat; i++) {
            view.ajouterAuteurDansListe(auteurs[i]);
        }
    }

    private void chargerSujets() {
        int nombre = metier.OBEP_Get_nb_subjects();

        if (nombre < 0) {
            view.showError("Impossible de récupérer les sujets.");
            return;
        }

        Subject[] sujets = new Subject[nombre];

        int resultat = metier.OBEP_Get_subjects(sujets, nombre);

        if (resultat < 0) {
            view.showError("Erreur lors du chargement des sujets.");
            return;
        }

        view.viderListeSujets();

        for (int i = 0; i < resultat; i++) {
            view.ajouterSujetDansListe(sujets[i]);
        }
    }
}
