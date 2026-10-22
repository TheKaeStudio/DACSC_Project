package Client.Protocol;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class TCP {
    private Socket clientsocket;
    private DataInputStream entree;
    private DataOutputStream sortie;

    public TCP() {
    }

    public boolean connecter(String ip, int port) {
        //fermer();

        try {
            // Création de la socket et connexion sur le serveur
            System.out.println("appel Socket");
            clientsocket = new Socket(ip, port);
            System.out.println(clientsocket);

            // Création des flux
            sortie = new DataOutputStream(clientsocket.getOutputStream());
            entree = new DataInputStream(clientsocket.getInputStream());

            return true;

        } catch (IOException e) {
            fermer();
            System.err.println("Erreur de connexion : " + e.getMessage());
            return false;
        }
    }

    // Envoyer une requête au serveur
    public boolean send(String message) {
        if (!estConnecte()) {
            return false;
        }

        try {

            String messagePlusEntete = String.format("%04d",  message.getBytes(StandardCharsets.UTF_8).length) + message;

            sortie.write(messagePlusEntete.getBytes(StandardCharsets.UTF_8));
            sortie.flush();
            return true;

        } catch (IOException e) {
            fermer();
            return false;
        }
    }


    public String receive() {
        if (!estConnecte()) {
            return null;
        }

        try {
            // Lire l'entête de 4 caractères
            byte[] entete = new byte[4];
            //readFully() est une méthode de DataInputStream qui permet de lire le nombre d'octet du buffer
            entree.readFully(entete);

            int taille = Integer.parseInt(new String(entete, StandardCharsets.UTF_8) );

            // Lire le contenu du message
            byte[] donnees = new byte[taille];
            entree.readFully(donnees);

            return new String(donnees, StandardCharsets.UTF_8);

        } catch (IOException | NumberFormatException e) {
            fermer();
            return null;
        }
    }

    // Vérifier si la socket est ouverte
    public boolean estConnecte() {
        return clientsocket != null && clientsocket.isConnected() && !clientsocket.isClosed();
    }

    // Fermer la connexion
    public void fermer() {
        try {
            if (clientsocket != null) {
                clientsocket.close();
            }
        } catch (IOException e) {
            System.err.println( "Erreur de fermeture : " + e.getMessage());
        } finally {
            clientsocket = null;
            entree = null;
            sortie = null;
        }
    }

}
