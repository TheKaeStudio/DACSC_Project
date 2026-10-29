
package view.GUI;


import controller.ControllerView;
import view.viewClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ViewClientAchat extends JFrame implements viewClient {

    private ControllerView controller;

    // Menu
    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu menu = new JMenu("☰");
    private final JMenuItem deconnexionItem = new JMenuItem("Se déconnecter");
    private final JMenuItem quitterItem = new JMenuItem("Quitter");
    private final JLabel vendeurLabel = new JLabel(" ");

    // Informations du client
    private final JTextField nomClientField = new JTextField(15);
    private final JTextField prenomClientField = new JTextField(15);

    private final JLabel clientCourantLabel = new JLabel("Aucun client sélectionné");
    private final JLabel totalLabel = new JLabel("Total : 0.00 €");
    private final JButton rechercherClient = creerBouton("Rechercher client",new Color(46, 204, 113), "RECHERCHER_CLIENT");

    private final JButton enregistrerClient =
            creerBouton("Enregistrer client", new Color(52, 152, 219), "ENREGISTRER_CLIENT");

    // Critères de recherche des livres
    private final JTextField titreRechercheField = new JTextField(12);
    private final JTextField auteurNomRechercheField = new JTextField(10);
    private final JTextField auteurPrenomRechercheField = new JTextField(10);
    private final JTextField sujetRechercheField = new JTextField(12);

    private final JSpinner prixMaxField = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100000.0, 1.0));

    private final JButton rechercherLivres =
            creerBouton("Rechercher",new Color(52, 152, 219), "RECHERCHER_LIVRES");

    // Table des livres trouvés
    private final DefaultTableModel livresModel = creerModel(
            "ID", "Titre", "Auteur", "Sujet", "Prix", "Stock"
    );

    private final JTable livresTable = new JTable(livresModel);

    private final JSpinner quantiteField =
            new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));

    private final JButton ajouterPanier =
            creerBouton("Ajouter au panier",new Color(52, 152, 219), "AJOUTER_PANIER");

    // Panier
    private final DefaultTableModel panierModel = creerModel(
            "ID Livre", "Titre", "Prix unitaire", "Quantité", "Sous-total"
    );

    private final JTable panierTable = new JTable(panierModel);

    private final JButton supprimerArticle =
            creerBouton("Supprimer",new Color(231, 76, 60), "SUPPRIMER_ARTICLE");

    private final JButton viderPanier =
            creerBouton("Vider le panier", new Color(234, 156, 67),"VIDER_PANIER");

    private final JButton payer =
            creerBouton("Payer",new Color(46, 204, 113), "PAYER");

    private final JButton annuler =
            creerBouton("Annuler",new Color(204, 107, 46), "ANNULER");

    public ViewClientAchat() {
        setTitle("Gestion des ventes de livres");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);   // le contrôleur gère la sortie (rend le stock)
        setSize(1200, 850);
        setMinimumSize(new Dimension(950, 650));
        setLocationRelativeTo(null);

        // Menu
        menu.add(deconnexionItem);
        menu.addSeparator();
        menu.add(quitterItem);

        menuBar.add(menu);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(vendeurLabel);
        menuBar.add(Box.createHorizontalStrut(10));
        setJMenuBar(menuBar);

        deconnexionItem.setActionCommand("DECONNEXION");
        quitterItem.setActionCommand("QUITTER");

        // Interface principale
        JPanel contenu = new JPanel(new BorderLayout(10, 10));
        contenu.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        contenu.add(creerZoneClient(), BorderLayout.NORTH);

        JPanel centre = new JPanel(new GridLayout(2, 1, 10, 10));
        centre.add(creerZoneRecherche());
        centre.add(creerZonePanier());

        contenu.add(centre, BorderLayout.CENTER);

        setContentPane(contenu);
        setActionsActives(false);                // activés après le login du vendeur
    }

    private JPanel creerZoneClient() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Client"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        panel.add(new JLabel("Nom :"), c);

        c.gridx = 1;
        panel.add(nomClientField, c);

        c.gridx = 2;
        panel.add(new JLabel("Prénom :"), c);

        c.gridx = 3;
        panel.add(prenomClientField, c);

        c.gridx = 0;
        c.gridy = 1;
        panel.add(rechercherClient, c);

        c.gridx = 1;
        panel.add(enregistrerClient, c);

        c.gridx = 2;
        c.gridwidth = 2;
        panel.add(clientCourantLabel, c);

        return panel;
    }

    private JPanel creerZoneRecherche() {
        JPanel panel = new JPanel(new BorderLayout(5, 8));
        panel.setBorder(
                BorderFactory.createTitledBorder("Recherche de livres")
        );

        JPanel criteres = new JPanel(new FlowLayout(FlowLayout.LEFT));

        criteres.add(new JLabel("Titre :"));
        criteres.add(titreRechercheField);

        criteres.add(new JLabel("Auteur (nom) :"));
        criteres.add(auteurNomRechercheField);
        criteres.add(new JLabel("Auteur (prénom) :"));
        criteres.add(auteurPrenomRechercheField);

        criteres.add(new JLabel("Sujet :"));
        criteres.add(sujetRechercheField);

        criteres.add(new JLabel("Prix max (0 = sans limite) :"));
        criteres.add(prixMaxField);

        criteres.add(rechercherLivres);

        panel.add(criteres, BorderLayout.NORTH);
        panel.add(new JScrollPane(livresTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(new JLabel("Quantité :"));
        actions.add(quantiteField);
        actions.add(ajouterPanier);

        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel creerZonePanier() {
        JPanel panel = new JPanel(new BorderLayout(5, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Panier du client"));

        panel.add(new JScrollPane(panierTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(totalLabel);
        actions.add(supprimerArticle);
        actions.add(viderPanier);
        actions.add(payer);
        actions.add(annuler);

        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private JButton creerBouton(String texte, Color bg, String commande) {
        JButton bouton = new JButton(texte);
        bouton.setBackground(bg);
        bouton.setForeground(Color.WHITE);
        bouton.setFont(new Font("Arial", Font.BOLD, 14));
        bouton.setFocusPainted(false);
        bouton.setBorderPainted(false);
        Dimension taille = bouton.getPreferredSize();
        bouton.setPreferredSize(new Dimension(Math.max(140, taille.width + 24), 45));
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

    // Valeurs du client
    public String getNomClient() {
        return nomClientField.getText().trim();
    }

    public String getPrenomClient() {
        return prenomClientField.getText().trim();
    }

    // Critères de recherche
    public String getTitreRecherche() {
        return titreRechercheField.getText().trim();
    }

    public String getAuteurNomRecherche() {
        return auteurNomRechercheField.getText().trim();
    }

    public String getAuteurPrenomRecherche() {
        return auteurPrenomRechercheField.getText().trim();
    }

    public String getSujetRecherche() {
        return sujetRechercheField.getText().trim();
    }

    public double getPrixMaximum() {
        return ((Number) prixMaxField.getValue()).doubleValue();
    }

    public int getQuantite() {
        return ((Number) quantiteField.getValue()).intValue();
    }

    // Sélections dans les tables
    public int getLigneLivreSelectionnee() {
        return livresTable.getSelectedRow();
    }

    public int getLignePanierSelectionnee() {
        return panierTable.getSelectedRow();
    }

    // Accès aux modèles pour afficher les résultats du serveur
    public DefaultTableModel getLivresModel() {
        return livresModel;
    }

    public DefaultTableModel getPanierModel() {
        return panierModel;
    }

    public void setVendeur(String login) {
        vendeurLabel.setText(login == null ? " " : "Vendeur : " + login);
        setActionsActives(login != null);      // rien ne fonctionne tant que le vendeur n'est pas connecté
    }

    private void setActionsActives(boolean actif) {
        for (JButton b : new JButton[]{rechercherClient, enregistrerClient, rechercherLivres, ajouterPanier,
                supprimerArticle, viderPanier, payer, annuler}) {
            b.setEnabled(actif);
        }
        deconnexionItem.setEnabled(actif);
    }

    public void setClientCourant(Integer clientId) {
        clientCourantLabel.setText(clientId == null ? "Aucun client sélectionné" : "Client n° " + clientId);
    }

    public void setTotal(double total) {
        totalLabel.setText(String.format("Total : %.2f €", total));
    }

    public void viderChampsClient() {
        nomClientField.setText("");
        prenomClientField.setText("");
    }

    public void viderPanierAffiche() {
        panierModel.setRowCount(0);
    }

    public void viderLivresAffiches() {
        livresModel.setRowCount(0);
    }

    @Override
    public void setController(ControllerView c) {
        this.controller = c;

        deconnexionItem.addActionListener(c);
        quitterItem.addActionListener(c);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                c.actionPerformed(new java.awt.event.ActionEvent(ViewClientAchat.this,
                        java.awt.event.ActionEvent.ACTION_PERFORMED, "QUITTER"));
            }
        });

        rechercherClient.addActionListener(c);
        enregistrerClient.addActionListener(c);
        rechercherLivres.addActionListener(c);
        ajouterPanier.addActionListener(c);
        supprimerArticle.addActionListener(c);
        viderPanier.addActionListener(c);
        payer.addActionListener(c);
        annuler.addActionListener(c);
    }

    @Override
    public void close() {
        dispose();
    }

    @Override
    public void showError(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Erreur", JOptionPane.ERROR_MESSAGE
        );
    }

    @Override
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Information",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
