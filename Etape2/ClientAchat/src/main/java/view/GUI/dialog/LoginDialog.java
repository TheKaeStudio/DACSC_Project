package view.GUI.dialog;

import controller.ControllerView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** Fenêtre (modale) de connexion / inscription du vendeur. */
public class LoginDialog extends JDialog {
    private final JTextField login = new JTextField(20);
    private final JPasswordField password = new JPasswordField(20);
    private final JButton btnConnexion = new JButton("Se connecter");
    private final JButton btnInscription = new JButton("S'inscrire");
    private final JButton btnQuitter = new JButton("Quitter");

    public LoginDialog(Frame owner) {
        super(owner, "Connexion du vendeur", true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        JPanel formulaire = new JPanel(new GridBagLayout());
        formulaire.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = 0;
        formulaire.add(new JLabel("Login :"), c);
        c.gridx = 1;
        formulaire.add(login, c);
        c.gridx = 0; c.gridy = 1;
        formulaire.add(new JLabel("Mot de passe :"), c);
        c.gridx = 1;
        formulaire.add(password, c);

        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        boutons.add(btnConnexion);
        boutons.add(btnInscription);
        boutons.add(btnQuitter);

        btnConnexion.setActionCommand("LOGIN_CONNEXION");
        btnInscription.setActionCommand("LOGIN_INSCRIPTION");
        btnQuitter.setActionCommand("LOGIN_QUITTER");

        setLayout(new BorderLayout());
        add(formulaire, BorderLayout.CENTER);
        add(boutons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnConnexion);       // Entrée = se connecter
        pack();
        setLocationRelativeTo(owner);
    }

    public void setController(ControllerView controller) {
        ActionListener l = controller;
        btnConnexion.addActionListener(l);
        btnInscription.addActionListener(l);
        btnQuitter.addActionListener(l);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                btnQuitter.doClick();                        // fermer la fenêtre = quitter
            }
        });
    }

    public String getLogin() {
        return login.getText().trim();
    }

    public String getPassword() {
        return new String(password.getPassword());
    }

    //Remet le formulaire à zéro (après une déconnexion).
    public void vider() {
        login.setText("");
        password.setText("");
        login.requestFocusInWindow();
    }

    public void setBoutonsActifs(boolean actifs) {
        btnConnexion.setEnabled(actifs);
        btnInscription.setEnabled(actifs);
    }
}
