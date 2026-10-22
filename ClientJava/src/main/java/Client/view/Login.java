package Client.view;
import Client.controller.ControllerView;
public interface Login {

    String getLogin();

    String getPassword();
    void showError(String message);

    void close();
}
