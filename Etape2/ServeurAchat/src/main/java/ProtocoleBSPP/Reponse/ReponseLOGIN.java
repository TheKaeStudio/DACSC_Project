package ProtocoleBSPP.Reponse;

import protocol.Reponse;

public class ReponseLOGIN implements Reponse {
    private static final long serialVersionUID = 1L;
    private final boolean valide;
    private final String message;

    public ReponseLOGIN(boolean valide, String message) {
        this.valide = valide;
        this.message = message;
    }

    public boolean isValide() { return valide; }
    public String getMessage() { return message; }
}
