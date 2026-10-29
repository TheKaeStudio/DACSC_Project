package ServeurGeneriqueTCP.thread;

import ServeurGeneriqueTCP.logg.Logger;
import protocol.FinConnexionException;
import protocol.Protocole;
import protocol.Reponse;
import protocol.Requete;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Traite UNE requête reçue sur la socket (serveur de requêtes) puis ferme la socket.
 */
public abstract class ThreadClient extends Thread {
    private static final AtomicInteger numCourant = new AtomicInteger(1);

    protected Protocole protocole;
    protected Socket csocket;
    protected Logger logger;
    private final int numero;

    // modèle "à la demande" : une socket est fournie dès la création
    public ThreadClient(Protocole protocole, Socket csocket, Logger logger) throws IOException {
        super("TH Client (protocole=" + protocole.getNom() + ")");
        this.protocole = protocole;
        this.logger = logger;
        this.csocket = csocket;
        this.numero = numCourant.getAndIncrement();
    }

    // modèle "en pool" : la socket sera fournie plus tard par la file d'attente
    public ThreadClient(Protocole protocole, ThreadGroup groupe, Logger logger) throws IOException {
        super(groupe, "TH Client (protocole=" + protocole.getNom() + ")");
        this.protocole = protocole;
        this.logger = logger;                 // (oublié avant : NullPointerException dans chaque thread du pool)
        this.csocket = null;
        this.numero = numCourant.getAndIncrement();
    }

    @Override
    public void run() {
        if (csocket == null) {
            logger.Trace("Erreur : socket client non initialisée.");
            return;
        }
        ObjectOutputStream oos = null;
        try {
            logger.Trace("TH Client " + numero + " : traitement de la requête...");
            oos = new ObjectOutputStream(csocket.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(csocket.getInputStream());

            Requete requete = (Requete) ois.readObject();
            Reponse reponse;
            try {
                System.out.println("je lu dans le flux : " + requete);
                reponse = protocole.TraiteRequete(requete, csocket);
                System.out.println("la reponse recu : " + reponse);
            } catch (FinConnexionException ex) {
                logger.Trace("Fin connexion demandée par protocole");
                reponse = ex.getReponse();
            }
            if (reponse != null) {
                oos.writeObject(reponse);
                oos.flush();
            }
            logger.Trace("TH Client " + numero + " : réponse envoyée au client.");
        } catch (IOException ex) {
            logger.Trace("Erreur I/O : " + ex.getMessage());
        } catch (ClassNotFoundException | ClassCastException ex) {
            logger.Trace("Erreur requete invalide : " + ex.getMessage());
        } catch (RuntimeException ex) {
            // une erreur imprévue ne doit jamais tuer un thread du pool
            logger.Trace("Erreur inattendue : " + ex);
        } finally {
            try {
                csocket.close();
            } catch (IOException ex) {
                logger.Trace("Erreur fermeture socket");
            }
        }
    }
}
