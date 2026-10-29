package ProtocoleBSPP.Reponse;

import protocol.Reponse;

public class ReponseERROR implements Reponse {
    private static final long serialVersionUID = 1L;
    private final String message;

    public ReponseERROR(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }

    @Override
    public String toString() {
        return "Reponse_ERROR{" + "message='" + message + '\'' + '}';
    }
}
