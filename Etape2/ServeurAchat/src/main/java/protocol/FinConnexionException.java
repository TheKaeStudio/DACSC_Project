package protocol;

public class FinConnexionException extends Exception {
    private static final long serialVersionUID = 1L;
    private final transient Reponse reponse;

    public FinConnexionException(Reponse reponse)
    {
        super("Fin de Connexion décidée par protocole");
        this.reponse = reponse;
    }

    public Reponse getReponse()
    {
        return reponse;
    }
}
