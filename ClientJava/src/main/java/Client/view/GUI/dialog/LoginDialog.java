package Client.view.GUI.dialog;

import Client.model.entity.User;
import Client.view.Login;

import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog implements Login {

    private User user = null;

    private final JTextField login = new JTextField(20);
    private final JPasswordField password = new JPasswordField(20);

    private final JButton btnConnexion = new JButton("Connexion");
    private final JButton btnInscription = new JButton("S'inscrire");

    public LoginDialog(Frame owner) {
        super(owner, "Connexion", true);

        // Champs du formulaire
        JPanel formulaire = new JPanel(new GridLayout(2, 2, 5, 5));

        formulaire.add(new JLabel("Login :"));
        formulaire.add(login);

        formulaire.add(new JLabel("Mot de passe :"));
        formulaire.add(password);

        // Boutons
        JPanel boutons = new JPanel();

        boutons.add(btnConnexion);
        boutons.add(btnInscription);

        // Fenêtre
        setLayout(new BorderLayout(10, 10));
        add(formulaire, BorderLayout.CENTER);
        add(boutons, BorderLayout.SOUTH);


        btnConnexion.addActionListener(e -> valider());
        btnInscription.addActionListener(e -> valider());

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(owner);
    }

    private void valider() {
        String nom = getLogin();
        String motDePasse = getPassword();

        if (nom.isEmpty() || motDePasse.isEmpty()) {
            showError("Veuillez remplir le login et le mot de passe.");
            return;
        }

        user = new User();
        user.setName(nom);
        user.setPassword(motDePasse);

        dispose();
    }
    @Override
    public String getLogin() {
        return login.getText().trim();
    }

    @Override
    public String getPassword() {
        return new String(password.getPassword());
    }

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
    public void close() {
        dispose();
    }

    public User getResultat() {
        return user;
    }
}