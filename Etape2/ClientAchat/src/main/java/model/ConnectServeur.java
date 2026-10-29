package model;

import ProtocoleBSPP.Reponse.*;
import ProtocoleBSPP.Requete.*;
import protocol.Reponse;
import protocol.Requete;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Côté client du protocole BSPP. Le serveur Achat est un serveur de requêtes :
 * une connexion TCP est ouverte pour chaque requête, puis refermée après la réponse.
 */
public class ConnectServeur {
    private static final int TIMEOUT_CONNEXION_MS = 5000;
    private static final int TIMEOUT_LECTURE_MS = 15000;

    private final String host;
    private final int port;

    public ConnectServeur(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() { return host; }
    public int getPort() { return port; }

    /** Envoie une requête et attend la réponse. Lève IOException si le serveur est injoignable. */
    private Reponse sendRequest(Requete requete) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), TIMEOUT_CONNEXION_MS);
            socket.setSoTimeout(TIMEOUT_LECTURE_MS);

            // le flux de sortie est créé (et vidé) en premier des deux côtés : pas d'interblocage
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            oos.writeObject(requete);
            oos.flush();

            Object reponse = ois.readObject();
            if (reponse instanceof ReponseERROR err) {
                throw new IOException("Erreur du serveur : " + err.getMessage());
            }
            return (Reponse) reponse;
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IOException("Réponse du serveur invalide : " + e.getMessage(), e);
        }
    }

    //Connexion (ou création de compte si newUser). Retourne la réponse pour afficher son message.
    public ReponseLOGIN login(String login, String password, boolean newUser) throws IOException {
        System.out.println("login et password recu " + login + " " + password);

        return (ReponseLOGIN) sendRequest(new RequeteLOGIN(login, password, newUser));
    }

    //Numéro du client, ou -1 s'il est introuvable / invalide.
    public int client(String nom, String prenom, boolean nouveau) throws IOException {
        Reponse r = sendRequest(new Requete_CLIENT(nom, prenom, nouveau));
        return ((ReponseCLIENT) r).getIdClient();
    }

    public ReponseSELECT_BOOK selectBook(Requete_SELECT_BOOK requete) throws IOException {
        return (ReponseSELECT_BOOK) sendRequest(requete);
    }

    public boolean addCaddyItem(int clientId, int livreId, int quantite) throws IOException {
        return ((ReponseADD_CADDY_ITEM) sendRequest(new Req_ADD_CADDY_ITEM(clientId, livreId, quantite))).getResultat();
    }

    public boolean delCaddyItem(int clientId, int livreId, int quantite) throws IOException {
        return ((ReponseDEL_CADDY_ITEM) sendRequest(new Req_DEL_CADDY_ITEM(clientId, livreId, quantite))).getResultat();
    }

    public boolean cancelCaddy(int clientId) throws IOException {
        return ((ReponseCANCEL_CADDY) sendRequest(new Requete_CANCEL_CADDY(clientId))).getResultat();
    }

    public boolean payCaddy(int clientId) throws IOException {
        return ((ReponsePay_CADDY) sendRequest(new Requete_PAY_CADDY(clientId))).getResultat();
    }
}
