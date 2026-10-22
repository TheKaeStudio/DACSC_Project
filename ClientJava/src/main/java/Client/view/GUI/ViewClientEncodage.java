package Client.view.GUI;

import Client.controller.ControllerView;
import Client.model.entity.Author;
import Client.model.entity.Subject;
import Client.model.entity.User;
import Client.view.GUI.dialog.LoginDialog;
import Client.view.viewClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ViewClientEncodage extends JFrame implements viewClient {

    private ControllerView cv;
    // =========================================================
    // CHAMPS DU FORMULAIRE
    // =========================================================

    private final JTextField titreField = creerChamp();
    private final JTextField isbnField = creerChamp();
    private JSpinner pagesField = new JSpinner(new SpinnerNumberModel(0, 0,1000, 1 ));
    private JSpinner anneeField = new JSpinner( new SpinnerNumberModel(1900, 1000, 2100, 1)
    );
    private JSpinner prixField = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100000.0, 0.5));
    private JSpinner stockField = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));

    private final JComboBox<Author> auteurCombo = new JComboBox<>();
    private final JComboBox<Subject> sujetCombo = new JComboBox<>();


    // =========================================================
    // MENU
    // =========================================================

    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu menu = new JMenu("☰");

    private final JMenuItem loginItem = new JMenuItem("Login");
    private final JMenuItem logoutItem = new JMenuItem("Logout");
    private final JMenuItem quitterItem = new JMenuItem("Quitter");

    // =========================================================
    // TABLEAU DES LIVRES
    // =========================================================

    private final DefaultTableModel tableModel = creerModel(
            "Id", "Titre", "Auteur", "Sujet", "ISBN",
            "Pages", "Année", "Prix", "Stock"
    );

    private final JTable listeLivre = new JTable(tableModel);

    // =========================================================
    // BOUTONS
    // =========================================================

    private final JButton ajouterAuteur = creerBouton(
            "Ajouter", new Color(52, 152, 219), "ADD_AUTHOR"
    );

    private final JButton ajouterSujet = creerBouton(
            "Ajouter", new Color(52, 152, 219), "ADD_SUBJECT"
    );

    private final JButton vider = creerBouton(
            "Vider les champs", new Color(219, 122, 52), "VIDER"
    );

    private final JButton ajouterLivre = creerBouton(
            "Ajouter nouveau livre",
            new Color(74, 166, 116),
            "ADD_BOOK"
    );

    // =========================================================
    // CONSTRUCTEUR
    // =========================================================

    public ViewClientEncodage() {
        setTitle("Application Encodage");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setMinimumSize(new Dimension(850, 500));
        setLocationRelativeTo(null);

        // ---------------- MENU ----------------

        menu.add(loginItem);
        menu.add(logoutItem);
        menu.addSeparator();
        menu.add(quitterItem);

        menuBar.add(menu);
        setJMenuBar(menuBar);

        loginItem.setActionCommand("LOGIN");
        logoutItem.setActionCommand("LOGOUT");
        quitterItem.setActionCommand("QUITTER");

        // ---------------- FORMULAIRE ----------------

        JPanel formulaire = new JPanel(new GridBagLayout());

        formulaire.setBorder(
                BorderFactory.createTitledBorder(
                        "Informations du livre"
                )
        );

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        ajouterChamp(formulaire, c, 0, 0, "Titre :", titreField);
        ajouterChamp(formulaire, c, 0, 1, "ISBN :", isbnField);
        ajouterChamp(formulaire, c, 0, 2, "Pages :", pagesField);
        ajouterChamp(formulaire, c, 0, 3, "Prix :", prixField);

        ajouterChamp(formulaire, c, 2, 0, "Auteur :", auteurCombo);
        ajouterChamp(formulaire, c, 2, 1, "Sujet :", sujetCombo);
        ajouterChamp(formulaire, c, 2, 2, "Année :", anneeField);
        ajouterChamp(formulaire, c, 2, 3, "Stock :", stockField);

        c.gridx = 4;
        c.gridy = 0;
        c.weightx = 0;
        formulaire.add(ajouterAuteur, c);

        c.gridy = 1;
        formulaire.add(ajouterSujet, c);

        c.gridy = 3;
        formulaire.add(vider, c);

        // ---------------- TABLEAU ----------------

        listeLivre.setFillsViewportHeight(true);
        listeLivre.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        listeLivre.setSelectionMode(ListSelectionModel.SINGLE_SELECTION  );

        JScrollPane scrollPane = new JScrollPane(listeLivre);

        JLabel titreTableau = new JLabel(
                "Liste des livres encodés lors de cette session :"
        );

        titreTableau.setForeground(new Color(45, 80, 180));
        titreTableau.setFont(new Font("Arial", Font.BOLD, 15));

        JPanel partieTableau = new JPanel(
                new BorderLayout(5, 10)
        );

        partieTableau.add(titreTableau, BorderLayout.NORTH);
        partieTableau.add(scrollPane, BorderLayout.CENTER);
        partieTableau.add(ajouterLivre, BorderLayout.SOUTH);

        // ---------------- ORGANISATION GÉNÉRALE ----------------

        JPanel contenu = new JPanel(
                new BorderLayout(10, 15)
        );

        contenu.setBorder( BorderFactory.createEmptyBorder(10, 10, 10, 10));

        contenu.add(formulaire, BorderLayout.NORTH);
        contenu.add(partieTableau, BorderLayout.CENTER);

        setContentPane(contenu);
        activerBoutons(false); //je desactive le bouton

        /// /////////////////////////////////////////////////////////////////
        //pour l'evenement click on doit logout                           //
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);             //
        addWindowListener(new WindowAdapter() {                           //
            @Override                                                     //
            public void windowClosing(WindowEvent e) {                    //
                System.out.println("L'utilisateur a cliqué sur la croix !");
                if (cv != null) {                                         //
                    cv.deconnecter();
                }
            }
        });
    }

    public void activerBoutons(boolean actif) {
        ajouterAuteur.setEnabled(actif);
        ajouterSujet.setEnabled(actif);
        ajouterLivre.setEnabled(actif);
        vider.setEnabled(actif);
    }

    // =========================================================
    // CRÉATION DES COMPOSANTS
    // =========================================================

    private void ajouterChamp(   JPanel panel,  GridBagConstraints c, int x, int y,
            String label,JComponent composant)
    {
        c.gridwidth = 1;
        c.weightx = 0;
        c.gridx = x;
        c.gridy = y;

        panel.add(new JLabel(label), c);

        c.gridx = x + 1;
        c.weightx = 1;

        panel.add(composant, c);
    }

    private static JTextField creerChamp() {
        return new JTextField(15);
    }

    private static JButton creerBouton(
            String texte,
            Color bg,
            String commande
    ) {
        JButton bouton = new JButton(texte);

        bouton.setBackground(bg);
        bouton.setForeground(Color.WHITE);
        bouton.setFont(new Font("Arial", Font.BOLD, 14));
        bouton.setFocusPainted(false);
        bouton.setBorderPainted(false);
        bouton.setPreferredSize(new Dimension(170, 40));
        bouton.setActionCommand(commande);

        return bouton;
    }

    private static DefaultTableModel creerModel(String... colonnes) {
        return new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int ligne, int colonne) {
                return false;
            }
        };
    }
    public String demanderNomSujet() {
        String nom = JOptionPane.showInputDialog(this,"Entrez le nom du sujet :",
                "Ajouter un sujet", JOptionPane.PLAIN_MESSAGE);

        if (nom == null) {
            return null; // L'utilisateur a annulé
        }

        nom = nom.trim();

        if (nom.isEmpty()) {
            return "";
        }

        return nom;
    }

    public String demanderLastnameAuteur() {
        String nom = JOptionPane.showInputDialog(this,"Entrez le nom de l'auteur :",
                "Ajouter un sujet", JOptionPane.PLAIN_MESSAGE);
        if (nom == null) {
            return null; // L'utilisateur a annulé
        }

        nom = nom.trim();

        if (nom.isEmpty()) {
            return "";
        }

        return nom;

    }
    public String demanderFirstnameAuteur() {
        String prenom = JOptionPane.showInputDialog(this,"Entrez le prenom de l'auteur :",
                "Ajouter un sujet", JOptionPane.PLAIN_MESSAGE);
        if (prenom == null) {
            return null; // L'utilisateur a annulé
        }

        prenom = prenom.trim();

        if (prenom.isEmpty()) {
            return "";
        }

        return prenom;

    }
    public String demanderBirthdayAuteur() {
        String date = JOptionPane.showInputDialog(this,"Entrez la date de l'auteur:",
                "Ajouter un sujet", JOptionPane.PLAIN_MESSAGE);
        if (date == null) {
            return null; // L'utilisateur a annulé
        }

        date = date.trim();

        if (date.isEmpty()) {
            return "";
        }

        return date;

    }


    // =========================================================
    // RÉCUPÉRATION DES VALEURS
    // =========================================================

    @Override
    public String getTitre() {
        return titreField.getText().trim();
    }

    @Override
    public String getIsbn() {
        return isbnField.getText().trim();
    }

    @Override
    public int getPages() {
        return (int) pagesField.getValue();
    }

    @Override
    public int getAnnee() {
        return (int) anneeField.getValue();
    }

    @Override
    public float getPrix() {
        return ((Number) prixField.getValue()).floatValue();
    }

    @Override
    public int getStock() {
        return (int) stockField.getValue();
    }

    @Override
    public String getSujet() {
        Object sujet = sujetCombo.getSelectedItem();

        return sujet == null ? "" : sujet.toString();
    }

    @Override
    public String getAuteur() {
        Object auteur = auteurCombo.getSelectedItem();

        return auteur == null ? "" : auteur.toString();
    }

    // =========================================================
    // AFFICHAGE DES MESSAGES
    // =========================================================

    @Override
    public void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Erreur",
                JOptionPane.ERROR_MESSAGE
        );
    }

    @Override
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Information",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // =========================================================
    // VIDER LE FORMULAIRE
    // =========================================================

    @Override
    public void viderChamps() {
        titreField.setText("");
        isbnField.setText("");
        pagesField.setValue(0);
        anneeField.setValue(1900);
        prixField.setValue(00.0);
        stockField.setValue(0);

        auteurCombo.setSelectedIndex(-1);
        sujetCombo.setSelectedIndex(-1);
    }

    // =========================================================
    // FERMER LA FENÊTRE
    // =========================================================

    @Override
    public void close() {
        dispose();
    }

    @Override
    public User promptNouveauUser() {
        LoginDialog login= new LoginDialog(this);
        login.setVisible(true);
        return login.getResultat();
    }

    @Override
    public User promptUser() {
        LoginDialog dialog = new LoginDialog(this);
        dialog.setVisible(true);
        return dialog.getResultat();
    }

    // =========================================================
    // CONNEXION AU CONTRÔLEUR
    // =========================================================

    @Override
    public void setController(ControllerView c) {
        this.cv = c;
        ajouterAuteur.addActionListener(c);
        ajouterSujet.addActionListener(c);
        vider.addActionListener(c);
        ajouterLivre.addActionListener(c);

        loginItem.addActionListener(c);
        logoutItem.addActionListener(c);
        quitterItem.addActionListener(c);
    }

    // =========================================================
    // MÉTHODES UTILISABLES PAR LE CONTRÔLEUR
    // =========================================================

    public void ajouterLivreDansTableau(Object... donnees) {
        if (donnees == null || donnees.length != 9) {
            throw new IllegalArgumentException(
                    "Un livre doit contenir exactement 9 valeurs."
            );
        }

        tableModel.addRow(donnees);
    }

    // Ajouter les objets reçus du serveur
    public void ajouterAuteurDansListe(Author auteur) {
        auteurCombo.addItem(auteur);
    }

    public void ajouterSujetDansListe(Subject sujet) {
        sujetCombo.addItem(sujet);
    }
    public void viderListeAuteurs() {
        auteurCombo.removeAllItems();
    }

    public void viderListeSujets() {
        sujetCombo.removeAllItems();
    }

    public void viderTableau() {
        tableModel.setRowCount(0);
    }

    // Récupérer l'auteur sélectionné
    public Author getAuteurSelectionne() {
        return (Author) auteurCombo.getSelectedItem();
    }

    // Récupérer le sujet sélectionné
    public Subject getSujetSelectionne() {
        return (Subject) sujetCombo.getSelectedItem();
    }


}