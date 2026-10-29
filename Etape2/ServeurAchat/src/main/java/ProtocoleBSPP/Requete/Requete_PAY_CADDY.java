package ProtocoleBSPP.Requete;

import protocol.RequeteBSPP;

/** PAY_CADDY : id du client dont on paie le caddie en cours. */
public class Requete_PAY_CADDY extends RequeteBSPP {
    private static final long serialVersionUID = 1L;
    private final Integer clientId;

    public Requete_PAY_CADDY(Integer clientId) {
        this.clientId = clientId;
    }

    public Integer getClientId() { return clientId; }

    @Override
    public String toString() {
        return "Requete_PAY_CADDY{clientId=" + clientId + '}';
    }
}
