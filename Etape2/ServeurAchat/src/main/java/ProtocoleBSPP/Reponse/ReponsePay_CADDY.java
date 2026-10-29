package ProtocoleBSPP.Reponse;

import protocol.Reponse;

public class ReponsePay_CADDY implements Reponse {
    private static final long serialVersionUID = 1L;
    private boolean resultat;

    public ReponsePay_CADDY(boolean res) {
        resultat = res;
    }
    public boolean getResultat()
    {
        return resultat;
    }

}
