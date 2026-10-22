package Client.Protocol;

import Client.model.entity.Author;
import Client.model.entity.Subject;

import java.util.Arrays;

public class OBEPClient {
    private final TCP tcp= new TCP();

    public OBEPClient(String ip, int port) {

        tcp.connecter(ip, port);
    }

    // =========================================================
    // ECHANGE : envoie une requête et récupère la réponse
    // =========================================================

    public String echange(String requete) {
        if (requete == null || !tcp.send(requete)) {
            tcp.fermer();
            return null;
        }

        // Attendre et recevoir la réponse
        String reponse = tcp.receive();


        if (reponse == null) {
            tcp.fermer();
            return null;
        }

        return reponse;
    }
    /// /////////////////////////LOGIN///////////////////////////////////////
    public boolean OBEP_Login(String log, String pwd) {
        if (log == null || pwd == null) {
            return false;
        }

        String requete = "LOGIN#" + log + "#" + pwd + "#";
        String reponse = echange(requete);


        if (reponse == null) {
            return false;
        }

        String[] token = reponse.split("#", -1);

        return token.length >= 2&& token[0].equals("LOGIN") && token[1].equals("ok");
    }

    /// /////////////////////////LOGOUT////////////////////////////////////////////
    public boolean OBEP_Logout() {
        String reponse = echange("LOGOUT");

        if (reponse == null) {
            return false;
        }

        String[] token = reponse.split("#", -1);

        return token.length >= 2
                && token[0].equals("LOGOUT")
                && token[1].equals("ok");
    }

