package ProtocoleBSPP.Reponse;

import protocol.Reponse;

public class ReponseCANCEL_CADDY implements Reponse {
    private static final long serialVersionUID = 1L;
    private boolean resultat;

    public ReponseCANCEL_CADDY(boolean resultat) {
        this.resultat = resultat;
    }
    public boolean getResultat()
    {
        return resultat;
    }
}
