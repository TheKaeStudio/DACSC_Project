package ServeurGeneriqueTCP;

import ProtocoleBSPP.BSPP;
import ServeurGeneriqueTCP.logg.FileLogger;
import ServeurGeneriqueTCP.thread.ThreadServeurPool;
import ServeurGeneriqueTCP.utils.ConfigLoader;
import protocol.Protocole;

public class MainServeur {
    public static void main(String[] args) {
        try {
            ConfigLoader config = new ConfigLoader();
            FileLogger fileLogger = new FileLogger();
            Protocole protocole = new BSPP(fileLogger);       // ouvre la connexion unique à la BD
            int port = Integer.parseInt(config.get("PORT_PAYMENT"));
            int taillePool = Integer.parseInt(config.get("POOL_THREAD"));

            ThreadServeurPool serveur = new ThreadServeurPool(port, protocole, taillePool, fileLogger);
            serveur.start();

            fileLogger.Trace("Serveur Achat lancé sur le port " + port + " (pool de " + taillePool + " threads)");
        } catch (Exception e) {
            System.err.println("Erreur au lancement du serveur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
