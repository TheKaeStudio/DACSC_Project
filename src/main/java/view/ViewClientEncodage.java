
package hepl.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ViewClientEncodage extends JFrame {

    private JTextField titreField =champLecture();
    private JTextField isbnField = champLecture();
    private JTextField pagesField = champLecture();
    private JTextField anneeField = champLecture();
    private JTextField prixField = champLecture();
    private JTextField stockField = champLecture();

    private JComboBox<String> auteurCombo;
    private JComboBox<String> sujetCombo;

    private final DefaultTableModel tableModel = creerModel("Id", "Titre", "Auteur", "Sujet", "ISBN", "Pages", "Année", "Prix", "Stock");

    private JButton ajouterAuteur = creerBouton("Ajouter",   new Color(52, 152, 219), "ADD_AUTHOR");;
    private JButton ajouterSujet =creerBouton("Ajouter",   new Color(52, 152, 219), "ADD_SUBJECT");;
    private JButton vider=creerBouton("Vider les champs",   new Color(219, 122, 52), "VIDER");;
    private JButton ajouterLivre = creerBouton("Ajouter nouveau livre",   new Color(74, 166, 116), "ADD_BOOK");;

    private JTable listeLivre = new JTable(tableModel);

    public ViewClientEncodage() {
        setTitle("Application Encodage");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 500);
        setLocationRelativeTo(null);

        // Nous construirons l'interface ici.
    }

    private static JButton creerBouton(String texte, Color bg, String cmd) {
        JButton b = new JButton(texte);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Arial", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(140, 45));
        b.setActionCommand(cmd);
        return b;
    }
    private static JTextField champLecture() {
        JTextField f = new JTextField();
        f.setEditable(false);
        return f;
    }
    private static DefaultTableModel creerModel(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }
}
