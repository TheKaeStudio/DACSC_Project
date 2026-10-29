import com.formdev.flatlaf.FlatDarkLaf;
import controller.ControllerView;
import model.ConnectServeur;
import view.GUI.ViewClientAchat;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {

    /** Usage : Main [adresse_serveur] [port]   (défaut : localhost 50000) */
    public static void main(String[] args) {
        FlatDarkLaf.setup();

        SwingUtilities.invokeLater(() -> {
            try {
                String host = args.length >= 1 ? args[0] : "localhost";
                int port = args.length >= 2 ? Integer.parseInt(args[1]) : 50000;

                ConnectServeur connectServeur = new ConnectServeur(host, port);
                ViewClientAchat view = new ViewClientAchat();
                ControllerView controller = new ControllerView(view, connectServeur);
                view.setController(controller);
                controller.run();                       // affiche la fenêtre
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Erreur au démarrage : " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
