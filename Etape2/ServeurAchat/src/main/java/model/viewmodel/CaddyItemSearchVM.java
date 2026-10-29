package model.viewmodel;

public class CaddyItemSearchVM {
    private Integer idCaddy;
    private Integer idLivre;
    private Integer quantite;

    public CaddyItemSearchVM(Integer idCaddy, Integer idLivre, Integer quantite) {
        this.idCaddy = idCaddy;
        this.idLivre = idLivre;
        this.quantite = quantite;
    }

    public Integer getIdCaddy() { return idCaddy; }
    public void setIdCaddy(Integer idCaddy) { this.idCaddy = idCaddy; }
    public Integer getIdLivre() { return idLivre; }
    public void setIdLivre(Integer idLivre) { this.idLivre = idLivre; }
    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }
}
