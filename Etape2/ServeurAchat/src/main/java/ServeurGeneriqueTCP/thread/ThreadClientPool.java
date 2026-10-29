package ServeurGeneriqueTCP.thread;

import ServeurGeneriqueTCP.logg.Logger;
import protocol.Protocole;

import java.io.IOException;

public class ThreadClientPool extends ThreadClient {
    private final FileAttente connexionsEnAttente;

    public ThreadClientPool(Protocole protocole, FileAttente file, ThreadGroup groupe, Logger logger) throws IOException {
        super(protocole, groupe, logger);
        connexionsEnAttente = file;
    }

    @Override
    public void run() {
        logger.Trace("TH Client (Pool) démarre...");
        while (!isInterrupted()) {
            try {
                csocket = connexionsEnAttente.getConnexion();   // attend une socket
                super.run();                                    // traite une requête
                csocket = null;
            } catch (InterruptedException ex) {
                logger.Trace("Demande d'interruption...");
                break;
            }
        }
        logger.Trace("TH Client (Pool) se termine.");
    }
}
