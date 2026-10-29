package ProtocoleBSPP.Requete;

import protocol.RequeteBSPP;

public class Requete_CANCEL_CADDY extends RequeteBSPP {
    private static final long serialVersionUID = 1L;
    private Integer clientId;

    public Requete_CANCEL_CADDY(Integer clientId) {
        this.clientId = clientId;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    @Override
    public String toString() {
        return "Requete_CANCEL_CADDY{" +
                "clientId=" + clientId +
                '}';
    }
}
