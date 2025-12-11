package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class AdminController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        // automatski otvori PRIJAVE kad se otvori admin panel
        showPrijave(null);
    }

    @FXML
    private void showPrijave(ActionEvent event) {
        loadView("/views/prijave.fxml");   // prilagodi path ako ti je drugačiji
    }

    @FXML
    private void showStudenti(ActionEvent event) {
        loadView("/views/studenti.fxml");  // prilagodi path ako ti je drugačiji
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            // ovdje možeš dodati i neku error poruku ako želiš
        }
    }
}