    /// ////////////////////////////////////////////////////////////////////////////////
    public int OBEP_Get_nb_authors() {

        String reponse = echange("GET_AUTHORS_nb#");

        if (reponse == null) {
            return -1;
        }

        String[] token = reponse.split("#", -1);


        if (token.length < 2 || !token[0].equals("GET_AUTHORS_nb") || !token[1].equals("ok"))
        {
            return -1;
        }

        try {
            return Integer.parseInt(token[2]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    // =========================================================
    // NOMBRE DE SUJETS
    // =========================================================

    public int OBEP_Get_nb_subjects() {

        String reponse = echange("GET_SUBJECTS_nb#");

        if (reponse == null) {
            return -1;
        }

        String[] token = reponse.split("#", -1);

        if (token.length < 2 || !token[0].equals("GET_SUBJECTS_nb") || !token[1].equals("ok")) {
            return -1;
        }

        try {
            return Integer.parseInt(token[2]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // =========================================================
    // RECUPERER LES AUTEURS
    // =========================================================

    public int OBEP_Get_authors(Author[] authors, int maxAuthors) {
        if (authors == null || maxAuthors < 0) {
            return -1;
        }

        String reponse = echange("GET_AUTHORS#");

        if (reponse == null) {
            return -1;
        }

        String[] token = reponse.split("#", -1);

        if (token.length < 3 || !token[0].equals("GET_AUTHORS") || !token[1].equals("ok"))
        {
            return -1;
        }

        try {
            int nombre = Integer.parseInt(token[2]);

            if (nombre < 0) {
                return -1;
            }

            int taille = Math.min(nombre,
                    Math.min(maxAuthors, authors.length));

            // Chaque auteur possède 3 champs : id, nom, prénom.
            if (token.length < 3 + nombre * 3) {
                return -1;
            }

            int j = 3;

            for (int i = 0; i < taille; i++) {
                int id = Integer.parseInt(token[j]);
                String nom = token[j + 1];
                String prenom = token[j + 2];

                authors[i] = new Author(id, nom, prenom);

                j += 3;
            }

            return taille;

        } catch (NumberFormatException e) {
            return -1;
        }
    }
    // =========================================================
    // RECUPERER LES SUJETS
    // =========================================================

    public int OBEP_Get_subjects(Subject[] subjects, int maxSubjects) {
        if (subjects == null || maxSubjects < 0) {
            return -1;
        }

        String reponse = echange("GET_SUBJECTS#");

        if (reponse == null) {
            return -1;
        }

        String[] token = reponse.split("#", -1);

        if (token.length < 3 || !token[0].equals("GET_SUBJECTS") || !token[1].equals("ok")) {
            return -1;
        }

        try {
            int nombre = Integer.parseInt(token[2]);

            if (nombre < 0) {
                return -1;
            }

            int taille = Math.min(nombre,Math.min(maxSubjects, subjects.length));

            // Chaque sujet possède 2 champs : id et nom.
            if (token.length < 3 + nombre * 2) {
                return -1;
            }

            int j = 3;

            for (int i = 0; i < taille; i++) {
                int id = Integer.parseInt(token[j]);
                String nom = token[j + 1];

                subjects[i] = new Subject(id, nom);

                j += 2;
            }

            return taille;

        } catch (NumberFormatException e) {
            return -1;
        }
    }


    // =========================================================
    // RECHERCHER UN AUTEUR
    // =========================================================

    public int OBEP_Get_author(String lastName, String firstName) {
        if (lastName == null || firstName == null) {
            return -1;
        }

        String requete = "GET_AUTHOR#" + lastName + "#" + firstName + "#";
        String reponse = echange(requete);

        if (reponse == null) {
            return -1;
        }

        String[] token = reponse.split("#", -1);

        if (token.length < 3 || !token[0].equals("GET_AUTHOR") || !token[1].equals("ok")) {
            return -1;
        }

        try {
            return Integer.parseInt(token[2]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // =========================================================
    // RECHERCHER UN SUJET
    // =========================================================
    public int OBEP_Get_subject(String name)
    {
        if (name == null) {
            return -1;
        }

        String requete = "GET_SUBJECT#" + name + "#";
        String reponse = echange(requete);

        if (reponse == null) {
            return -1;
        }

        String[] token = reponse.split("#", -1);

        if (token.length < 3 || !token[0].equals("GET_SUBJECT") || !token[1].equals("ok")) {
            return -1;
        }

        try {
            return Integer.parseInt(token[2]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }


    // =========================================================
    // AJOUTER UN AUTEUR
    // =========================================================

    public int OBEP_add_author( String lastName, String firstName,String birthDay) {

        if (lastName == null || firstName == null || birthDay == null) {
            return -1;
        }

        String requete = "ADD_AUTHOR#"  + lastName + "#" + firstName + "#" + birthDay + "#";

        String reponse = echange(requete);

        if (reponse == null) {
            return -1;
        }

        String[] token = reponse.split("#", -1);

        if (token.length < 2 || !token[0].equals("ADD_AUTHOR") || !token[1].equals("ok")) {
            return -1;
        }

        try {
            return Integer.parseInt(token[2]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // =========================================================
    // AJOUTER UN SUJET
    // =========================================================

    public int OBEP_add_subject(String name) {
        if (name == null) {
            return -1;
        }

        String requete = "ADD_SUBJECT#" + name + "#";
        String reponse = echange(requete);


        if (reponse == null) {
            return -1;
        }

        String[] token = reponse.split("#", -1);

        if (token.length < 2 || !token[0].equals("ADD_SUBJECT") || !token[1].equals("ok")) {
            return -1;
        }

        try {
            return Integer.parseInt(token[2]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }


    /// ////////////////////////////////////////////////////////////////////////////////
    public int OBEP_add_book(Integer authorId, Integer subjectId, String title, String isbn, int pageCount, int stockQuantity, float price, int publishYear) {
        String requete = "ADD_BOOK#" + authorId + "#" + subjectId + "#" + title + "#" + isbn + "#" + pageCount + "#" + stockQuantity + "#" + price + "#" + publishYear;
        String reponse = echange(requete);

        if (reponse == null) {
            return -1;
        }
        String[] token = reponse.split("#", -1);
        if (token.length < 2|| !token[0].equals("ADD_BOOK") || !token[1].equals("ok")) {
            return -1;
        }
        try {
            return Integer.parseInt(token[2]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /// /////////////////////////////////////////////////////////////////////////////
    public void OBEP_close()
    {
        tcp.fermer();
    }

}
