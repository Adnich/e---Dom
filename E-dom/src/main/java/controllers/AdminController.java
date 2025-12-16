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
    public void showPregledSistema(javafx.event.ActionEvent event) {
        loadView("/views/dashboard-view.fxml");
    }

    // Metode za klik na dugmad
    @FXML
    public void showPrijave(ActionEvent event) {
        loadView("/views/prijave.fxml");
    }

    @FXML
    public void showStudenti(ActionEvent event) {
        loadView("/views/studenti.fxml");
    }

    @FXML
    public void initialize() {
        loadView("/views/dashboard-view.fxml");
    }

    // Pomoćna metoda za učitavanje FXML-a
    private void loadView(String fxmlPath) {
        if (contentArea == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // ✅ global CSS (ako imaš)
            var globalCss = getClass().getResource("/css/app.css"); // opcionalno
            if (globalCss != null && !view.getStylesheets().contains(globalCss.toExternalForm())) {
                view.getStylesheets().add(globalCss.toExternalForm());
            }

            // ✅ view-specific CSS: prijave
            if ("/views/prijave.fxml".equals(fxmlPath)) {
                var prijaveCss = getClass().getResource("/css/prijave.css");
                System.out.println("prijave.css = " + prijaveCss);
                if (prijaveCss != null && !view.getStylesheets().contains(prijaveCss.toExternalForm())) {
                    view.getStylesheets().add(prijaveCss.toExternalForm());
                }
            }

            Object controller = loader.getController();
            if (controller instanceof DashboardController) {
                ((DashboardController) controller).setAdminController(this);
            }

            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}