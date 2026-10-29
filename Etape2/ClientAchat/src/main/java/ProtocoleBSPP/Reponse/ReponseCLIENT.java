package ProtocoleBSPP.Reponse;

import protocol.Reponse;

public class ReponseCLIENT implements Reponse {
    private static final long serialVersionUID = 1L;
    private Integer idClient;

    public ReponseCLIENT(Integer idClient) {
        this.idClient = idClient;
    }
    public Integer getIdClient()
    {
        return idClient;
    }
}
