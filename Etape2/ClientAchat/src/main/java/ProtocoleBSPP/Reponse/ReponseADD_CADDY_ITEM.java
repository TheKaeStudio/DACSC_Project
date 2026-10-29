package ProtocoleBSPP.Reponse;

import protocol.Reponse;

public class ReponseADD_CADDY_ITEM implements Reponse {
    private static final long serialVersionUID = 1L;
    private boolean resultat;

    public ReponseADD_CADDY_ITEM(boolean resultat) {
        this.resultat = resultat;
    }
    public boolean getResultat()
    {
        return resultat;
    }
}
