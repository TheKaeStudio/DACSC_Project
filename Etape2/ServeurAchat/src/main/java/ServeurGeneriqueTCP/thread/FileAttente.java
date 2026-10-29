package ServeurGeneriqueTCP.thread;

import java.net.Socket;
import java.util.LinkedList;

public class FileAttente {
    private final LinkedList<Socket> fileAttente = new LinkedList<>();

    public synchronized void addConnexion(Socket socket) {
        fileAttente.addLast(socket);
        notify();           // réveille un thread du pool
    }

    public synchronized Socket getConnexion() throws InterruptedException {
        while (fileAttente.isEmpty())
            wait();
        return fileAttente.removeFirst();
    }
}
