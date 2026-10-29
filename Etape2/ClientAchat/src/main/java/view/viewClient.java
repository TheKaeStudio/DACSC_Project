package view;

import controller.ControllerView;

public interface viewClient {
    void setController(ControllerView controller);

    void showError(String message);

    void showMessage(String message);

    void close();
}
