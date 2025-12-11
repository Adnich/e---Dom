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

    // Metode za klik na dugmad
    @FXML
    private void showPrijave(ActionEvent event) {
        loadView("/views/prijave.fxml");
    }

    @FXML
    private void showStudenti(ActionEvent event) {
        loadView("/views/studenti.fxml");
    }

    @FXML
    public void initialize() {
        loadView("/views/dashboard-view.fxml");
    }

    // Pomoćna metoda za učitavanje FXML-a
    private void loadView(String fxmlPath) {
        if (contentArea == null) return; // zaštita za Scene Builder

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
