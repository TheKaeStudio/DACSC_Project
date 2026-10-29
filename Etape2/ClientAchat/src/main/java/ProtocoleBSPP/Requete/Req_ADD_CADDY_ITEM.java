package ProtocoleBSPP.Requete;

import protocol.RequeteBSPP;

/** ADD_CADDY_ITEM : le serveur est sans état, la requête porte donc le numéro du client. */
public class Req_ADD_CADDY_ITEM extends RequeteBSPP {
    private static final long serialVersionUID = 1L;
    private final Integer clientId;
    private final Integer livreId;
    private final Integer quantity;

    public Req_ADD_CADDY_ITEM(Integer clientId, Integer livreId, Integer quantity) {
        this.clientId = clientId;
        this.livreId = livreId;
        this.quantity = quantity;
    }

    public Integer getClientId() { return clientId; }
    public Integer getLivreId() { return livreId; }
    public Integer getQuantity() { return quantity; }

    @Override
    public String toString() {
        return "Req_ADD_CADDY_ITEM{clientId=" + clientId + ", livreId=" + livreId + ", quantity=" + quantity + '}';
    }
}
