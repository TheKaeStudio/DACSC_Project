
package Client.view;

import Client.controller.ControllerView;
import Client.model.entity.User;

public interface viewClient {

    // Récupérer les données saisies
    String getTitre();
    String getIsbn();
    int getPages();
    int getAnnee();
    float getPrix();
    int getStock();
    String getSujet();
    String getAuteur();

    // Actions d'affichage
    void showError(String message);
    void showMessage(String message);
    void viderChamps();
    void close();
    User promptNouveauUser();
    User promptUser();
    // Relier la vue au contrôleur
    void setController(ControllerView c);
}

