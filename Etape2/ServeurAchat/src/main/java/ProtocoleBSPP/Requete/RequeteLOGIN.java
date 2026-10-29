package ProtocoleBSPP.Requete;

import protocol.RequeteBSPP;

/** LOGIN : authentification d'un employé (vendeur), ou création de son compte si newUser est vrai. */
public class RequeteLOGIN extends RequeteBSPP {
    private static final long serialVersionUID = 1L;
    private final String login;
    private final String password;
    private final boolean newUser;

    public RequeteLOGIN(String login, String password, boolean newUser) {
        this.login = login;
        this.password = password;
        this.newUser = newUser;
    }

    public String getLogin() { return login; }
    public String getPassword() { return password; }
    public boolean isNewUser() { return newUser; }

    @Override
    public String toString() {      // jamais le mot de passe dans les logs
        return "RequeteLOGIN{login='" + login + "', newUser=" + newUser + '}';
    }
}
