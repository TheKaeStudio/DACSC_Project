package protocol;

import java.net.Socket;

public interface Protocole {
    String getNom();

    /**
     * Traite une requête et retourne la réponse.
     * Lève FinConnexionException quand le protocole décide de terminer la communication
     * (le serveur générique ne sait pas lui-même quand fermer).
     */
    Reponse TraiteRequete(Requete requete, Socket socket) throws FinConnexionException;
}
