package ServeurGeneriqueTCP.thread;

import ServeurGeneriqueTCP.logg.Logger;
import protocol.Protocole;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ThreadServeurPool extends ThreadServeur {
    private final FileAttente connexionsEnAttente;
    private final ThreadGroup pool;
    private final int taillePool;

    public ThreadServeurPool(int port, Protocole protocole, int taillePool, Logger logger) throws IOException {
        super(port, protocole, logger);
        connexionsEnAttente = new FileAttente();
        pool = new ThreadGroup("POOL");
        this.taillePool = taillePool;
    }

    @Override
    public void run() {
        logger.Trace("Démarrage du TH Serveur (Pool)...");
        try {
            for (int i = 0; i < taillePool; i++) {
                new ThreadClientPool(protocole, connexionsEnAttente, pool, logger).start();
            }
        } catch (IOException ex) {
            logger.Trace("Erreur I/O lors de la création du pool de threads");
            return;
        }

        try {
            ssocket.setSoTimeout(2000);     // pour pouvoir vérifier régulièrement l'interruption
        } catch (IOException ex) {
            logger.Trace("Erreur I/O (timeout)");
        }

        while (!isInterrupted()) //isInterrupted() est définie dans la classe mere Thread
        {
            try {
                Socket csocket = ssocket.accept();
                logger.Trace("Connexion acceptée, mise en file d'attente.");
                connexionsEnAttente.addConnexion(csocket);
            } catch (SocketTimeoutException ex) {
                // simple réveil périodique, rien à signaler
            } catch (IOException ex) {
                logger.Trace("Erreur I/O : " + ex.getMessage());
            }
        }
        logger.Trace("TH Serveur (Pool) interrompu.");
        pool.interrupt();
        try {
            ssocket.close();
        } catch (IOException ignored) {
        }
    }
}
